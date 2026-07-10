package com.appetit.gastrosystem.controller;

import com.appetit.gastrosystem.model.*;
import com.appetit.gastrosystem.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReporteService reporteService;
    private final MenuService menuService;
    private final MesaService mesaService;
    private final UsuarioService usuarioService;
    private final ReservaService reservaService;
    private final PedidoService pedidoService;

    public AdminController(ReporteService reporteService,
                           MenuService menuService,
                           MesaService mesaService,
                           UsuarioService usuarioService,
                           ReservaService reservaService,
                           PedidoService pedidoService) {
        this.reporteService = reporteService;
        this.menuService = menuService;
        this.mesaService = mesaService;
        this.usuarioService = usuarioService;
        this.reservaService = reservaService;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("ventasHoy", reporteService.obtenerVentasDelDia());
        model.addAttribute("platosMasVendidos", reporteService.obtenerPlatosMasVendidosDelDia());
        model.addAttribute("totalPedidos", pedidoService.listarTodos().size());
        model.addAttribute("totalMesas", mesaService.listarTodas().size());
        model.addAttribute("totalUsuarios", usuarioService.listarTodos().size());
        return "admin/dashboard";
    }

    // --- CRUD MENU (Categorias & Platos) ---
    @GetMapping("/menu")
    public String menuManager(Model model) {
        model.addAttribute("categorias", menuService.listarCategorias());
        model.addAttribute("platos", menuService.listarPlatos());
        model.addAttribute("nuevaCategoria", new Categoria());
        model.addAttribute("nuevoPlato", new Plato());
        return "admin/menu";
    }

    @PostMapping("/menu/categoria/guardar")
    public String guardarCategoria(@ModelAttribute("nuevaCategoria") Categoria categoria) {
        menuService.guardarCategoria(categoria);
        return "redirect:/admin/menu?categoriaSuccess";
    }

    @PostMapping("/menu/plato/guardar")
    public String guardarPlato(@ModelAttribute("nuevoPlato") Plato plato,
                               @RequestParam("idCategoria") Integer idCategoria) {
        Categoria categoria = menuService.buscarCategoriaPorId(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoría inválida"));
        plato.setCategoria(categoria);
        menuService.guardarPlato(plato);
        return "redirect:/admin/menu?platoSuccess";
    }

    @PostMapping("/menu/plato/{id}/toggle")
    public String togglePlato(@PathVariable("id") Long id, @RequestParam("activo") Boolean activo) {
        menuService.actualizarEstadoPlato(id, activo);
        return "redirect:/admin/menu";
    }

    // --- CRUD MESAS ---
    @GetMapping("/mesas")
    public String mesasManager(Model model) {
        model.addAttribute("mesas", mesaService.listarTodas());
        model.addAttribute("nuevaMesa", new Mesa());
        return "admin/mesas";
    }

    @PostMapping("/mesas/guardar")
    public String guardarMesa(@ModelAttribute("nuevaMesa") Mesa mesa) {
        try {
            mesaService.guardarMesa(mesa);
            return "redirect:/admin/mesas?success";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/mesas?error=" + e.getMessage();
        }
    }

    @PostMapping("/mesas/{id}/eliminar")
    public String eliminarMesa(@PathVariable("id") Integer id) {
        mesaService.eliminarMesa(id);
        return "redirect:/admin/mesas?deleted";
    }

    // --- GESTIÓN USUARIOS ---
    @GetMapping("/usuarios")
    public String usuariosManager(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("roles", NombreRol.values());
        model.addAttribute("nuevoUsuario", new Usuario());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("nuevoUsuario") Usuario usuario,
                                 @RequestParam("nombreRol") NombreRol nombreRol) {
        try {
            usuarioService.registrarUsuarioInterno(usuario, nombreRol);
            return "redirect:/admin/usuarios?success";
        } catch (Exception e) {
            return "redirect:/admin/usuarios?error=" + e.getMessage();
        }
    }

    @PostMapping("/usuarios/{id}/toggle")
    public String toggleUsuario(@PathVariable("id") Long id, @RequestParam("activo") Boolean activo) {
        usuarioService.actualizarEstado(id, activo);
        return "redirect:/admin/usuarios";
    }

    // --- GESTIÓN RESERVAS ---
    @GetMapping("/reservas")
    public String reservasManager(Model model) {
        model.addAttribute("reservas", reservaService.listarTodas());
        model.addAttribute("mesasDisponibles", mesaService.listarPorEstado(EstadoMesa.DISPONIBLE));
        return "admin/reservas";
    }

    @PostMapping("/reservas/{id}/confirmar")
    public String confirmarReserva(@PathVariable("id") Long id) {
        reservaService.cambiarEstadoReserva(id, EstadoReserva.CONFIRMADA);
        return "redirect:/admin/reservas?successConfirm";
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelarReserva(@PathVariable("id") Long id) {
        reservaService.cambiarEstadoReserva(id, EstadoReserva.CANCELADA);
        return "redirect:/admin/reservas?successCancel";
    }

    @PostMapping("/reservas/{id}/asignar-mesa")
    public String asignarMesaReserva(@PathVariable("id") Long id,
                                     @RequestParam(value = "idMesa", required = false) Integer idMesa) {
        reservaService.asignarMesa(id, idMesa);
        return "redirect:/admin/reservas?successMesa";
    }

    @GetMapping("/reporte/ventas-pdf")
    public void descargarReportePdf(HttpServletResponse response) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=reporte_ventas_diarias.pdf");
        try {
            reporteService.generarReporteDiarioPdf(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/resetear-sistema")
    public String resetearSistema() {
        pedidoService.resetearPedidosYVentas();
        return "redirect:/admin?resetSuccess";
    }
}
