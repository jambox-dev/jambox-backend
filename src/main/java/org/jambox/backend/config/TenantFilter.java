package org.jambox.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Tenant;
import org.jambox.backend.repository.TenantRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantId = request.getHeader("X-Tenant-ID");
        String serverName = request.getServerName();

        if (tenantId == null) {
            String tenantSubdomain = request.getHeader("X-Tenant-Subdomain");
            if (tenantSubdomain != null && !tenantSubdomain.isEmpty()) {
                Tenant tenant = tenantRepository.findBySubdomain(tenantSubdomain).orElse(null);
                if (tenant != null) {
                    tenantId = tenant.getId();
                }
            }
        }

        if (tenantId == null && serverName != null) {
            // Check for subdomain
            if (serverName.endsWith(".jambox.dev")) {
                String subdomain = serverName.replace(".jambox.dev", "");
                // Ignore "www" or empty
                if (!subdomain.isEmpty() && !subdomain.equals("www") && !subdomain.equals("jambox.dev")) {
                     Tenant tenant = tenantRepository.findBySubdomain(subdomain).orElse(null);
                     if (tenant != null) {
                         tenantId = tenant.getId();
                     }
                }
            }
        }

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
