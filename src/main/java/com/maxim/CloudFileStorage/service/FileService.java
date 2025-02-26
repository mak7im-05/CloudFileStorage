package com.maxim.CloudFileStorage.service;

import com.maxim.CloudFileStorage.dto.MinioObjectDto;
import com.maxim.CloudFileStorage.util.PathUtil;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.maxim.CloudFileStorage.util.PathUtil.buildFilePath;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;

    public void renameFile(int userId, String currentDirectory, String oldName, String newName) throws Exception {
        String extension = PathUtil.getExtension(oldName);
        String oldFilePath = buildFilePath(userId, currentDirectory, oldName);
        String newFilePath = buildFilePath(userId, currentDirectory, newName + extension);

        if (minioService.fileExists(oldFilePath) && !minioService.fileExists(newFilePath)) {
            minioService.copyFile(oldFilePath, newFilePath);
            minioService.deleteFile(oldFilePath);
        } else {
            throw new Exception("Файл не был переименован");
        }
    }

    public void uploadFile(String path, MultipartFile file, int userId) {

        if(!minioService.folderExists(path)) {
            throw new IllegalArgumentException("Папка в которой хотите создать файл, не существует");
        }

        String filePath = buildFilePath(userId, path, file.getOriginalFilename());
        if (minioService.fileExists(filePath)) {
            throw new IllegalArgumentException("Файл с таким именем уже существует");
        }
        try {
            minioService.putObject(file, filePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        createDirectories(filePath);
    }

    public List<MinioObjectDto> listFiles(String path, int userId) throws Exception {
        String fullPathToDirectory = buildFilePath(userId, path);
        Iterable<Result<Item>> results = minioService.listObjects(fullPathToDirectory);
        return getMinioObjectList(results, fullPathToDirectory);
    }

    private List<MinioObjectDto> getMinioObjectList(Iterable<Result<Item>> results, String fullPathToDirectory) throws Exception {
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

    private boolean isCurrentDirectory(Item item, String fullPathToDirectory) {
        return item.objectName().equals(fullPathToDirectory);
    }

    public InputStream downloadFile(int userId, String objectName) throws Exception {
        String path = buildFilePath(userId, objectName);
        return minioService.getObject(path);
    }

    private void createDirectories(String filePath) {
        PathUtil.getPathSegments(filePath).forEach(file -> {
            if (!minioService.fileExists(file)) {
                minioService.createEmptyFolder(file);
            }
        });
    }

    public void deleteFile(int userId, String pathToFile) {
        String filePath = buildFilePath(userId, pathToFile);
        try {
            minioService.deleteFile(filePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
