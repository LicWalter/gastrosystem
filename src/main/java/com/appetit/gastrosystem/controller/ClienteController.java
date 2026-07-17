package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.security.UsuarioDetails;
import com.appetit.gastrosystem.services.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cliente")
@Transactional(readOnly = true)
public class ClienteController {

    private final ReservaService reservaService;
    private final PedidoService pedidoService;
    private final MenuService menuService;
    private final ReporteService reporteService;
    private final FacturaJasperService facturaJasperService;

    public ClienteController(ReservaService reservaService,
                             PedidoService pedidoService,
                             MenuService menuService,
                             ReporteService reporteService,
                             FacturaJasperService facturaJasperService) {
        this.reservaService = reservaService;
        this.pedidoService = pedidoService;
        this.menuService = menuService;
        this.reporteService = reporteService;
        this.facturaJasperService = facturaJasperService;
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

    @Transactional
    @PostMapping("/reserva/guardar")
    public String guardarReserva(@AuthenticationPrincipal UsuarioDetails usuarioDetails,
                                 @RequestParam("fechaHoraStr") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaHora,
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

    @Transactional
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
        Pedido pedido = pedidoService.buscarPorIdConDetalles(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        model.addAttribute("pedido", pedido);
        return "cliente/ver_pedido";
    }

    @GetMapping("/pedido/factura-pdf/{id}")
    public void descargarFacturaPdf(@PathVariable("id") Long id, HttpServletResponse response) {
        Pedido pedido = pedidoService.buscarPorIdConDetalles(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        try {
            byte[] pdfBytes = facturaJasperService.generarFacturaPdf(pedido);
            response.setContentType("application/pdf");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"factura_" + id + ".pdf\"");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("text/plain;charset=UTF-8");
                response.setStatus(500);
                java.io.PrintWriter writer = response.getWriter();
                writer.println("DIAGNOSTICO DE ERROR DE FACTURA (PEDIDO #" + id + "):");
                e.printStackTrace(writer);
                writer.flush();
            } catch (Exception ignored) {}
        }
    }

    @Transactional
    @PostMapping("/pedido/{id}/cancelar")
    public String cancelarPedido(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        
        // Validar que el cliente sea el propietario
        if (!pedido.getCliente().getIdUsuario().equals(usuarioDetails.getUsuario().getIdUsuario())) {
            throw new IllegalArgumentException("No tiene permisos para cancelar este pedido");
        }
        
        // Solo permitir cancelar si está en estado RECIBIDO
        if (pedido.getEstado() == EstadoPedido.RECIBIDO) {
            pedidoService.cambiarEstado(id, EstadoPedido.CANCELADO);
            return "redirect:/cliente?cancelSuccess";
        } else {
            return "redirect:/cliente?cancelError";
        }
    }
}
