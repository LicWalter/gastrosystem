package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.security.UsuarioDetails;
import com.appetit.gastrosystem.services.MenuService;
import com.appetit.gastrosystem.services.PedidoService;
import com.appetit.gastrosystem.services.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ReservaService reservaService;
    private final PedidoService pedidoService;
    private final MenuService menuService;

    public ClienteController(ReservaService reservaService,
                             PedidoService pedidoService,
                             MenuService menuService) {
        this.reservaService = reservaService;
        this.pedidoService = pedidoService;
        this.menuService = menuService;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UsuarioDetails usuarioDetails, Model model) {
        Usuario cliente = usuarioDetails.getUsuario();
        model.addAttribute("usuario", cliente);
        model.addAttribute("reservas", reservaService.listarPorCliente(cliente.getIdUsuario()));
        model.addAttribute("pedidos", pedidoService.listarPorCliente(cliente.getIdUsuario()));
        return "cliente/dashboard";
    }

    @GetMapping("/reserva/nueva")
    public String nuevaReservaForm(Model model) {
        model.addAttribute("reserva", new Reserva());
        return "cliente/nueva_reserva";
    }

    @PostMapping("/reserva/guardar")
    public String guardarReserva(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
                                 @RequestParam("fechaHoraStr") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                                 @RequestParam("numeroPersonas") Integer numeroPersonas) {
        Usuario cliente = usuarioDetails.getUsuario();
        Reserva reserva = new Reserva();
        reserva.setUsuario(cliente);
        reserva.setFechaHora(fechaHora);
        reserva.setNumeroPersonas(numeroPersonas);
        
        reservaService.crearReserva(reserva);
        return "redirect:/cliente?reservaSuccess";
    }

    @GetMapping("/pedido/nuevo")
    public String nuevoPedidoForm(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("platos", menuService.listarPlatosActivos());
        if (error != null) {
            model.addAttribute("pedidoError", error);
        }
        return "cliente/nuevo_pedido";
    }

    @PostMapping("/pedido/guardar")
    public String guardarPedido(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
                                @RequestParam("direccionEntrega") String direccionEntrega,
                                @RequestParam(value = "platoIds", required = false) List<Long> platoIds,
                                @RequestParam(value = "cantidades", required = false) List<Integer> cantidades) {
        Usuario cliente = usuarioDetails.getUsuario();
        
        if (platoIds == null || cantidades == null || platoIds.isEmpty()) {
            return "redirect:/cliente/pedido/nuevo?error=Debe seleccionar al menos un plato";
        }

        // Validar que haya al menos un plato con cantidad > 0
        boolean tieneItems = false;
        for (int i = 0; i < platoIds.size(); i++) {
            if (cantidades.get(i) != null && cantidades.get(i) > 0) {
                tieneItems = true;
                break;
            }
        }

        if (!tieneItems) {
            return "redirect:/cliente/pedido/nuevo?error=Debe indicar la cantidad para al menos un plato";
        }

        // Crear pedido a domicilio
        Pedido pedido = pedidoService.crearPedidoDomicilio(cliente.getIdUsuario(), direccionEntrega);

        // Agregar ítems
        for (int i = 0; i < platoIds.size(); i++) {
            Integer cant = cantidades.get(i);
            if (cant != null && cant > 0) {
                pedidoService.agregarItem(pedido.getIdPedido(), platoIds.get(i), cant);
            }
        }

        return "redirect:/cliente?pedidoSuccess";
    }

    @GetMapping("/pedido/ver/{id}")
    public String verPedido(@PathVariable("id") Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        model.addAttribute("pedido", pedido);
        return "cliente/ver_pedido";
    }
}
