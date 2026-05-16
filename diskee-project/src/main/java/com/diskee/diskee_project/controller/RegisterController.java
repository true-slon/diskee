package com.diskee.diskee_project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Аутентификация/Регистрация")
@RestController
@RequestMapping(path = "/app/v4/auth/register", produces = "application/hal+json")
@RequiredArgsConstructor
public class RegisterController {
    
}
