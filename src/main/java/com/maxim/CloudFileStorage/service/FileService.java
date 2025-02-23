package com.maxim.CloudFileStorage.service;

import com.maxim.CloudFileStorage.dto.MinioObjectDto;
import com.maxim.CloudFileStorage.util.PathUtil;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.maxim.CloudFileStorage.util.PathUtil.buildFilePath;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${minio.bucketName}")
    private String bucketName;
    private final MinioClient minioClient;

    public void renameFile(int userId, String currentDirectory, String oldName, String newName) throws Exception {
        String extension = PathUtil.getExtension(oldName);
        String oldFilePath = buildFilePath(userId, currentDirectory, oldName);
        String newFilePath = buildFilePath(userId, currentDirectory, newName + extension);

        if (fileExists(oldFilePath) && !fileExists(newFilePath)) {
            minioClient.copyObject(
                    CopyObjectArgs
                            .builder()
                            .source(CopySource.builder().bucket(bucketName).object(oldFilePath).build())
                            .bucket(bucketName)
                            .object(newFilePath)
                            .build());
            deleteFile(userId, currentDirectory + oldName);
        } else {
            throw new Exception("Файл с таким именем уже существует");
        }
    }

    public void deleteFile(int userId, String path) throws Exception {
        String filePath = buildFilePath(userId, path);
        if (fileExists(filePath)) {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());
        } else {
            throw new Exception("Файл не существует");
        }
    }

    public void uploadFile(String path, MultipartFile file, int userId) {
        String filePath = buildFilePath(userId, path, file.getOriginalFilename());
        if (fileExists(filePath)) {
            throw new IllegalArgumentException("Файл с таким именем уже существует");
        }
        createDirectories(filePath);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    private boolean fileExists(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<MinioObjectDto> listFiles(String path, int userId) throws Exception {
        String fullPathToDirectory = buildFilePath(userId, path);
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(fullPathToDirectory).build());

        List<MinioObjectDto> minioObjectList = new ArrayList<>();
        for (Result<Item> result : results) {
            Item item = result.get();
            if (isCurrentDirectory(item, fullPathToDirectory)) {
                continue;
            }
            boolean isDir = item.objectName().endsWith("/");
            String objectPath = item.objectName().replaceFirst("user-\\d+-files/", "");
            String objectName = item.objectName().replaceFirst(fullPathToDirectory, "");
            if (isDir) {
                objectName = objectName.substring(0, objectName.length() - 1);
            }

            minioObjectList.add(new MinioObjectDto(objectName, objectPath, isDir));
        }
        return minioObjectList;
    }

    public InputStream downloadFile(int userId, String objectName) throws Exception {
        String fullPathToFile = buildFilePath(userId, objectName);
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fullPathToFile)
                .build());
    }

    private static boolean isCurrentDirectory(Item item, String fullPathToDirectory) {
        return item.objectName().equals(fullPathToDirectory);
    }

    private void createDirectories(String filePath) {
        PathUtil.getPathSegments(filePath).forEach(segment -> {
            if (!fileExists(segment)) {
                try {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(segment)
                                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                                    .build());
                } catch (Exception e) {
                    throw new RuntimeException("Error creating directory", e);
                }
            }
        });
    }
}
