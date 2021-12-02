from sklearn import preprocessing, model_selection, linear_model, ensemble, metrics
import numpy as np

class Wine:
  def __init__(self, d):
    self.myData = np.genfromtxt(d, delimiter=',', skip_header=1, autostrip=True, usecols=range(0, 12))#, dtype=np.float64, skip_header=1)
    self.data = self.normalize(self.myData[:, :-1])
    self.target = np.ravel(self.myData[:, -1:])

    print(f"Found {self.data.shape[1]} attributes with {self.data.shape[0]} samples")

  def normalize(self, a):
    sclr = preprocessing.MinMaxScaler()
    sclr.fit(a)
    return sclr.transform(a)

w = Wine("winequality-white.csv")
x_train, x_test, y_train, y_test = model_selection.train_test_split(w.data, w.target, test_size=0.2, train_size=0.8)

_lin = linear_model.LinearRegression()
_forest = ensemble.RandomForestRegressor()

_lin.fit(x_train, y_train)
_forest.fit(x_train, y_train)

y_pred_lin = _lin.predict(x_test)
y_pred_forest = _forest.predict(x_test)

linScore_MSE = metrics.mean_squared_error(y_test, y_pred_lin, squared=False)
forestScore_MSE = metrics.mean_squared_error(y_test, y_pred_forest, squared=False)
#linScore_F1 = metrics.f1_score(y_test, y_pred_lin)
#forestScore_F1 = metrics.f1_score(y_test, y_pred_forest)

print(f"Linear Regression MSE: {linScore_MSE}\nRandom Forest MSE: {forestScore_MSE}")
#print(f"Linear Regression F1: {linScore_F1}\nRandom Forest F1: {forestScore_F1}")