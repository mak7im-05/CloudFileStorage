package com.maxim.CloudFileStorage.util;

import com.maxim.CloudFileStorage.dto.BreadcrumbsDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PathUtil {
    public static String buildFilePath(int userId, String... parts) {
        StringBuilder filePath = new StringBuilder(String.format("user-%s-files/", userId));
        for (String part : parts) {
            filePath.append(part);
        }
        return filePath.toString();
    }

    public static List<String> getPathSegments(String path) {
        List<String> segments = new ArrayList<>();
        String[] parts = path.split("/");
        StringBuilder basePath = new StringBuilder(parts[0] + "/");

        for (int i = 1; i < parts.length - 1; i++) {
            basePath.append(parts[i]).append("/");
            segments.add(basePath.toString());
        }

        return segments;
    }

    public static String getExtension(String filename) {
        int lastDotPosition = filename.lastIndexOf('.');

        if (lastDotPosition == -1) {
            return "";
        }

        return filename.substring(lastDotPosition + 1);
    }

    public static List<BreadcrumbsDto> getBreadcrumbs(String path) {
        List<BreadcrumbsDto> breadcrumbs = new ArrayList<>();

        if (path == null || path.isEmpty()) {
            return breadcrumbs;
        }

        breadcrumbs.add(new BreadcrumbsDto("root", ""));

        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) {
                currentPath.append(parts[i]).append("/");
                breadcrumbs.add(new BreadcrumbsDto(parts[i], currentPath.toString()));
            }
        }

        return breadcrumbs;
    }
}
