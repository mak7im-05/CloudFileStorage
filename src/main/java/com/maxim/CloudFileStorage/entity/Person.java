package com.maxim.CloudFileStorage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "person")
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Имя не должно быть пустым")
    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 3, max = 64, message = "Имя должно быть больше 3 символов и меньше 64")
    @Column(name = "username", unique = true)
    private String username;

    @NotEmpty(message = "Пароль не должно быть пустым")
    @NotBlank(message = "Пароль не должно быть пустым")
    @Size(min = 3, max = 64, message = "Пароль должно быть больше 3 символов и меньше 64")
    @Column(name = "password")
    private String password;
}
