package com.maxim.CloudFileStorage.util;


import com.maxim.CloudFileStorage.dto.PersonDto;
import com.maxim.CloudFileStorage.entity.Person;
import com.maxim.CloudFileStorage.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PersonValidator implements Validator {

    private final AuthenticationService authenticationService;

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        PersonDto personDto = (PersonDto) target;

        if (!Objects.equals(personDto.password(), personDto.confirmPassword())) {
            errors.rejectValue("password", "", "Пароли не совпадают");
            return;
        }

        Optional<Person> person = authenticationService.findByUsername(personDto.username());
        person.ifPresent(a -> errors.rejectValue("username", "", "Пользователь с таким именем уже существует"));
    }
}
