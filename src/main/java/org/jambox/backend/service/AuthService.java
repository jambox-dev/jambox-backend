package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.dto.AuthResponse;
import org.jambox.backend.model.dto.LoginRequest;
import org.jambox.backend.model.dto.RegisterRequest;
import org.jambox.backend.model.entity.Tenant;
import org.jambox.backend.model.entity.User;
import org.jambox.backend.model.enums.AuthProvider;
import org.jambox.backend.model.enums.Role;
import org.jambox.backend.model.enums.SubscriptionStatus;
import org.jambox.backend.model.enums.SubscriptionTier;
import org.jambox.backend.repository.TenantRepository;
import org.jambox.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new RuntimeException("Subdomain already taken");
        }

        // Create Tenant
        Tenant tenant = Tenant.builder()
                .name(request.getTenantName())
                .subdomain(request.getSubdomain())
                .createdAt(LocalDateTime.now())
                .build();
        tenant = tenantRepository.save(tenant);

        // Create User
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .roles(Set.of(Role.ADMIN)) // First user is Admin of the tenant
                .tenantId(tenant.getId())
                .subscriptionTier(SubscriptionTier.FREE)
                .subscriptionStatus(SubscriptionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Link owner to tenant
        tenant.setOwnerId(user.getId());
        // We need to save user first to get ID, then update tenant? 
        // Or just save user, then update tenant.
        user = userRepository.save(user);
        
        tenant.setOwnerId(user.getId());
        tenantRepository.save(tenant);

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .user(user)
                .build();
    }
}
