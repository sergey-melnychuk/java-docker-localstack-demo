package edu.blog.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileServiceImpl implements FileService {

    private final String bucket;
    private final AmazonS3 amazonS3;

    public FileServiceImpl(String bucket, AmazonS3 amazonS3) {
        this.bucket = bucket;
        this.amazonS3 = amazonS3;
    }

    @Override
    public String get(String key) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucket, key);
        try (S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(objectInputStream));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    @Override
    public void put(String key, String data) {
        amazonS3.putObject(bucket, key, data);
    }

    @Override
    public void createBucket(String bucket) {
        amazonS3.createBucket(bucket);
    }
}
