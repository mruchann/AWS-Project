package p1;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Label;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

//import com.example.rekognition.DetectLabelsS3;
//import com.example.sqs.SQSExample;
//import com.example.sqs.SQSExample;
//import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
//import software.amazon.awssdk.services.sqs.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
//import com.amazonaws.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

//import p1.DetectLabelsS3;
//import p1.SQSExample;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Random;


public class DriverA {
  public static void main(String[] args){
    // 1 EC2-A will Get an image from S3 bucket
    // 2 EC2-A will use rekognition to Detect Label
    // 3 If the image file has a car, EC2-A will push the file name to SQS
    // 4 EC2-B will retrieve image filename from SQS
    // 5 EC2-B will retrieve image from S3 bucket
    // 6 EC2-B will Detect Text
    
    // Create queue
    SqsClient sqsClient = SqsClient.builder()
        .region(Region.US_WEST_2)
        .build();
    
    Random rand = new Random();
    int groupID = rand.nextInt(999);
    //String q_url = p1.SQSExample.createQueue(sqsClient, "P1_Q");
    //System.out.println(q_url);
    String q_url = "https://sqs.us-west-2.amazonaws.com/936324536037/P1_Q.fifo";
    List<String> imageNames = getS3ObjectNames("unr-cs442");
    // Download and label 10 images
    // If we find a car, send the filename to the queue
    //sendToSQS("Test", q_url, sqsClient);
    for (String f : imageNames){
      String filename = getAndDetectLabels(f);
      if (filename != "None"){
        sendToSQS(filename, q_url, groupID, sqsClient);
      }
    }

    // Stop index
    sendToSQS("-1", q_url, groupID, sqsClient);

  }
  public static String getAndDetectLabels(String filename){
    //String args = "unr-cs442 " + filename + ".jpeg";
    String[] args = {"unr-cs442", filename};
    //System.out.println(args);
    //Class_S3 = new DetectLabelsS3();

    // Rekognition
    //List<Label> labels = p1.DetectLabelsS3.driver(args);
    List<Label> labels = p1.DetectLabelsS3.driver(args);

    // If we don't find a car, this will send
    //String sendToSQS = "";
    for (Label label: labels){
      //System.out.println(label.name() + " / " + label.confidence().toString());
      if ((label.name().equals("Car")) && (label.confidence() >= 90.0)){
        return (filename); // Found a car
      }
    }
    return "None"; // Did not find a car
  }

  public static void sendToSQS(String f, String url, int id, SqsClient client){
    //final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    /*SendMessageRequest send_msg_request = new SendMessageRequest()
        .withQueueUrl(url)
        .withMessageBody(f)
        .withDelaySeconds(5);*/
    Random rand = new Random();
    SendMessageRequest send_msg_request = SendMessageRequest.builder()
        .queueUrl(url)
        .messageBody(f)
        .messageDeduplicationId(String.valueOf(rand.nextInt(99999)))
        .messageGroupId("Nick" + id)
        .build();
    //send_msg_request.setMessageGroupId("P1");
    //sqs.sendMessage(send_msg_request);
    client.sendMessage(send_msg_request);
  }

  public static List<String> getS3ObjectNames(String bucketName){
    //Region region = Region.US
    S3Client s3 = S3Client.builder()
        .region(Region.US_WEST_2)
        .build();

    try {
      ListObjectsRequest listObjects = ListObjectsRequest
          .builder()
          .bucket(bucketName)
          .build();

      ListObjectsResponse res = s3.listObjects(listObjects);
      List<S3Object> objects = res.contents();

      List<String> keys = new ArrayList<>();

      for (ListIterator iterVals = objects.listIterator(); iterVals.hasNext(); ) {
        S3Object myValue = (S3Object) iterVals.next();
        //System.out.print("\n The name of the key is " + myValue.key());
        keys.add(myValue.key());
      }
      return keys;

    }
    catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
      return null;
    }
  }
}
