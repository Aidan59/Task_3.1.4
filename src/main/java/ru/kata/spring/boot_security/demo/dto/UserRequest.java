package ru.kata.spring.boot_security.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Set;
import ru.kata.spring.boot_security.demo.enums.EmploymentType;

public record UserRequest(@NotBlank String name, String surname,
                          @Min(value = 18, message = "Age must be at least 18") int age,
                          @NotBlank @Email String email, String password,
                          BigDecimal salary, String position,
                          EmploymentType employmentType, Set<Long> roleIds) {}
