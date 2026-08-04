package com.securestream.auth.security;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Get Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. If no token, continue request
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract JWT token

        String jwt = authHeader.substring(7);


        // 4. Extract userName /email from token

        String username = jwtService.extractUsername(jwt);


        // 5. Check user is not already authenticated

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

               System.out.println(username);
            // 6. Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 7. Validate token
            if (jwtService.isTokenValid(jwt, userDetails)) {


                // 8. Create Authentication object

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );


                // 9. Store user in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        }


        // 10. Continue request
        filterChain.doFilter(request,response);

    }

}