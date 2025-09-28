package com.company.employee.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final String cookieName;

    public JwtAuthenticationSuccessHandler(JwtUtil jwtUtil, String cookieName) {
        this.jwtUtil = jwtUtil;
        this.cookieName = cookieName;
        // Default target is '/', but do not always force it — allow savedRequest to take precedence
        this.setDefaultTargetUrl("/");
        this.setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.toList());

        // Check if remember me is requested
        boolean rememberMe = "true".equals(request.getParameter("remember-me"))
                || "on".equals(request.getParameter("remember-me"));

        String token = jwtUtil.generateToken(principal.getUsername(), roles, rememberMe);

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        // For local dev without HTTPS we don't set Secure; in production set this true
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.getExpirationMs(rememberMe) / 1000));

        response.addCookie(cookie);

        // If client expects JSON (API / AJAX), return token JSON.
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        boolean wantsJson = (accept != null && accept.contains("application/json"))
                || (xRequestedWith != null && "XMLHttpRequest".equalsIgnoreCase(xRequestedWith));

        if (wantsJson) {
            response.setContentType("application/json");
            response.getWriter().write('{' + "\"accessToken\":\"" + token + "\"}");
            response.getWriter().flush();
            return;
        }

        // For browser logins, redirect based on role
        String targetUrl = "/"; // default
        if (roles.contains("ADMIN")) {
            targetUrl = "/";
        } else if (roles.contains("HR")) {
            targetUrl = "/payroll";
        }

        // Perform redirect
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
