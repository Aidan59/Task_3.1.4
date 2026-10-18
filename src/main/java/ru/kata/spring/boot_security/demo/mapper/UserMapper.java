package ru.kata.spring.boot_security.demo.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.dto.RoleDto;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.dto.UserRequest;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

@Component
public class UserMapper {

    private final UserService userService;

    public UserMapper(UserService userService) {
        this.userService = userService;
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getAge(),
                user.getEmail(),
                user.getSalary(),
                user.getPosition(),
                user.getEmploymentType(),
                toRoleDtos(user.getRoles().stream().toList()));
    }

    public List<UserDto> toDtos(List<User> users) {
        return users.stream().map(this::toDto).toList();
    }

    public RoleDto toRoleDto(Role role) {
        return new RoleDto(role.getId(), role.getName());
    }

    public List<RoleDto> toRoleDtos(List<Role> roles) {
        return roles.stream().map(this::toRoleDto).toList();
    }

    public User toEntity(UserRequest request) {
        return toEntity(request, 0L);
    }

    public User toEntity(UserRequest request, long id) {
        User user = new User();
        user.setId(id);
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setAge(request.age());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setSalary(request.salary());
        user.setPosition(request.position());
        user.setEmploymentType(request.employmentType());
        user.setRoles(userService.findRolesByIds(
                request.roleIds() == null ? null : List.copyOf(request.roleIds())));
        return user;
    }
}
