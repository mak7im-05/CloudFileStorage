package com.maxim.CloudFileStorage.service;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.maxim.CloudFileStorage.util.PathUtil.buildFilePath;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final MinioService minioService;

    public void createFolder(String path, String folderName, int userId) throws Exception {
        String folderPath = buildFilePath(userId, path, folderName);
        if (!minioService.folderExists(folderPath)) {
            minioService.createEmptyFolder(folderPath + "/");
        } else {
            throw new Exception("Папка с таким именем уже существует");
        }
    }

    public void renameFolder(int userId, String path, String oldName, String newName) throws Exception {
        String oldFolderPath = buildFilePath(userId, path, oldName);
        String newFolderPath = buildFilePath(userId, path, newName);

        if (minioService.folderExists(oldFolderPath) && !minioService.folderExists(newFolderPath)) {
            renameFolderContents(oldFolderPath, newFolderPath);
        } else {
            throw new Exception("Папка с таким именем уже существует");
        }
    }

    public void deleteFolder(String path, int userId) throws Exception {
        String folderPath = buildFilePath(userId, path);
        deleteFolderContents(folderPath);
    }

    private void renameFolderContents(String oldFolderPath, String newFolderPath) throws Exception {
        Iterable<Result<Item>> results = minioService.listObjectsRecursively(oldFolderPath);
        for (Result<Item> result : results) {
            Item item = result.get();
            String oldObjectPath = item.objectName();
            String newObjectPath = oldObjectPath.replace(oldFolderPath, newFolderPath);
            minioService.copyFile(oldObjectPath, newObjectPath);
            minioService.deleteFile(oldObjectPath);
        }
    }

    private void deleteFolderContents(String folderPath) throws Exception {
        Iterable<Result<Item>> results = minioService.listObjectsRecursively(folderPath);
        for (Result<Item> result : results) {
            minioService.deleteFile(result.get().objectName());
        }
    }
}
