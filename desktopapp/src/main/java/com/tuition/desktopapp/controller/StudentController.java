package com.tuition.desktopapp.controller;

import com.tuition.desktopapp.dto.ApiDtos;
import com.tuition.desktopapp.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.StudentRegistrationResponse registerStudent(@Valid @RequestBody ApiDtos.StudentRegistrationRequest request) {
        return studentService.registerStudent(request);
    }
}
