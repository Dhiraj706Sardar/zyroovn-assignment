package com.finance.backend.security;

import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.UserStatus;
import com.finance.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Validate token format and signature
                if (!jwtTokenProvider.validateToken(jwt)) {
                    log.debug("Invalid JWT token for request: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract claims from token
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                Role tokenRole = jwtTokenProvider.getRoleFromToken(jwt);
                UserStatus tokenStatus = jwtTokenProvider.getStatusFromToken(jwt);

                // Verify user still exists and is not deleted
                Optional<User> userOptional = userRepository.findByIdAndNotDeleted(userId);
                if (userOptional.isEmpty()) {
                    log.info("Authentication failed: User not found or deleted. UserId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                User user = userOptional.get();

                // Verify user is active
                if (user.getStatus() != UserStatus.ACTIVE) {
                    log.info("Authentication failed: User account is not active. Username: {}, Status: {}", 
                             username, user.getStatus());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Verify role in token matches current role in database
                if (user.getRole() != tokenRole) {
                    log.info("Authentication failed: User role has changed. Username: {}, Token role: {}, Current role: {}", 
                             username, tokenRole, user.getRole());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Create user principal with current database values
                UserPrincipal userPrincipal = new UserPrincipal(
                    user.getId(), 
                    user.getUsername(), 
                    user.getRole(),
                    user.getStatus()
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userPrincipal, 
                            null, 
                            userPrincipal.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("User authenticated successfully: {}", username);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context for request: {}", 
                     request.getRequestURI(), ex);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
