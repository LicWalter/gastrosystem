package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.security.UsuarioDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        model.addAttribute("usuario", usuarioDetails.getUsuario());
        return "inicio";
    }
}
