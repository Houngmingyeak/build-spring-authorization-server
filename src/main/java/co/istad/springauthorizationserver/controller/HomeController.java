package co.istad.springauthorizationserver.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home(Authentication authentication) {

        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authorityName = authority.getAuthority();
            if (authorityName.startsWith("ROLE_")) {
                roles.add(authorityName);
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Authorization Server is running");
        body.put("loggedInAs", authentication.getName());
        body.put("roles", roles);

        return body;
    }
}