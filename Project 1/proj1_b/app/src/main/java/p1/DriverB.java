package p1;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.rekognition.model.TextDetection;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DriverB {
  public static void main(String[] args){
    // 1 EC2-A will Get an image from S3 bucket
    // 2 EC2-A will use rekognition to Detect Label
    // 3 If the image file has a car, EC2-A will push the file name to SQS
    // 4 EC2-B will retrieve image filename from SQS
    // 5 EC2-B will retrieve image from S3 bucket
    // 6 EC2-B will Detect Text

    String q_url = "https://sqs.us-west-2.amazonaws.com/936324536037/P1_Q.fifo";
    createFile();

    String filename = "None";
    SqsClient client = p1.SQSExample.getClient();

    do{
      filename = getMessage(q_url, client);
      if ((filename != "None") && (filename.indexOf("-1") == -1)){
        System.out.println("Processing " + filename);
        p1.GetObjectData.getObjectBytes("unr-cs442", filename, ("./" + filename));
        List<TextDetection> textCollection = p1.DetectText.detectTextLabels(filename);
        addTextToFile(filename, textCollection);
      }
    }while (filename.indexOf("-1") == -1);

  }

  public static String getMessage(String url, SqsClient client){
    List<Message> msgs = p1.SQSExample.receiveMessages(client, url);
    if (!msgs.isEmpty()) {return msgs.get(0).body();}
    else {return "None";}
  }

  public static void addTextToFile(String image, List<TextDetection> labels){
    try{
      FileWriter write = new FileWriter("output.txt", true);
      write.write(image + "\n--------\n");
      String out = new String();
      for (TextDetection text : labels){
        out += (text.detectedText() + " (" + text.confidence().toString() + "%)\n");
      }
      write.write(out + "--------\n");
      write.close();
    }
    catch (IOException e){
      System.out.println("Something went wrong...");
      e.printStackTrace();
    }
  }

  public static void createFile(){
    try{
      File f = new File("output.txt");
      if (f.createNewFile()) {System.out.println("Output file created.");}
      else {System.out.println("Output file already exists, appending to end.");}
    }
    catch (IOException e){
      System.out.println("Something went wrong...");
      e.printStackTrace();
    }
  }
}
