package com.securestream.auth.security;

import java.io.IOException;

import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Get Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. If no token, continue request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 3. Extract JWT token

        String jwt = authHeader.substring(7);


        // 4. Extract userName /email from token
        String username = jwtService.extractUserId(jwt);

        try{
        // 5. Validate token
        if (jwtService.isAccessToken(jwt) && jwtService.isTokenValid(jwt)) {
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                System.out.println(username);
                // 6. Load user from database
                CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // 7. Create Authentication object
                // 8. Store user in SecurityContext

            }

        }
    } catch (JwtException | IllegalArgumentException e) {
        log.debug("JWT processing failed: {}", e.getMessage());
    } catch (Exception e) {
        log.warn("Unexpected error while authenticating JWT: {}", e.getMessage());
    }

        filterChain.doFilter(request,response);
    }


    // 10. Continue request


}

