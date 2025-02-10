package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.service.FileService;
import com.maxim.CloudFileStorage.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class mainController {

    private final FileService fileService;
    private final FolderService folderService;

    @GetMapping("/hello")
    public String hello() throws Exception {
        folderService.createFolder(1, "", "folder1");
        folderService.createFolder(1, "folder1/", "folder2");
        folderService.renameFolder(1, "", "folder1", "folder3");
        return "hello";
    }
}
