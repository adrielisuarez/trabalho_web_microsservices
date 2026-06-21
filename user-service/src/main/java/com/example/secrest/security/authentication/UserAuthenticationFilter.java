package com.example.secrest.security.authentication;

import com.example.secrest.entity.User;
import com.example.secrest.repository.UserRepository;
import com.example.secrest.security.service.JwtTokenService;
import com.example.secrest.security.service.UserDetailsImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        System.out.println("[AUTH FILTER] " + method + " " + uri + " Authorization=" + authHeader);

        // ENDPOINTS PÚBLICOS
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = recoverToken(request);

        if (token != null) {
            System.out.println("[AUTH FILTER] token extracted=" + token);
            String email = jwtTokenService.getSubjectFromToken(token);
            System.out.println("[AUTH FILTER] email from token=" + email);
            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("Usuário não encontrado"));

            UserDetailsImpl userDetails =
                    new UserDetailsImpl(user);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            System.out.println("[AUTH FILTER] authentication set: " + authentication.isAuthenticated() + " authorities=" + authentication.getAuthorities());
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } else {
            System.out.println("[AUTH FILTER] no token found; rejecting request");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            return authHeader.replace("Bearer ", "");
        }

        return null;
    }

    private boolean isPublicEndpoint(
            HttpServletRequest request) {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        return ("/users".equals(uri)
                    && "POST".equalsIgnoreCase(method))
                || ("/users/login".equals(uri)
                    && "POST".equalsIgnoreCase(method))
                || ("/auth/request-code".equals(uri)
                    && "POST".equalsIgnoreCase(method))
                || ("/auth/verify-code".equals(uri)
                    && "POST".equalsIgnoreCase(method));
    }
}