package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    private String bucketName = "rjayb2b"; // Replace with your bucket name

    public List<String> getExamImages() {
        // List all objects in the bucket with a specific prefix, if needed
        return amazonS3.listObjects(bucketName)
                .getObjectSummaries()
                .stream()
                .map(s3Object -> amazonS3.getUrl(bucketName, s3Object.getKey()).toString())
                .collect(Collectors.toList());
    }
}
