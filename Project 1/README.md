# Programming Assignment 1
##### Nick Alvarez
###### CS 442 - Fall 2021

## Setting Up AWS

This project requires two EC2 instances and an SQS queue to communicate between them.

### EC2

The following properties were used to configure each instance:

- Amazon Linux 2 AMI (HVM), SSD Volume Type - 64 bit (x86)
- t2.micro (1 vCPUs, 2.5 GHz 1 GiB memory, EBS only)
- General Purpose SSD (8 GiB)

It is assumed the user is able to configure a security group and access the instances via SSH and associated keys.

An Access Key must be created to access AWS Services such as Rekognition.

### SQS

One FIFO queue must be created with the following properties:

- Maximum message size: 256 KB
- Message retention period: 1 Hour
- Default visibility timeout: 5 Seconds
- Delivery delay: 0 Seconds
- Receive message wait time: 5 Seconds
- Content-based deduplication: Disabled
- High throughput FIFO: Disabled
- Deduplication scope: Message group
- FIFO throughput limit: Per queue

## Configure EC2

The EC2 instances cannot run the project out of the box. Some packages must be installed to build and run the project. These instructions apply to each instance.

### Setting up Environment
```sh
sudo yum install java-devel
mkdir proj1_{a,b} # Respective to each instance
# Upload code ####
scp -i yourkey.pem -r proj1_{a,b} ec2-user@YourInstance:~/proj1_{a,b}
# This is from a local machine to remote server.
##################
wget https://services.gradle.org/distributions/gradle-6.9.1-bin.zip -P /tmp
sudo unzip -d /opt/gradle /tmp/gradle-6.9.1-bin.zip
sudo nano /etc/profile.d/gradle.sh
# in gradle.sh ###
> export GRADLE_HOME=/opt/gradle/gradle-6.9.1
> export PATH=${GRADLE_HOME}/bin:${PATH}
##################
sudo chmod +x /etc/profile.d/gradle.sh
source /etc/profile.d/gradle.sh
```

### Building the Project
```sh
cd ~/proj1_a/proj1_a
gradle build
gradle wrapper --gradle-version 6.9.1
gradle run
```