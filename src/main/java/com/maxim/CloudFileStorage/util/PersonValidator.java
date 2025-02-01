package com.maxim.CloudFileStorage.util;


import com.maxim.CloudFileStorage.dto.PersonDto;
import com.maxim.CloudFileStorage.entity.Person;
import com.maxim.CloudFileStorage.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PersonValidator implements Validator {

    private final PersonService personService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonDto personDto = (PersonDto) target;

        if(!Objects.equals(personDto.password(), personDto.confirmPassword())) {
            errors.rejectValue("password", "", "Пароли не совпадают");
            return;
        }

        Optional<Person> person = personService.findByUsername(personDto.username());
        person.ifPresent(a -> errors.rejectValue("username", "", "Пользователь с таким именем уже существует"));
    }
}
