# Programming Assignment 1
##### Nick Alvarez
###### CS 442 - Fall 2021

## Required Information

The code repository can be found on [GitHub](https://github.com/nalvarez508/cs442) (but if you're reading this, you're probably already there).

The docker image that can run the application is found at [DockerHub](https://hub.docker.com/r/nalvarez508/cs442).

## Running the Project

Luckily, training these models does not take very long.

### Locally

Some libraries are required to run the local version of the app. I didn't include a requirements file, so the following commands will install them.

#### [p2.py](p2.py) (The Good Version)
Install the required modules with `pip install scikit-learn numpy`.

Run the project with `python3 p2.py`.

#### [p2-spark.py](p2-spark.py) (The Spark Version)
Install the required modules with `pip install pyspark mllib numpy pandas`. You will also need some kind of Java runtime on your machine with `JAVA_HOME` pointing to this.

Run the project with `python3 p2-spark.py`.

### With Docker

You'll need [Docker](https://docs.docker.com/get-docker/).

With Docker installed, run `docker pull nalvarez508/cs442` to download the image. Then, run it with `docker run nalvarez508/cs442`. Any required libraries or binaries will be downloaded automatically. This may take some time.

The associated file is [p2-spark.py](p2-spark.py).