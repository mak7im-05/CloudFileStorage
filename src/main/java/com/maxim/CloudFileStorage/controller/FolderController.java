package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.security.PersonDetails;
import com.maxim.CloudFileStorage.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/folder")
public class FolderController {

    private final FolderService folderService;

    @PostMapping("/delete")
    public String deleteFolder(@RequestParam("pathToFile") String pathToFile,
                               @RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
                               @AuthenticationPrincipal PersonDetails personDetails,
                               RedirectAttributes redirectAttributes) {
        int userId = personDetails.getPerson().getId();
        try {
            folderService.deleteFolder(pathToFile, userId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Папка не была удалена");
        }
        return "redirect:/main?currentDirectory=" + currentDirectory;
    }

    @PostMapping("/rename")
    public String renameFolder(@RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
                                @RequestParam("newName") String newName,
                                @RequestParam("oldName") String oldName,
                                @AuthenticationPrincipal PersonDetails personDetails,
                                RedirectAttributes redirectAttributes) {
        int userId = personDetails.getPerson().getId();
        try {
            folderService.renameFolder(userId, currentDirectory, oldName, newName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Папка не была переименована");
        }
        return "redirect:/main?currentDirectory=" + currentDirectory;
    }

    @PostMapping("/create")
    public String createNewFolder(@RequestParam(name = "currentDirectory", defaultValue = "") String currentDirectory,
                                  @RequestParam("folderName") String folderName,
                                  @AuthenticationPrincipal PersonDetails personDetails,
                                  RedirectAttributes redirectAttributes) {
        int userId = personDetails.getPerson().getId();
        if (folderName.isBlank()) {
            folderName = "Новая папка";
        }
        try {
            folderService.createFolder(currentDirectory, folderName, userId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Папка не была создана");
        }
        return "redirect:/main?currentDirectory=" + currentDirectory;

    }
}
