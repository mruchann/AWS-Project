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
# Upload code #######
scp -i yourkey.pem -r proj1_{a,b} ec2-user@YourInstance:~/proj1_{a,b}
# This is from a local machine to remote server.
#####################
wget https://services.gradle.org/distributions/gradle-6.9.1-bin.zip -P /tmp
sudo unzip -d /opt/gradle /tmp/gradle-6.9.1-bin.zip
sudo nano /etc/profile.d/gradle.sh
# in gradle.sh ######
> export GRADLE_HOME=/opt/gradle/gradle-6.9.1
> export PATH=${GRADLE_HOME}/bin:${PATH}
#####################
sudo chmod +x /etc/profile.d/gradle.sh
source /etc/profile.d/gradle.sh
sudo nano ~/.bash_profile
# in .bash_profile ##
> export AWS_ACCESS_KEY_ID=EXAMPLEKEY # Keys to access AWS services
> export AWS_SECRET_ACCESS_KEY=SUPERSECRET12345
> export AWS_DEFAULT_REGION=us-west-2
#####################
```

### Building the Project
```bash
cd ~/proj1_a/proj1_a
gradle build
gradle wrapper --gradle-version 6.9.1
```

Now the project is ready to go.

## Running the Project

Start the project on EC2 B and then EC2 A. It does not necessarily matter which is first, but this order demonstrates the messages being received and processed in parallel.

```bash
gradle run # On each instance
```

EC2 A will start processing the images and sending filenames with cars to the queue. EC2 B is polling the queue waiting for filenames so it can download the images from the S3 bucket. It will detect text in these images and write the results to a file `output.txt`.

### Output

|EC2 A|EC2 B|
|-----|-----|
|Detected labels for 1.jpeg|Processing 1.jpeg|
|Detected labels for 10.jpeg||
|Detected labels for 2.jpeg||
|Detected labels for 3.jpeg|Processing 3.jpeg|
|Detected labels for 4.jpeg||
|Detected labels for 5.jpeg||
|Detected labels for 6.jpeg|Processing 6.jpeg|
|Detected labels for 7.jpeg||
|Detected labels for 8.jpeg|Processing 8.jpeg|
|Detected labels for 9.jpeg||

At the end `-1` is sent which signals to EC2 B to exit.

## Brief Code Overview

I will discuss the main functions of each driver file.

### Driver A

```java
public static void main(String[] args){
  // 1 EC2-A will Get an image from S3 bucket
  // 2 EC2-A will use rekognition to Detect Label
  // 3 If the image file has a car, EC2-A will push the file name to SQS
  
  // Create queue
  SqsClient sqsClient = SqsClient.builder()
      .region(Region.US_WEST_2)
      .build();
  
  Random rand = new Random();
  int groupID = rand.nextInt(999); // For SQS MessageGroupID
  String q_url = "https://sqs.us-west-2.amazonaws.com/XXXXXXXXXX/P1_Q.fifo";
  List<String> imageNames = getS3ObjectNames("unr-cs442");

  // Download and label 10 images
  // If we find a car, send the filename to the queue
  for (String f : imageNames){
    String filename = getAndDetectLabels(f); // Sends image to Rekognition
    if (filename != "None"){ // Filename returned if 90% confident image is car
      sendToSQS(filename, q_url, groupID, sqsClient);
    }
  }

  // Stop index
  sendToSQS("-1", q_url, groupID, sqsClient);
}
```

### Driver B

```java
public static void main(String[] args){
  // 4 EC2-B will retrieve image filename from SQS
  // 5 EC2-B will retrieve image from S3 bucket
  // 6 EC2-B will Detect Text

  String q_url = "https://sqs.us-west-2.amazonaws.com/XXXXXXXXXX/P1_Q.fifo";
  createFile(); // Output file created or ready to be appended
  String filename = "None";
  SqsClient client = p1.SQSExample.getClient();

  do{
    filename = getMessage(q_url, client); // Checks SQS for messages
    if ((filename != "None") && (filename.indexOf("-1") == -1)){
      // Received a filename
      System.out.println("Processing " + filename);
      // Download image
      p1.GetObjectData.getObjectBytes("unr-cs442", filename, ("./" + filename));
      // Detect text in image
      List<TextDetection> textCollection = p1.DetectText.detectTextLabels(filename);
      // Add this text to output
      addTextToFile(filename, textCollection);
    }
  }while (filename.indexOf("-1") == -1); // Continue polling until stop index
}
```