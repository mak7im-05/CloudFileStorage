package com.maxim.CloudFileStorage.service;

import com.maxim.CloudFileStorage.dto.MinioObjectDto;
import com.maxim.CloudFileStorage.util.PathUtil;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.maxim.CloudFileStorage.util.PathUtil.buildFilePath;

@Service
@RequiredArgsConstructor
public class SearchFilesService {

    @Value("${minio.bucketName}")
    private String bucketName;

    private final MinioClient minioClient;

    public List<MinioObjectDto> searchFile(String filesName, int userId) throws Exception{
        String userRootPrefix = buildFilePath(userId);

        List<MinioObjectDto> minioObjectList = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(userRootPrefix).recursive(true).build());
        for(Result<Item> res: results) {
            Item item = res.get();
            String objectName = item.objectName();
            String[] partsObjectName = objectName.split("/");
            String lastPart = partsObjectName[partsObjectName.length - 1];
            if(lastPart.equals(filesName)) {
                boolean isDir = objectName.endsWith("/");
                String objectPath = objectName.replaceFirst("user-\\d+-files/", "");
                String fileName = String.valueOf(Paths.get(item.objectName()).getFileName());
                minioObjectList.add(new MinioObjectDto(fileName, objectPath, isDir));
            }
        }
        return minioObjectList;
    }

}
