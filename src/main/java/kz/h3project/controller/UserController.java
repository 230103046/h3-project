package kz.h3project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.h3project.model.user.dto.CreateUserRequest;
import kz.h3project.model.user.dto.UserResponseDto;
import kz.h3project.model.user.entity.Role;
import kz.h3project.model.user.entity.User;
import kz.h3project.model.user.enums.RoleDic;
import kz.h3project.model.user.data.TokenPrincipal;
import kz.h3project.repository.RoleRepository;
import kz.h3project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Users API", description = "Управление пользователями (матрица: view own / view all / CRUD)")
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Свой профиль", description = "View own profile — любой авторизованный")
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal TokenPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(toResponseDto(user));
    }

    @Operation(summary = "Список всех пользователей", description = "View all users — только ADMIN")
    @PreAuthorize("@authorization.hasAccessToReadAllUsers()")
    @GetMapping("/list")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> dtos = users.stream()
                .map(this::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Пользователь по username", description = "View own — если свой username; иначе View all (ADMIN)")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDto> getUser(
            @AuthenticationPrincipal TokenPrincipal principal,
            @RequestParam String username) {
        if (!principal.getUsername().equals(username) && !principal.getPermissions().contains("READ_ALL_USERS")) {
            return ResponseEntity.status(403).build();
        }
        return userRepository.findByUsername(username)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Создать пользователя", description = "Create user — только ADMIN")
    @PreAuthorize("@authorization.hasAccessToWriteUser()")
    @PostMapping
    @Transactional
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        Role userRole = roleRepository.findByName(RoleDic.USER.name())
                .orElseThrow(() -> new IllegalStateException("USER role not found"));
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(request.isEnabled())
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);
        return ResponseEntity.ok(toResponseDto(user));
    }

    @Operation(summary = "Обновить пользователя", description = "Update user — только ADMIN")
    @PreAuthorize("@authorization.hasAccessToWriteUser()")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(request.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(toResponseDto(user));
    }

    @Operation(summary = "Удалить пользователя", description = "Delete user — только ADMIN")
    @PreAuthorize("@authorization.hasAccessToDeleteUser()")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponseDto toResponseDto(User u) {
        return UserResponseDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
