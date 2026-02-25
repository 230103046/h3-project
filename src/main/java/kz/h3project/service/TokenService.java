package kz.h3project.service;

import jakarta.transaction.Transactional;
import kz.h3project.model.user.data.TokenPrincipal;
import kz.h3project.model.user.entity.OpaqueToken;
import kz.h3project.model.user.entity.Permission;
import kz.h3project.model.user.entity.User;
import kz.h3project.repository.OpaqueTokenRepository;
import kz.h3project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final OpaqueTokenRepository tokenRepo;
    private final UserRepository userRepo;

    @Transactional
    public TokenPrincipal validateToken(String tokenValue) {
        OpaqueToken token = tokenRepo.findByToken(tokenValue)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadCredentialsException("Token expired");

        User user = userRepo.findById(token.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName().name())
                .collect(Collectors.toSet());

        return new TokenPrincipal(user.getUsername(), user.getId(), permissions);
    }

    public String createToken(User user) {
        String token = UUID.randomUUID().toString();
        OpaqueToken t = new OpaqueToken();
        t.setToken(token);
        t.setUserId(user.getId());
        t.setExpiresAt(LocalDateTime.now().plusDays(7));
        tokenRepo.save(t);
        return token;
    }
}
