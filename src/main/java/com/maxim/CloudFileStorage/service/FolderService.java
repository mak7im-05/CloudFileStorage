package com.maxim.CloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.maxim.CloudFileStorage.util.PathUtil.buildFilePath;

@Service
@RequiredArgsConstructor
public class FolderService {

    @Value("${minio.bucketName}")
    private String bucketName;

    private final MinioClient minioClient;

    public void createFolder(String path, String folderName, int userId) throws Exception {
        String folderPath = buildFilePath(userId, path, folderName);
        if (!folderExists(folderPath)) {
            createEmptyFolder(folderPath + "/");
        } else {
            throw new Exception("Папка с таким именем уже существует");
        }
    }

    public void renameFolder(int userId, String path, String oldName, String newName) throws Exception {
        String oldFolderPath = buildFilePath(userId, path, oldName);
        String newFolderPath = buildFilePath(userId, path, newName);

        if (folderExists(oldFolderPath) && !folderExists(newFolderPath)) {
            renameFolderContents(oldFolderPath, newFolderPath);
        } else {
            throw new Exception("Папка с таким именем уже существует");
        }
    }

    private boolean folderExists(String folderPath) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(folderPath).build());
        for (Result<Item> result : results) {
            return true;
        }
        return false;
    }

    public void deleteFolder(String path, int userId) throws Exception {
        String folderPath = buildFilePath(userId, path);
        deleteFolderContents(folderPath);
    }

    private void createEmptyFolder(String folderPath) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        minioClient.putObject(
                PutObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .object(folderPath)
                        .stream(emptyContent, 0, -1)
                        .build());
    }

    private void renameFolderContents(String oldFolderPath, String newFolderPath) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(oldFolderPath).recursive(true).build());
        for (Result<Item> result : results) {
            Item item = result.get();
            String oldObjectPath = item.objectName();
            String newObjectPath = oldObjectPath.replace(oldFolderPath, newFolderPath);
            copyObject(oldObjectPath, newObjectPath);
            deleteObject(oldObjectPath);
        }
    }

    private void copyObject(String oldObjectPath, String newObjectPath) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .source(CopySource.builder().bucket(bucketName).object(oldObjectPath).build())
                        .bucket(bucketName)
                        .object(newObjectPath)
                        .build());
    }

    private void deleteObject(String objectPath) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectPath)
                        .build());
    }

    private void deleteFolderContents(String folderPath) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(folderPath).recursive(true).build());
        for (Result<Item> result : results) {
            Item item = result.get();
            deleteObject(item.objectName());
        }
    }
}
