package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.Usuario;
import com.appetit.gastrosystem.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Nombre de usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("loginSuccess", "Sesión cerrada correctamente.");
        }
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("usuario", new Usuario());
        if (error != null) {
            model.addAttribute("registroError", error);
        }
        return "registro";
    }

    @PostMapping("/registro/guardar")
    public String registrarCliente(@ModelAttribute("usuario") Usuario usuario) {
        try {
            usuarioService.registrarCliente(usuario);
            return "redirect:/login?success";
        } catch (IllegalArgumentException e) {
            return "redirect:/registro?error=" + e.getMessage();
        }
    }
}
