package com.BEProjects.url_shortener.web.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserForm (
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid Email format")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Full name is required")
        String name){
}
