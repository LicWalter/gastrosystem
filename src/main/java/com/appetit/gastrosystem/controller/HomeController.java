package com.appetit.gastrosystem.controller;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMINISTRADOR")) {
                return "redirect:/admin";
            } else if (role.equals("ROLE_MESERO")) {
                return "redirect:/mesero";
            } else if (role.equals("ROLE_COCINA")) {
                return "redirect:/cocina";
            } else if (role.equals("ROLE_DOMICILIARIO")) {
                return "redirect:/domiciliario";
            } else if (role.equals("ROLE_CLIENTE")) {
                return "redirect:/cliente";
            }
        }
        return "inicio";
    }
}
