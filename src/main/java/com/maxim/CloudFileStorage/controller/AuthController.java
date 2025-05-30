package com.maxim.CloudFileStorage.controller;

import com.maxim.CloudFileStorage.dto.PersonDto;
import com.maxim.CloudFileStorage.entity.Person;
import com.maxim.CloudFileStorage.mapper.PersonMapper;
import com.maxim.CloudFileStorage.service.AuthenticationService;
import com.maxim.CloudFileStorage.util.PersonValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final PersonValidator personValidator;
    private final PersonMapper personMapper;
    private final AuthenticationService authenticationService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String showRegistrationPage(@ModelAttribute("person") PersonDto personDto) {
        return "registration";
    }

    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute("person") @Valid PersonDto personDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        personValidator.validate(personDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        Person person = personMapper.toEntity(personDto);
        authenticationService.register(person);
        redirectAttributes.addFlashAttribute("successMessage", "Регистрирование успешно. Пожалуйста войдите в аккаунт");
        return "redirect:/auth/login";
    }
}
