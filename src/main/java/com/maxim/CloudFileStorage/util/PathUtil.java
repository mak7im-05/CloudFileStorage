package com.maxim.CloudFileStorage.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PathUtil {

    public static String buildUserFolderPath(int userId, String path, String folderName) {
        return String.format("user-%s-files/%s%s/", userId, path, folderName);
    }

    public static String buildUserFilePath(int userId, String path, String folderName) {
        return String.format("user-%s-files/%s%s", userId, path, folderName);
    }
}
