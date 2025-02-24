package com.maxim.CloudFileStorage.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucketName}")
    private String bucketName;

    private final MinioClient minioClient;

    public boolean fileExists(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean folderExists(String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(path).build());
        for (Result<Item> result : results) {
            return true;
        }
        return false;
    }

    public void copyFile(String oldFilePath, String newFilePath) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs
                        .builder()
                        .source(CopySource.builder().bucket(bucketName).object(oldFilePath).build())
                        .bucket(bucketName)
                        .object(newFilePath)
                        .build());
    }

    public void deleteFile(String path) throws Exception {
        if (fileExists(path)) {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
        } else {
            throw new Exception("Файл не существует");
        }
    }

    public void putObject(MultipartFile file, String path) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    public void createEmptyFolder(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания папки", e);
        }
    }

    public Iterable<Result<Item>> listObjects(String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(path).build());
    }

    public Iterable<Result<Item>> listObjectsRecursively(String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(path).recursive(true).build());
    }

    public InputStream getObject(String path) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }
}
