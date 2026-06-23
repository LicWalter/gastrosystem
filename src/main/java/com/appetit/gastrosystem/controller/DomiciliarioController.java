package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.security.UsuarioDetails;
import com.appetit.gastrosystem.services.DomicilioService;
import com.appetit.gastrosystem.services.PagoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/domiciliario")
public class DomiciliarioController {

    private final DomicilioService domicilioService;
    private final PagoService pagoService;

    public DomiciliarioController(DomicilioService domicilioService, PagoService pagoService) {
        this.domicilioService = domicilioService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        Usuario repartidor = usuarioDetails.getUsuario();
        
        List<Domicilio> misEntregas = domicilioService.listarActivosPorRepartidor(repartidor.getIdUsuario());
        List<Domicilio> disponibles = domicilioService.listarDisponiblesParaRepartidor();
        
        model.addAttribute("misEntregas", misEntregas);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("metodosPago", MetodoPago.values());
        
        return "domiciliario/dashboard";
    }

    @PostMapping("/entrega/{id}/tomar")
    public String tomarEntrega(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        Usuario repartidor = usuarioDetails.getUsuario();
        domicilioService.asignarRepartidor(id, repartidor.getIdUsuario());
        return "redirect:/domiciliario";
    }

    @PostMapping("/entrega/{id}/entregar")
    public String marcarEntregado(@PathVariable("id") Long id,
                                  @RequestParam("metodoPago") MetodoPago metodoPago) {
        Domicilio domicilio = domicilioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada"));

        // 1. Marcar como entregado (esto cambia el pedido a ENTREGADO)
        domicilioService.actualizarEstadoEntrega(id, EstadoEntrega.ENTREGADO);
        
        // 2. Registrar el pago (esto cambia el pedido a PAGADO)
        pagoService.registrarPago(domicilio.getPedido().getIdPedido(), metodoPago);

        return "redirect:/domiciliario?success";
    }
}
