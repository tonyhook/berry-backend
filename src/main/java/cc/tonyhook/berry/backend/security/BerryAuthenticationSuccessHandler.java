package cc.tonyhook.berry.backend.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import cc.tonyhook.berry.backend.dao.security.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class BerryAuthenticationSuccessHandler implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        session.setAttribute("id", userRepository.findByUsername(userDetails.getUsername()).getId());
        session.setAttribute("username", userDetails.getUsername());
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
    }

}
