package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.services.MenuService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class HomeController {

    private final MenuService menuService;

    public HomeController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            model.addAttribute("categorias", menuService.listarCategorias());
            model.addAttribute("platos", menuService.listarPlatosActivos());
            return "index";
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
