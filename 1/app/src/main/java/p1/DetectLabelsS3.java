// snippet-sourcedescription:[DetectLabels.java demonstrates how to capture labels (like water and mountains)  from a given image located in an Amazon Simple Storage Service (Amazon S3) bucket.]
//snippet-keyword:[AWS SDK for Java v2]
// snippet-service:[Amazon Rekognition]
// snippet-keyword:[Code Sample]
// snippet-sourcetype:[full-example]
// snippet-sourcedate:[09-27-2021]
// snippet-sourceauthor:[scmacdon - AWS]
/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package p1;
// snippet-start:[rekognition.java2.detect_labels_s3.import]
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import java.util.List;
// snippet-end:[rekognition.java2.detect_labels_s3.import]

/**
 * To run this Java V2 code example, ensure that you have setup your development environment, including your credentials.
 *
 * For information, see this documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */

public class DetectLabelsS3 {

    public static void main(String[] args) {

        final String USAGE = "\n" +
                "Usage: " +
                "   <bucket> <image>\n\n" +
                "Where:\n" +
                "   bucket - the name of the Amazon S3 bucket that contains the image (for example, ,ImageBucket)." +
                "   image - the name of the image located in the Amazon S3 bucket (for example, Lake.png). \n\n";

         if (args.length != 2) {
             System.out.println(USAGE);
             System.exit(1);
         }

        String bucket = args[0];
        String image = args[1];
        Region region = Region.US_WEST_2;
        RekognitionClient rekClient = RekognitionClient.builder()
                .region(region)
                .build();

        getLabelsfromImage(rekClient, bucket, image);
        rekClient.close();
    }

    public static List<Label> driver(String[] args){
      String bucket = args[0];
      String image = args[1];
      Region region = Region.US_WEST_2;
      RekognitionClient rekClient = RekognitionClient.builder()
              .region(region)
              .build();

      List<Label> output = getLabelsfromImage(rekClient, bucket, image);
      rekClient.close();
      return output;
    }

    // snippet-start:[rekognition.java2.detect_labels_s3.main]
    public static List<Label> getLabelsfromImage(RekognitionClient rekClient, String bucket, String image) {
        try {
        //List<String> labels = new List<String>();

        S3Object s3Object = S3Object.builder()
                .bucket(bucket)
                .name(image)
                .build() ;

        Image myImage = Image.builder()
                .s3Object(s3Object)
                .build();

        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                .image(myImage)
                .maxLabels(10)
                .build();

        DetectLabelsResponse labelsResponse = rekClient.detectLabels(detectLabelsRequest);
        List<Label> labels = labelsResponse.labels();

        System.out.println("Detected labels for " + image);
        /*for (Label label: labels) {
            System.out.println(label.name() + ": " + label.confidence().toString());
        }*/

        return labels;

    } catch (RekognitionException e) {
        System.out.println(e.getMessage());
        System.exit(1);
        return null;
    }
  }
    // snippet-end:[rekognition.java2.detect_labels_s3.main]
}