package edu.blog.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class FileServiceFactory {
    private static final String AWS_SIGNIN_REGION = "us-east2";

    public static FileService custom(String bucket, String awsServiceEndpoint) {
        return new FileServiceImpl(bucket, getS3(awsServiceEndpoint));
    }

    private static AmazonS3 getS3(String awsServiceEndpoint) {

        /*
        For standard client following line of code would be enough:
        `return AmazonS3ClientBuilder.defaultClient();`
         */

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        /*
        EndpointConfiguration has only constructor taking 2 arguments: service endpoint and sign-in region,
        thus both values must be provided. Using the 'us-east2' value as a sign-in region gets the job done.
         */

        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsServiceEndpoint, AWS_SIGNIN_REGION));

        /*
        Enable path-style access in order to ensure service endpoint is not taken into account,
        (which is "${BUCKET_NAME}.localhost" for this client) as it is not valid DNS name.
         */

        builder.enablePathStyleAccess();

        return builder.build();
    }
}
