package io.localmotion.storage.file.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.localmotion.storage.file.FileAccessor;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import lombok.SneakyThrows;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Requires(env="aws")
public class S3FileAccessor implements FileAccessor {

    @Value("${aws.s3.region}")
    private String clientRegion;

    @Override
    public boolean fileExists(String location, String path, String name) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
//                .withCredentials(new ProfileCredentialsProvider())
                .build();

        return s3Client.doesObjectExist(location, getS3Key(path, name));
    }

    @SneakyThrows
    public List<String> readFile(String location, String path, String name) {
        List<String> result = new ArrayList<>();
        S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
//                    .withCredentials(new ProfileCredentialsProvider())
//                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .build();

            // Get an object and print its contents.
            System.out.println("Downloading an object");
            fullObject = s3Client.getObject(new GetObjectRequest(location, getS3Key(path, name)));
            System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());
            System.out.println("Content: ");
            result = readLinesFromStream(fullObject.getObjectContent());
        } finally {
            // To ensure that the network connection doesn't remain open, close any open input streams.
            if (fullObject != null) {
                fullObject.close();
            }
            if (objectPortion != null) {
                objectPortion.close();
            }
            if (headerOverrideObject != null) {
                headerOverrideObject.close();
            }
        }

         return result;
    }

//    public List<String> readFile(String location, String name) {
//        List<String> result = new ArrayList<>();
//        S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
//        try {
//            try {
//                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                        .withRegion(clientRegion)
//                        .withCredentials(new ProfileCredentialsProvider())
//                        .build();
//
//                // Get an object and print its contents.
//                System.out.println("Downloading an object");
//                fullObject = s3Client.getObject(new GetObjectRequest(location, name));
//                System.out.println("Content-Type: " + fullObject.getObjectMetadata().getContentType());
//                System.out.println("Content: ");
//                result = readLinesFromStream(fullObject.getObjectContent());
//
//            } catch (AmazonServiceException e) {
//                // The call was transmitted successfully, but Amazon S3 couldn't process
//                // it, so it returned an error response.
//                e.printStackTrace();
//            } catch (SdkClientException e) {
//                // Amazon S3 couldn't be contacted for a response, or the client
//                // couldn't parse the response from Amazon S3.
//                e.printStackTrace();
//            } finally {
//                // To ensure that the network connection doesn't remain open, close any open input streams.
//                if (fullObject != null) {
//                    fullObject.close();
//                }
//                if (objectPortion != null) {
//                    objectPortion.close();
//                }
//                if (headerOverrideObject != null) {
//                    headerOverrideObject.close();
//                }
//            }
//        } catch(IOException e) {
//             e.printStackTrace();
//        }
//
//         return result;
//    }
//

    private List<String> readLinesFromStream(InputStream input) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        while ((line = reader.readLine()) != null) {
            result.add(line);
            System.out.println(line);
        }
        System.out.println();
        return result;
    }

    public void writeFile(String location, String path, String name, String content) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
//                .withCredentials(new ProfileCredentialsProvider())
                .build();

        s3Client.putObject(location, getS3Key(path, name), content);
    }

//  public void writeFile(String location, String name, String content) {
//        try {
//            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                    .withRegion(clientRegion)
//                    .withCredentials(new ProfileCredentialsProvider())
//                    .build();
//
//            s3Client.putObject(location, name, content);
//        }
//        catch(AmazonServiceException e) {
//            // The call was transmitted successfully, but Amazon S3 couldn't process
//            // it, so it returned an error response.
//            e.printStackTrace();
//        }
//        catch(SdkClientException e) {
//            // Amazon S3 couldn't be contacted for a response, or the client
//            // couldn't parse the response from Amazon S3.
//            e.printStackTrace();
//        }
//    }

    @Override
    public void deleteFile(String location, String path, String name) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
//                .withCredentials(new ProfileCredentialsProvider())
                .build();

        s3Client.deleteObject(location, getS3Key(path, name));
    }

//    @Override
//    public void deleteFile(String location, String name) {
//        try {
//            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                    .withRegion(clientRegion)
//                    .withCredentials(new ProfileCredentialsProvider())
//                    .build();
//
//            s3Client.deleteObject(location, name);
//        }
//        catch(AmazonServiceException e) {
//            // The call was transmitted successfully, but Amazon S3 couldn't process
//            // it, so it returned an error response.
//            e.printStackTrace();
//        }
//        catch(SdkClientException e) {
//            // Amazon S3 couldn't be contacted for a response, or the client
//            // couldn't parse the response from Amazon S3.
//            e.printStackTrace();
//        }
//    }

    private String getS3Key(String path, String name) {
        if (path == null || path.equals(""))
            return name;
        else {
            StringBuilder sb = new StringBuilder();
//            if (!path.startsWith("/"))
//                sb.append("/");
            sb.append(path);
            if (!path.endsWith("/"))
                sb.append("/");
            sb.append(name);
            return sb.toString();
        }
    }
}

