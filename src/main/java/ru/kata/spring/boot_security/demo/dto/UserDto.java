package ru.kata.spring.boot_security.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import ru.kata.spring.boot_security.demo.enums.EmploymentType;

public record UserDto(Long id, String name, String surname, int age, String email,
                      BigDecimal salary, String position, EmploymentType employmentType,
                      List<RoleDto> roles) { }
