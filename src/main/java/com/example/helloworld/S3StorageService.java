package com.example.helloworld;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final boolean autoCreateBucket;

    public S3StorageService(
            S3Client s3Client,
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.s3.auto-create-bucket:true}") boolean autoCreateBucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.autoCreateBucket = autoCreateBucket;
    }

    public void ensureBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException exception) {
            createBucket();
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                createBucket();
            } else {
                throw exception;
            }
        }
    }

    public void createBucket() {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
    }

    public void put(String key, byte[] content, String contentType) {
        var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
    }

    public byte[] get(String key) {
        var request = GetObjectRequest.builder().bucket(bucket).key(key).build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    public List<S3Object> list() {
        return s3Client.listObjectsV2(request -> request.bucket(bucket)).contents();
    }

    public void delete(String key) {
        s3Client.deleteObject(request -> request.bucket(bucket).key(key));
    }

    public String bucket() {
        return bucket;
    }

    public boolean autoCreateBucket() {
        return autoCreateBucket;
    }
}