package com.maxim.CloudFileStorage.service;

import com.maxim.CloudFileStorage.util.PathUtil;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final String bucketName = "user-files";
    private final MinioClient minioClient;

    public void createFolder(int userId, String path, String folderName) throws Exception {
        String folderPath = PathUtil.buildUserFolderPath(userId, path, folderName);
        if (!folderExists(folderPath)) {
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            minioClient.putObject(
                    PutObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(folderPath)
                            .stream(emptyContent, 0, -1)
                            .build());
            return;
        }
        throw new Exception("Folder is already exists");
    }

    public void renameFolder(int userId, String path, String oldName, String newName) throws Exception {
        String oldFolderPath = PathUtil.buildUserFolderPath(userId, path, oldName);
        String newFolderPath = PathUtil.buildUserFolderPath(userId, path, newName);

        if (folderExists(oldFolderPath) && !folderExists(newFolderPath)) {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs
                            .builder()
                            .bucket(bucketName)
                            .prefix(oldFolderPath)
                            .build());
            for (Result<Item> result : results) {
                Item item = result.get();
                String oldObjectPath = item.objectName();
                String newObjectPath = oldObjectPath.replace(oldFolderPath, newFolderPath);
                minioClient.copyObject(
                        CopyObjectArgs
                                .builder()
                                .source(
                                        CopySource
                                                .builder()
                                                .bucket(bucketName)
                                                .object(oldObjectPath)
                                                .build()
                                )
                                .bucket(bucketName)
                                .object(newObjectPath)
                                .build());
                minioClient.removeObject(
                        RemoveObjectArgs
                                .builder()
                                .bucket(bucketName)
                                .object(oldObjectPath)
                                .build());
            }
            return;
        }
        throw new Exception("Folder doesn't exist or new folder name already exists");
    }

    private boolean folderExists(String folderPath) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs
                        .builder()
                        .bucket(bucketName)
                        .prefix(folderPath)
                        .build());
        for (Result<Item> result : results) {
            return true;
        }
        return false;
    }
}
