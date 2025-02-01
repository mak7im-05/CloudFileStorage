package com.maxim.CloudFileStorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record PersonDto(
        @NotEmpty(message = "Имя не должно быть пустым")
        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 3, max = 64, message = "Имя должно быть больше 3 символов и меньше 64")
        String username,
        @NotEmpty(message = "Пароль не должно быть пустым")
        @NotBlank(message = "Пароль не должно быть пустым")
        @Size(min = 3, max = 64, message = "Пароль должно быть больше 3 символов и меньше 64")
        String password,
        @NotEmpty(message = "Пароль не должно быть пустым")
        @NotBlank(message = "Пароль не должно быть пустым")
        @Size(min = 3, max = 64, message = "Пароль должно быть больше 3 символов и меньше 64")
        String confirmPassword
) {
}
