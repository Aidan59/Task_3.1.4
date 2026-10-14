package ru.kata.spring.boot_security.demo.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.kata.spring.boot_security.demo.dto.RoleDto;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.dto.UserRequest;
import ru.kata.spring.boot_security.demo.enums.EmploymentType;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserDto> findAll() {
        return userService.findAll().stream().map(UserDto::from).toList();
    }

    @GetMapping("/users/{id}")
    public UserDto findOne(@PathVariable("id") long id) {
        return UserDto.from(findOrThrow(id));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = toEntity(new User(), request);
        checkEmailIsFree(user);
        userService.addUser(user);

        return UserDto.from(user);
    }

    @PutMapping("/users/{id}")
    public UserDto update(@PathVariable("id") long id, @Valid @RequestBody UserRequest request) {
        findOrThrow(id);

        User user = toEntity(new User(), request);
        user.setId(id);
        checkEmailIsFree(user);
        userService.updateUser(user);

        return UserDto.from(findOrThrow(id));
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") long id) {
        findOrThrow(id);

        User user = new User();
        user.setId(id);
        userService.deleteUser(user);
    }

    @GetMapping("/roles")
    public List<RoleDto> findAllRoles() {
        return userService.findAllRoles().stream()
                .map(role -> new RoleDto(role.getId(), role.getName()))
                .toList();
    }

    @GetMapping("/employment-types")
    public EmploymentType[] employmentTypes() {
        return EmploymentType.values();
    }

    private User findOrThrow(long id) {
        User probe = new User();
        probe.setId(id);

        User found = userService.getUser(probe);
        if (found == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return found;
    }

    private void checkEmailIsFree(User user) {
        if (userService.existsByEmail(user.getEmail(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already in use");
        }
    }

    private User toEntity(User user, UserRequest request) {
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
