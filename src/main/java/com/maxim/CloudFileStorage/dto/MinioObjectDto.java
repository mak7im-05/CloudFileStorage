package com.maxim.CloudFileStorage.dto;

public record MinioObjectDto(
        String name,
        String path,
        Boolean isDir
) {
}
