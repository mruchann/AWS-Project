from pyspark.sql import SparkSession
import numpy as np
import pandas as pd
from pyspark.ml import feature, regression, evaluation
#from pyspark.mllib import evaluation
from sklearn import model_selection

SHOW_VALUES = True

spark = SparkSession.builder.master("local[1]") \
  .appName("Project2") \
  .getOrCreate()

class Wine:
  def __init__(self, d, reduce_dims=False):
    #d = d.replace("file:/", "")
    self.myData = spark.read.options(delimiter=",", header=True, inferSchema=True) \
      .csv(d)
    #self.myData = pd.DataFrame.to_numpy(self.myData)

    assembler = feature.VectorAssembler(inputCols=["fixed acidity", 	"volatile acidity", "citric acid", "residual sugar", "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density", "pH", "sulphates", "alcohol"], outputCol="features")
    self.data = assembler.transform(self.myData.drop("_c12"))
    if reduce_dims:
      p = feature.PCA(k=3)
      p.fit(self.data)
      self.data = p.transform(self.data)
    #self.target = np.ravel(self.myData[:, -1:])
    #self.data.data.rename(columns={"quality": "label"}, inplace=True)
    self.data = self.data.withColumnRenamed('quality', 'label')

    print(f"Found {len(self.data.columns)-2} attributes with {(self.data.count())} samples")

  def normalize(self, a):
    sclr = feature.MinMaxScaler(inputCol="features", outputCol="scaledFeatures")
    sclr = sclr.fit(a)
    return sclr.transform(a)

#w = Wine("winequality-white.csv")
w = Wine("winequality-white.csv")
x_train, x_test = w.data.randomSplit([0.8, 0.2], seed=42)

x_train = w.normalize(x_train)
x_test = w.normalize(x_test)

def runModel(m):
  model = models.get(m)
  tsf = model.fit(x_train)
  y_pred = tsf.transform(x_test)
  #y_pred2 = x_test.rdd.map(lambda lp: (float(tsf.predict(lp.features)), lp.label))
  #y_pred.select("label", "prediction").show(10)
  #print(y_pred2.take(6))
  score = evaluation.RegressionEvaluator(metricName="rmse")
  #score = metrics.mean_squared_error(y_test, y_pred, squared=False)
  result = score.evaluate(y_pred)
  print(f"{m} : {result}")
  #try:
  print("The following parameters were used for this model.")
  #  print(f"### F1 Score ### {str(model)} : {metrics.f1_score(y_test, y_pred, average='micro')}")
  #except ValueError:
  #  pass
  if SHOW_VALUES: print("A sampling of the results..."); y_pred.select("label", "prediction").show(6)

def getMeSomeResults(models):
  print('''#### Root Mean Squared Error ####\n#### Closer to 0.0 is better ####''')
  if SHOW_VALUES: print("\nSample test data:"); x_test.select('label').show(6)
  for m in models: # Expecting a list of models
    runModel(m)

models = {
  "LinearRegression()" : regression.LinearRegression(),
  "RandomForestRegressor()" : regression.RandomForestRegressor()
}

getMeSomeResults(models)