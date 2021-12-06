from sklearn import preprocessing, model_selection, linear_model, ensemble, metrics, svm, decomposition, tree
import numpy as np

SHOW_VALUES = True

class Wine:
  def __init__(self, d, reduce_dims=False):
    self.myData = np.genfromtxt(d, delimiter=',', skip_header=1, autostrip=True, usecols=range(0, 12))
    #self.data = self.normalize(self.myData[:, :-1])
    self.data = self.myData[:, :-1]
    if reduce_dims:
      p = decomposition.PCA(3)
      p.fit(self.data)
      self.data = p.transform(self.data)
    self.target = np.ravel(self.myData[:, -1:])

    print(f"Found {self.data.shape[1]} attributes with {self.data.shape[0]} samples")

  def normalize(self, a):
    sclr = preprocessing.PowerTransformer()
    sclr.fit(a)
    return sclr.transform(a)

w = Wine("winequality-white.csv")
x_train, x_test, y_train, y_test = model_selection.train_test_split(w.data, w.target, test_size=0.2, train_size=0.8)

x_train = w.normalize(x_train)
x_test = w.normalize(x_test)

def runModel(model):
  model.fit(x_train, y_train)
  y_pred = model.predict(x_test)
  #y_pred = np.round(y_pred_decimal) # Since we are scoring with integers out of 10, round to the nearest
  score = metrics.mean_squared_error(y_test, y_pred, squared=False)
  print(f"{str(model)} : {score}")
  try:
    print(f"### F1 Score ### {str(model)} : {metrics.f1_score(y_test, y_pred, average='micro')}")
  except ValueError:
    pass
  if SHOW_VALUES: print(f"A sampling of the results...\n{y_pred[:6]}\n")

def getMeSomeResults(models):
  print('''#### Root Mean Squared Error ####\n#### Closer to 0.0 is better ####''')
  if SHOW_VALUES: print(f"\nSample test data:\n{y_test[:6]}\n")
  for m in models: # Expecting a list of models
    runModel(m)

getMeSomeResults([linear_model.LinearRegression(),
linear_model.LogisticRegression(max_iter=1000),
ensemble.RandomForestRegressor(),
svm.SVR(),
tree.DecisionTreeClassifier(),
tree.DecisionTreeRegressor(),
ensemble.RandomForestRegressor(bootstrap=False, max_depth=90, max_features='sqrt', min_samples_leaf=1, min_samples_split=5, n_estimators=1300),
svm.SVR(tol=1e-5, C=2.0, epsilon=0.14), svm.SVR(tol=1e-5, C=4.0, epsilon=0.3),
tree.DecisionTreeClassifier(criterion='gini', max_depth=7, max_leaf_nodes=None, min_samples_leaf=1, min_samples_split=12)])

def helper_tuneModel(est, grid=False):
  print("Using", str(est))
  if str(est) == "RandomForestRegressor()":
    random_grid = {'n_estimators': [int(x) for x in np.linspace(start = 200, stop = 2000, num = 10)],
                  'max_features': ['auto', 'sqrt'],
                  'max_depth': [int(x) for x in np.linspace(10, 110, num = 11)],
                  'min_samples_split': [2, 5, 10],
                  'min_samples_leaf': [1, 2, 4],
                  'bootstrap': [True, False]}
    tuned_grid = {'n_estimators': [int(x) for x in np.linspace(start = 1100, stop = 1300, num = 3)],
                  'max_features': ['sqrt'],
                  'max_depth': [int(x) for x in np.linspace(70, 90, num = 3)],
                  'min_samples_split': [5],
                  'min_samples_leaf': [1],
                  'bootstrap': [False]}
  elif str(est) == "SVR()": #{'tol': 1e-05, 'epsilon': 0.14, 'C': 2.0}
    random_grid = {'tol': [1e-1, 1e-2, 1e-3, 1e-4, 1e-5],
                  'epsilon': [float(x) for x in np.linspace(0.01, 0.3, num=30)],
                  'C': [float(x) for x in np.linspace(0.5, 4.0, 32)]}
    tuned_grid = {'tol': [1e-5],
                  'epsilon': [float(x) for x in np.linspace(0.2, 0.4, num=5)],
                  'C': [float(x) for x in np.linspace(3.0, 5.0, 10)]}
  elif str(est) == "LogisticRegression()":
    random_grid = {'penalty': ['l2'], # 'none', 'l2', 'l1', 'elasticnet'
                  'tol': [1e-4],
                  'C': [float(x) for x in np.linspace(0.5, 3.0, 32)],
                  'solver': ['saga'], # 'newton-cg', 'lbfgs', 'liblinear', 'sag', 'saga'
                  'max_iter': [int(x) for x in np.linspace(100, 3000, 100)]}
  elif str(est) == "DecisionTreeClassifier()":
    random_grid = {"criterion": ['gini', 'entropy'],
                  "max_depth": [int(x) for x in np.linspace(1, 15, 8)],
                  "min_samples_leaf": [int(x) for x in np.linspace(1, 15, 8)],
                  "min_samples_split": [int(x) for x in np.linspace(2, 15, 8)],
                  "max_leaf_nodes": [None, 5, 10, 20]
              }
    tuned_grid = {"criterion": ['gini'],
                  "max_depth": [None, 3, 5, 7],
                  "min_samples_leaf": [1, 6, 11],
                  "min_samples_split": [3, 5, 12],
                  "max_leaf_nodes": [None, 5, 20]
              }
  if grid == False:
    m = model_selection.RandomizedSearchCV(estimator=est, param_distributions=random_grid, n_iter = 30, cv = 3, verbose=1, random_state=42, n_jobs = -1, scoring='neg_root_mean_squared_error')
  elif grid == True:
    m = model_selection.GridSearchCV(estimator=est, param_grid=tuned_grid, cv=3, verbose=2, n_jobs=-1, scoring='neg_root_mean_squared_error')
  runModel(m)
  print(m.best_params_)

#helper_tuneModel(tree.DecisionTreeClassifier(), True)