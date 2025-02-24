package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.dto.BreadcrumbsDto;
import com.maxim.CloudFileStorage.dto.MinioObjectDto;
import com.maxim.CloudFileStorage.security.PersonDetails;
import com.maxim.CloudFileStorage.service.FileService;
import com.maxim.CloudFileStorage.service.FolderService;
import com.maxim.CloudFileStorage.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final FileService fileService;
    private final FolderService folderService;

    @GetMapping("/main")
    public String showMainPage(
            @RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
            @AuthenticationPrincipal PersonDetails personDetails,
            Model model) throws Exception {
        int userId = personDetails.getPerson().getId();


        if (!folderService.isFolderExists(currentDirectory, userId)) {
            model.addAttribute("errorMessage", "Директория не существует");
        }

        List<BreadcrumbsDto> breadcrumbs = PathUtil.getBreadcrumbs(currentDirectory);
        List<MinioObjectDto> files = fileService.listFiles(currentDirectory, userId);
        model.addAttribute("currentPath", currentDirectory);
        model.addAttribute("username", personDetails.getPerson().getUsername());
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("files", files);
        return "main";
    }
}
