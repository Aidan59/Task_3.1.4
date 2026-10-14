package ru.kata.spring.boot_security.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import ru.kata.spring.boot_security.demo.enums.EmploymentType;
import ru.kata.spring.boot_security.demo.model.User;

public record UserDto(Long id, String name, String surname, int age, String email,
                      BigDecimal salary, String position, EmploymentType employmentType,
                      List<RoleDto> roles) {

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getName(), u.getSurname(), u.getAge(), u.getEmail(),
                u.getSalary(), u.getPosition(), u.getEmploymentType(),
                u.getRoles().stream().map(r -> new RoleDto(r.getId(), r.getName())).toList());
    }
}
