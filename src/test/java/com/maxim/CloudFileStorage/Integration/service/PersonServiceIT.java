package com.maxim.CloudFileStorage.Integration.service;

import com.maxim.CloudFileStorage.Integration.IntegrationTestBase;
import com.maxim.CloudFileStorage.entity.Person;
import com.maxim.CloudFileStorage.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@RequiredArgsConstructor
public class PersonServiceIT extends IntegrationTestBase {

    private final PersonService personService;

    @Test
    void shouldCreateNewUserRecord_WhenRegisterUserIsCalled() {
        Person person = new Person();
        person.setUsername("test1");
        person.setPassword("password");

        personService.register(person);
        Optional<Person> personOptional = personService.findByUsername("test1");

        personOptional.ifPresent(actualPerson -> assertEquals(person.getUsername(), actualPerson.getUsername()));
    }

    @Test
    void shouldReturnEmptyOptional_WhenFindByUsernameIsCalled() {
        Optional<Person> personOptional = personService.findByUsername("test2");

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

        personService.register(person);

        assertThrows(IllegalArgumentException.class, () -> personService.register(person2));
    }
}