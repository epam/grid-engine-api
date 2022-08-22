## GRID-ENGINE-API
____
### Table of contents
_____
* [Introduction](#introduction)
* [Features](#features)
* [Build process](#build-process)
  * [Using jar file](#using-jar-file)
  * [Using docker](#using-docker)
* [System overview](#system-overview)
* [API Reference](#api-reference)
* [Developer guide](#developer-guide)

### Introduction
______
This is a RESTful API which provides a convenient way for interaction with different types of grid engines (SGE, SLURM, etc.)

### Features
____
- Helps to unify work with different types of grid engines
- Allows to simplify interaction with different grid engines for other services
- Provides remote interaction with grid engines

### Build process
________
To build and run this application locally you'll need Java 11 or later, Git and Docker to be installed on your computer.
From your command line:
### *Using jar file*

````
# Clone this repository
$ git clone https://github.com/ekazachkova/grid-engine-api.git

# Go into the repository
$ cd grid-engine-api

# Build
$ ./gradlew build

# Run the app
$ java -jar build/libs/grid-engine-api-1.0-SNAPSHOT.jar
````
### *Using docker*
```
# Clone this repository
$ git clone https://github.com/ekazachkova/grid-engine-api.git

# Go into the repository
$ cd grid-engine-api

# Build
$ ./gradlew build

# Build Docker image
$ docker build -f ./docker/Dockerfile -t grid-engine-api:latest .

# Run Docker image
$ docker run -d -p 8080:8080 grid-engine-api:latest 
```

For more information please [see](docker/sge/README.md)

### System overview
____
The figure shows schematically how the application is arranged. It is logically divided into three levels:
1. Controllers - provide an interface for interacting with the application;
2. Services - contain common code (not related to any grid-engine: preprocessing, verification, etc.) and also connect the API with the provider;
3. Providers - implement interaction of the application directly with the underlying client of the cluster (SGE, Slurm, etc.), file system, etc.


![grid-engine-api-diag](docs/attachments/images/grid-engine-api-diagram.png)

### API Reference
_____
API Reference docs can be found [here](docs/api) 

### Developer guide
_____
Developer guide can be found [here](docs/developer_guide/developer_guide.md)
