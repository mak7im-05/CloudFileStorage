package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.dto.MinioObjectDto;
import com.maxim.CloudFileStorage.security.PersonDetails;
import com.maxim.CloudFileStorage.service.SearchFilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchFilesService searchFilesService;

    @GetMapping("/search")
    public String search(@RequestParam("query") String query,
                         @AuthenticationPrincipal PersonDetails personDetails,
                         Model model
                         ) {
        int userId = personDetails.getPerson().getId();
        try {
            List<MinioObjectDto> files = searchFilesService.searchFile(query, userId);
            model.addAttribute("username", personDetails.getPerson().getUsername());
            model.addAttribute("files", files);
            return "files-search";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
