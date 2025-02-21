package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.security.PersonDetails;
import com.maxim.CloudFileStorage.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    @PostMapping("/delete")
    public String deleteFile(@RequestParam("pathToFile") String pathToFile,
                             @RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
                             @AuthenticationPrincipal PersonDetails personDetails,
                             RedirectAttributes redirectAttributes) {
        int userId = personDetails.getPerson().getId();
        try {
            fileService.deleteFile(userId, pathToFile);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Файл не был удален");
        }
        return "redirect:/main?currentDirectory=" + currentDirectory;
    }

    @ResponseBody
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("pathToFile") String pathToFile,
                                               @AuthenticationPrincipal PersonDetails personDetails) {
        int userId = personDetails.getPerson().getId();
        try (InputStream inputStream = fileService.downloadFile(userId, pathToFile)) {
            byte[] fileContent = inputStream.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(pathToFile, StandardCharsets.UTF_8));

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while downloading file", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
                             @RequestParam("newName") String newName,
                             @RequestParam("oldName") String oldName,
                             @AuthenticationPrincipal PersonDetails personDetails,
                             RedirectAttributes redirectAttributes) {
        int userId = personDetails.getPerson().getId();
        try {
            fileService.renameFile(userId, currentDirectory, oldName, newName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Файл не был переименован");
        }
        return "redirect:/main?currentDirectory=" + currentDirectory;
    }
}
