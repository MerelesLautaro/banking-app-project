package com.lautadev.lautadev.Security;

import com.lautadev.lautadev.Exception.ApiException;
import com.lautadev.lautadev.Service.authentication.TokenService;
import com.lautadev.lautadev.Util.Constants;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JWTBlacklistManager jwtBlacklistManager;

    @Value("${jwt.header}")
    private String authHeader;

    @Value("${jwt.prefix}")
    private String authPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String uri = request.getRequestURI();
        boolean isUnprotectedPath = Constants.UNPROTECTED_PATHS.stream().anyMatch(uri::startsWith);

        if (isUnprotectedPath) {
            doFilter(request, response, filterChain);
            return;
        }

        final String authHeaderValue = request.getHeader(authHeader);

        try {
            if (authHeaderValue == null
                    || !authHeaderValue.startsWith(authPrefix)
                    || jwtBlacklistManager.isBlackListed(authHeaderValue)) {
                throw ApiException.accessDenied();
            }

            final String token = authHeaderValue.substring(authPrefix.length());
            final String userEmail = tokenService.extractUsername(token);

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && currentAuth == null) {
                UserDetails user = userDetailsService.loadUserByUsername(userEmail);

                if (tokenService.isTokenValid(token, user)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    throw ApiException.accessDenied();
                }
            }

            filterChain.doFilter(request, response);

        } catch (ApiException | JwtException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            log.error("Error inesperado en JWTAuthenticationFilter", e);
            throw e;
        }
    }
}
