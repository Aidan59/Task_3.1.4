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
import ru.kata.spring.boot_security.demo.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AdminRestController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public List<UserDto> findAll() {
        return userMapper.toDtos(userService.findAll());
    }

    @GetMapping("/users/{id}")
    public UserDto findOne(@PathVariable("id") long id) {
        return userMapper.toDto(findOrThrow(id));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = userMapper.toEntity(request);
        checkEmailIsFree(user);
        userService.addUser(user);

        return userMapper.toDto(user);
    }

    @PutMapping("/users/{id}")
    public UserDto update(@PathVariable("id") long id, @Valid @RequestBody UserRequest request) {
        findOrThrow(id);

        User user = userMapper.toEntity(request, id);
        user.setId(id);
        checkEmailIsFree(user);
        userService.updateUser(user);

        return userMapper.toDto(findOrThrow(id));
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
        return userMapper.toRoleDtos(userService.findAllRoles());
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

}
