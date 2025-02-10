package com.maxim.CloudFileStorage.Integration.service;

import com.maxim.CloudFileStorage.Integration.IntegrationTestBase;
import com.maxim.CloudFileStorage.entity.Person;
import com.maxim.CloudFileStorage.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RequiredArgsConstructor
public class PersonServiceIT extends IntegrationTestBase {

    private final AuthenticationService authenticationService;

    @Test
    void shouldCreateNewUserRecord_WhenRegisterUserIsCalled() {
        Person person = new Person();
        person.setUsername("test1");
        person.setPassword("password");

        authenticationService.register(person);
        Optional<Person> personOptional = authenticationService.findByUsername("test1");

        personOptional.ifPresent(actualPerson -> assertEquals(person.getUsername(), actualPerson.getUsername()));
    }

    @Test
    void shouldReturnEmptyOptional_WhenFindByUsernameIsCalled() {
        Optional<Person> personOptional = authenticationService.findByUsername("test2");

        assertEquals(Optional.empty(), personOptional);
    }

    @Test
    void shouldThrowException_WhenRegisterUserWithNonUniqueUsername() {
        Person person = new Person();
        person.setUsername("test4");
        person.setPassword("password");

        Person person2 = new Person();
        person.setUsername("test4");
        person.setPassword("password");

        authenticationService.register(person);

        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(person2));
    }
}