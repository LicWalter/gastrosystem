package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.DetallePedido;
import com.appetit.gastrosystem.model.Pedido;
import com.appetit.gastrosystem.model.TipoPedido;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servicio encargado de generar la factura PDF mediante JasperReports 6.21.
 *
 * Flujo:
 *  1. Carga y compila la plantilla factura.jrxml desde /resources/reports/
 *  2. Construye los parámetros del encabezado (datos del pedido y cliente)
 *  3. Crea un JRBeanCollectionDataSource con los detalles del pedido
 *  4. Exporta el reporte a un arreglo de bytes (PDF)
 */
@Service
public class FacturaJasperService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String RUTA_PLANTILLA = "reports/factura.jrxml";

    /**
     * Genera la factura PDF para un pedido dado.
     *
     * @param pedido El pedido con sus relaciones cargadas (detalles, cliente, mesa, domicilio, pago)
     * @return Arreglo de bytes del PDF generado
     * @throws JRException Si ocurre algún error en JasperReports
     */
    public byte[] generarFacturaPdf(Pedido pedido) throws JRException {

        // ── 1. Compilar la plantilla ──────────────────────────────────────
        InputStream plantillaStream = obtenerPlantilla();
        JasperReport jasperReport = JasperCompileManager.compileReport(plantillaStream);

        // ── 2. Construir parámetros del encabezado ────────────────────────
        Map<String, Object> parametros = construirParametros(pedido);

        // ── 3. Construir datasource con los detalles ──────────────────────
        List<DetalleFacturaBean> items = convertirDetalles(pedido.getDetalles());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

        // ── 4. Llenar y exportar el reporte ───────────────────────────────
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    // ── Métodos privados ──────────────────────────────────────────────────

    private InputStream obtenerPlantilla() {
        try {
            ClassPathResource resource = new ClassPathResource(RUTA_PLANTILLA);
            return resource.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("No se encontró la plantilla de factura en: " + RUTA_PLANTILLA, e);
        }
    }

    private Map<String, Object> construirParametros(Pedido pedido) {
        Map<String, Object> params = new HashMap<>();

        // Datos básicos del pedido
        params.put("P_FACTURA_NUM", pedido.getIdPedido());
        params.put("P_FECHA",
                pedido.getFechaPedido() != null
                        ? pedido.getFechaPedido().format(FORMATO_FECHA)
                        : "N/A");
        params.put("P_TIPO_PEDIDO",
                pedido.getTipoPedido() != null
                        ? pedido.getTipoPedido().name()
                        : "N/A");
        params.put("P_ESTADO",
                pedido.getEstado() != null
                        ? pedido.getEstado().name()
                        : "N/A");
        params.put("P_TOTAL",
                pedido.getTotal() != null
                        ? pedido.getTotal()
                        : BigDecimal.ZERO);

        // Datos del cliente
        if (pedido.getCliente() != null) {
            params.put("P_CLIENTE_NOMBRE",
                    pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido());
            params.put("P_CLIENTE_EMAIL", pedido.getCliente().getEmail());
            params.put("P_CLIENTE_TEL",
                    pedido.getCliente().getTelefono() != null
                            ? pedido.getCliente().getTelefono() : "");
        } else if (pedido.getMesero() != null) {
            params.put("P_CLIENTE_NOMBRE", "Consumo en local");
            params.put("P_CLIENTE_EMAIL",
                    "Mesero: " + pedido.getMesero().getNombre()
                            + " " + pedido.getMesero().getApellido());
            params.put("P_CLIENTE_TEL", "");
        } else {
            params.put("P_CLIENTE_NOMBRE", "Cliente Anónimo");
            params.put("P_CLIENTE_EMAIL", "");
            params.put("P_CLIENTE_TEL", "");
        }

        // Mesa o dirección de domicilio
        if (pedido.getTipoPedido() == TipoPedido.MESA && pedido.getMesa() != null) {
            params.put("P_MESA", String.valueOf(pedido.getMesa().getNumero()));
            params.put("P_DIRECCION", "");
        } else if (pedido.getTipoPedido() == TipoPedido.DOMICILIO && pedido.getDomicilio() != null) {
            params.put("P_MESA", "");
            params.put("P_DIRECCION", pedido.getDomicilio().getDireccionEntrega());
        } else {
            params.put("P_MESA", "");
            params.put("P_DIRECCION", "");
        }

        // Método de pago
        if (pedido.getPago() != null && pedido.getPago().getMetodoPago() != null) {
            params.put("P_METODO_PAGO", pedido.getPago().getMetodoPago().name());
        } else {
            params.put("P_METODO_PAGO", "Pendiente");
        }

        return params;
    }

    private List<DetalleFacturaBean> convertirDetalles(List<DetallePedido> detalles) {
        List<DetalleFacturaBean> items = new ArrayList<>();
        if (detalles != null) {
            for (DetallePedido dp : detalles) {
                items.add(new DetalleFacturaBean(
                        dp.getPlato() != null ? dp.getPlato().getNombre() : "N/A",
                        dp.getCantidad(),
                        dp.getPrecioUnitario(),
                        dp.getSubtotal()
                ));
            }
        }
        return items;
    }

    // ── Bean interno (DataSource row) ─────────────────────────────────────

    /**
     * Bean plano que JasperReports usa como fila del datasource.
     * Cada campo corresponde a un &lt;field&gt; declarado en el .jrxml.
     */
    public static class DetalleFacturaBean {

        private final String nombrePlato;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal subtotal;

        public DetalleFacturaBean(String nombrePlato, Integer cantidad,
                                  BigDecimal precioUnitario, BigDecimal subtotal) {
            this.nombrePlato = nombrePlato;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
            this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        }

        public String getNombrePlato()    { return nombrePlato; }
        public Integer getCantidad()      { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public BigDecimal getSubtotal()   { return subtotal; }
    }
}
