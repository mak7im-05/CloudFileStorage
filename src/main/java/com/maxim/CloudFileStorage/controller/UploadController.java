package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.security.PersonDetails;
import com.maxim.CloudFileStorage.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/upload")
public class UploadController {

    private final FileService fileService;

    @PostMapping("/files")
    public String uploadFolder(@RequestParam("files") MultipartFile[] files,
                               @RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
                               @AuthenticationPrincipal PersonDetails personDetails,
                               RedirectAttributes redirectAttributes) {
        int userId = personDetails.getPerson().getId();
        Arrays.stream(files).forEach(file -> {
            try {
                fileService.uploadFile(currentDirectory, file, userId);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Файл не был загружен, возможно он уже существует");
            }
        });
        return "redirect:/main?currentDirectory=" + currentDirectory;
    }
}
