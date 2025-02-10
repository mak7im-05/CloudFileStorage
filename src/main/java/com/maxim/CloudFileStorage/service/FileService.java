package com.maxim.CloudFileStorage.service;

import com.maxim.CloudFileStorage.util.PathUtil;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileService {

    private final String bucketName = "user-files";
    private final MinioClient minioClient;

    public void renameFile(int userId, String path, String oldName, String newName) throws Exception {
        String oldFilePath = PathUtil.buildUserFilePath(userId, path, oldName);
        String newFilePath = PathUtil.buildUserFilePath(userId, path, newName);

        if (fileExists(oldFilePath) && !fileExists(newFilePath)) {
            minioClient.copyObject(
                    CopyObjectArgs
                            .builder()
                            .source(
                                    CopySource
                                            .builder()
                                            .bucket(bucketName)
                                            .object(oldFilePath)
                                            .build()
                            )
                            .bucket(bucketName)
                            .object(newFilePath)
                            .build());
            deleteFile(userId, path, oldName);
            return;
        }
        throw new Exception("File doesn't exist or new file name already exists");
    }

    public void deleteFile(int userId, String path, String fileName) throws Exception {
        String filePath = PathUtil.buildUserFilePath(userId, path, fileName);
        if (fileExists(filePath)) {
            minioClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());
            return;
        }
        throw new Exception("File doesn't exist");
    }

    private boolean fileExists(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void uploadFile(String path, MultipartFile file, int userId) throws Exception {
        String filePath = PathUtil.buildUserFilePath(userId, path, file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }
    }

//    public void listFiles(String prefix) throws Exception {
//        Iterable<Result<Item>> results = minioClient.listObjects(
//                    ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build());
//
//        for (Result<Item> result : results) {
//            Item item = result.get();
//            System.out.println(item.objectName());
//        }
//    }

//    public void listBuckets() throws Exception {
//        List<Bucket> buckets = minioClient.listBuckets();
//        for (Bucket bucket : buckets) {
//            System.out.println(bucket.name());
//        }
//    }


}
