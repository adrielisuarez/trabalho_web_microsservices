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
import java.util.Arrays;

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

        // ENDPOINTS PÚBLICOS
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = recoverToken(request);

        if (token != null) {

            String email =
                    jwtTokenService.getSubjectFromToken(token);

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

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } else {

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

        return ("/users".equals(uri) && "POST".equalsIgnoreCase(method))
                || ("/users/login".equals(uri) && "POST".equalsIgnoreCase(method));
    }
}