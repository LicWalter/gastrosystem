package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.security.UsuarioDetails;
import com.appetit.gastrosystem.services.MenuService;
import com.appetit.gastrosystem.services.MesaService;
import com.appetit.gastrosystem.services.PagoService;
import com.appetit.gastrosystem.services.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@RequestMapping("/mesero")
@Transactional(readOnly = true)
public class MeseroController {

    private final MesaService mesaService;
    private final PedidoService pedidoService;
    private final MenuService menuService;
    private final PagoService pagoService;

    public MeseroController(MesaService mesaService,
                            PedidoService pedidoService,
                            MenuService menuService,
                            PagoService pagoService) {
        this.mesaService = mesaService;
        this.pedidoService = pedidoService;
        this.menuService = menuService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Mesa> mesas = mesaService.listarTodas();
        model.addAttribute("mesas", mesas);
        
        // Cargar pedidos activos de cada mesa (si existen)
        // Usamos un helper en el template, o mapeamos en el controlador, pero para que sea simple y limpio:
        model.addAttribute("pedidoService", pedidoService);
        return "mesero/dashboard";
    }

    @Transactional
    @PostMapping("/pedido/abrir")
    public String abrirPedido(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
                              @RequestParam("idMesa") Integer idMesa) {
        Usuario mesero = usuarioDetails.getUsuario();
        Pedido pedido = pedidoService.crearPedidoMesa(idMesa, mesero.getIdUsuario());
        return "redirect:/mesero/pedido/ver/" + pedido.getIdPedido();
    }

    @GetMapping("/pedido/ver/{id}")
    public String verPedido(@PathVariable("id") Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorIdConDetalles(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        model.addAttribute("pedido", pedido);
        model.addAttribute("metodosPago", MetodoPago.values());
        return "mesero/pedido_mesa";
    }

    @GetMapping("/pedido/ver/{id}/agregar")
    public String agregarItemForm(@PathVariable("id") Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorIdConDetalles(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        model.addAttribute("pedido", pedido);
        model.addAttribute("platos", menuService.listarPlatosActivos());
        return "mesero/agregar_item";
    }

    @Transactional
    @PostMapping("/pedido/ver/{id}/agregar/guardar")
    public String guardarItem(@PathVariable("id") Long id,
                              @RequestParam("idPlato") Long idPlato,
                              @RequestParam("cantidad") Integer cantidad) {
        pedidoService.agregarItem(id, idPlato, cantidad);
        return "redirect:/mesero/pedido/ver/" + id;
    }

    @Transactional
    @PostMapping("/pedido/ver/{id}/remover/{idDetalle}")
    public String removerItem(@PathVariable("id") Long id,
                              @PathVariable("idDetalle") Long idDetalle) {
        pedidoService.removerItem(id, idDetalle);
        return "redirect:/mesero/pedido/ver/" + id;
    }

    @Transactional
    @PostMapping("/pedido/ver/{id}/cambiar-estado")
    public String cambiarEstadoPedido(@PathVariable("id") Long id,
                                      @RequestParam("estado") EstadoPedido estado) {
        pedidoService.cambiarEstado(id, estado);
        return "redirect:/mesero/pedido/ver/" + id;
    }

    @Transactional
    @PostMapping("/pedido/ver/{id}/pagar")
    public String pagarPedido(@PathVariable("id") Long id,
                              @RequestParam("metodoPago") MetodoPago metodoPago) {
        try {
            pagoService.registrarPago(id, metodoPago);
            return "redirect:/mesero?pagoSuccess";
        } catch (Exception e) {
            return "redirect:/mesero/pedido/ver/" + id + "?error=" + e.getMessage();
        }
    }
}
