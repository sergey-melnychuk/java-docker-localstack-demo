package edu.blog.service;

import java.io.IOException;

public interface FileService {

    /**
     * Get a content under given key
     * @param key key
     * @return
     */
    String get(String key) throws IOException;

    /**
     * Put a data as a content under given key
     * @param key key
     * @param data data
     */
    void put(String key, String data);

    /**
     * Create a bucket with given name
     * @param bucket bucket
     */
    void createBucket(String bucket);
}
