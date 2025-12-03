package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.mapper.QueueMapper;
import org.jambox.backend.model.QueueResponseModel;
import org.jambox.backend.model.SpotifyToken;
import org.jambox.backend.model.entity.Tenant;
import org.jambox.backend.model.entity.User;
import org.jambox.backend.repository.TenantRepository;
import org.jambox.backend.repository.UserRepository;
import org.jambox.backend.service.SpotifyAuthService;
import org.jambox.backend.service.SpotifyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyService spotifyService;
    private final QueueMapper queueMapper;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code, @RequestParam(name = "state") String tenantId) {
        SpotifyToken token = spotifyAuthService.getAccessToken(code, tenantId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        tenant.setSpotifyAccessToken(token.getAccessToken());
        tenant.setSpotifyRefreshToken(token.getRefreshToken());
        tenant.setSpotifyTokenExpiresAt(LocalDateTime.now().plusSeconds(3600)); // Default 1h
        
        tenantRepository.save(tenant);
        
        return "success";
    }

    @GetMapping("/login")
    public ResponseEntity<Object> login(@RequestParam(required = false) String tenantId) throws URISyntaxException {
        if (tenantId == null) {
            // Try to get from current user
            Tenant currentTenant = getCurrentTenant();
            if (currentTenant != null) {
                tenantId = currentTenant.getId();
            } else {
                return new ResponseEntity<>("Tenant ID required", HttpStatus.BAD_REQUEST);
            }
        }
        
        URI spotify = new URI(spotifyAuthService.redirectToAuthCodeFlow(clientId, tenantId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(spotify);
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }

    @GetMapping("/loggedin")
    public ResponseEntity<Object> loggedIn() {
        Tenant tenant = getCurrentTenant();
        if (tenant == null || tenant.getSpotifyRefreshToken() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/queue")
    public Object getQueue(){
        Tenant tenant = getCurrentTenant();
        if (tenant == null) {
             return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        QueueResponseModel queue = spotifyService.getUserQueue(tenant).block();
        if (queue == null) {
            return new org.jambox.backend.model.entity.Song[0];
        }
        return queueMapper.toSongList(queue).toArray(org.jambox.backend.model.entity.Song[]::new);
    }

    private Tenant getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getTenantId() != null) {
                return tenantRepository.findById(user.getTenantId()).orElse(null);
            }
        }
        return null;
    }
}