package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.EstadoPedido;
import com.appetit.gastrosystem.model.Pedido;
import com.appetit.gastrosystem.services.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/cocina")
public class CocinaController {

    private final PedidoService pedidoService;

    public CocinaController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidosCocina();
        model.addAttribute("pedidos", pedidos);
        return "cocina/dashboard";
    }

    @PostMapping("/pedido/{id}/preparar")
    public String iniciarPreparacion(@PathVariable("id") Long id) {
        pedidoService.cambiarEstado(id, EstadoPedido.EN_PREPARACION);
        return "redirect:/cocina";
    }

    @PostMapping("/pedido/{id}/listo")
    public String marcarListo(@PathVariable("id") Long id) {
        pedidoService.cambiarEstado(id, EstadoPedido.LISTO);
        return "redirect:/cocina";
    }
}
