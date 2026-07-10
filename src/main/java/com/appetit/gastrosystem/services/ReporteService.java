package com.appetit.gastrosystem.services;

import com.appetit.gastrosystem.model.Pedido;
import com.appetit.gastrosystem.model.DetallePedido;
import com.appetit.gastrosystem.model.TipoPedido;
import com.appetit.gastrosystem.repository.PedidoRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;

    public ReporteService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Double obtenerVentasDelDia() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        Double sum = pedidoRepository.sumTotalVentas(start, end);
        return sum != null ? sum : 0.0;
    }

    public Map<String, Long> obtenerPlatosMasVendidosDelDia() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        List<Object[]> raw = pedidoRepository.findPlatosMasVendidos(start, end);
        
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : raw) {
            if (row.length >= 2) {
                String plato = (String) row[0];
                Long cantidad = (Long) row[1];
                result.put(plato, cantidad);
            }
        }
        return result;
    }

    public void generarReporteDiarioPdf(OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(43, 45, 66));
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        // Header Title
        Paragraph title = new Paragraph("GastroSystem - Reporte Diario de Ventas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subTitle = new Paragraph("Generado el: " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()), smallFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(20);
        document.add(subTitle);

        // Stats summary
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        Double ventasTotales = obtenerVentasDelDia();
        List<Pedido> pedidos = pedidoRepository.findPedidosDelDiaConRelaciones(start, end);

        Paragraph summaryHeader = new Paragraph("Resumen del Día", sectionFont);
        summaryHeader.setSpacingBefore(10);
        summaryHeader.setSpacingAfter(10);
        document.add(summaryHeader);

        // Summary Table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);

        PdfPCell c1 = new PdfPCell(new Phrase("Ventas Totales (Pagadas):", boldFont));
        c1.setBackgroundColor(new Color(240, 240, 240));
        c1.setPadding(8);
        summaryTable.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase("$" + String.format("%.2f", ventasTotales), boldFont));
        c2.setPadding(8);
        summaryTable.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase("Total Pedidos Realizados:", boldFont));
        c3.setBackgroundColor(new Color(240, 240, 240));
        c3.setPadding(8);
        summaryTable.addCell(c3);

        PdfPCell c4 = new PdfPCell(new Phrase(String.valueOf(pedidos.size()), normalFont));
        c4.setPadding(8);
        summaryTable.addCell(c4);

        document.add(summaryTable);

        // Platos más vendidos
        Paragraph dishesHeader = new Paragraph("Platos Más Vendidos (Hoy)", sectionFont);
        dishesHeader.setSpacingBefore(10);
        dishesHeader.setSpacingAfter(10);
        document.add(dishesHeader);

        Map<String, Long> platosMasVendidos = obtenerPlatosMasVendidosDelDia();
        if (platosMasVendidos.isEmpty()) {
            document.add(new Paragraph("No se registran platos vendidos el día de hoy.", normalFont));
        } else {
            PdfPTable dishesTable = new PdfPTable(2);
            dishesTable.setWidthPercentage(100);
            dishesTable.setSpacingAfter(20);
            dishesTable.setWidths(new float[]{3f, 1f});

            PdfPCell h1 = new PdfPCell(new Phrase("Plato", headerFont));
            h1.setBackgroundColor(new Color(43, 45, 66));
            h1.setPadding(8);
            dishesTable.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("Cantidad Vendida", headerFont));
            h2.setBackgroundColor(new Color(43, 45, 66));
            h2.setPadding(8);
            dishesTable.addCell(h2);

            for (Map.Entry<String, Long> entry : platosMasVendidos.entrySet()) {
                PdfPCell cellPlato = new PdfPCell(new Phrase(entry.getKey(), normalFont));
                cellPlato.setPadding(6);
                dishesTable.addCell(cellPlato);

                PdfPCell cellCant = new PdfPCell(new Phrase(String.valueOf(entry.getValue()), normalFont));
                cellCant.setPadding(6);
                cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                dishesTable.addCell(cellCant);
            }
            document.add(dishesTable);
        }

        // Listado de pedidos del día
        Paragraph pedidosHeader = new Paragraph("Detalle de Pedidos del Día", sectionFont);
        pedidosHeader.setSpacingBefore(10);
        pedidosHeader.setSpacingAfter(10);
        document.add(pedidosHeader);

        if (pedidos.isEmpty()) {
            document.add(new Paragraph("No se registran pedidos el día de hoy.", normalFont));
        } else {
            PdfPTable pTable = new PdfPTable(5);
            pTable.setWidthPercentage(100);
            pTable.setSpacingAfter(10);
            pTable.setWidths(new float[]{1f, 1.5f, 2.5f, 1.5f, 1.5f});

            String[] headers = {"Pedido #", "Tipo", "Cliente / Mesero", "Estado", "Total"};
            for (String hText : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(hText, headerFont));
                hCell.setBackgroundColor(new Color(43, 45, 66));
                hCell.setPadding(8);
                pTable.addCell(hCell);
            }

            for (Pedido p : pedidos) {
                pTable.addCell(new PdfPCell(new Phrase("#" + p.getIdPedido(), normalFont)));
                pTable.addCell(new PdfPCell(new Phrase(String.valueOf(p.getTipoPedido()), normalFont)));

                String usuarioNombre = "";
                if (p.getCliente() != null) {
                    usuarioNombre = p.getCliente().getNombre() + " " + p.getCliente().getApellido();
                } else if (p.getMesero() != null) {
                    usuarioNombre = p.getMesero().getNombre() + " " + p.getMesero().getApellido();
                }
                pTable.addCell(new PdfPCell(new Phrase(usuarioNombre, normalFont)));
                pTable.addCell(new PdfPCell(new Phrase(String.valueOf(p.getEstado()), normalFont)));
                
                PdfPCell totalCell = new PdfPCell(new Phrase("$" + String.format("%.2f", p.getTotal()), normalFont));
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pTable.addCell(totalCell);
            }
            document.add(pTable);
        }

        document.close();
    }

    public void generarFacturaPdf(Pedido pedido, OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(43, 45, 66));
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font titleSmallFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(43, 45, 66));
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        // Header Title
        Paragraph brand = new Paragraph("GastroSystem", titleFont);
        brand.setAlignment(Element.ALIGN_CENTER);
        document.add(brand);

        Paragraph info = new Paragraph("Factura de Venta / Resumen de Pedido\nNIT: 900.123.456-7\nCalle del Sabor #123, GastroCiudad\nTeléfono: (601) 555-0199", smallFont);
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(20);
        document.add(info);

        // Invoice Header Table
        PdfPTable invoiceHeaderTable = new PdfPTable(2);
        invoiceHeaderTable.setWidthPercentage(100);
        invoiceHeaderTable.setSpacingAfter(20);

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.addElement(new Paragraph("FACTURA N°: #" + pedido.getIdPedido(), titleSmallFont));
        c1.addElement(new Paragraph("Fecha: " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(pedido.getFechaPedido()), normalFont));
        c1.addElement(new Paragraph("Tipo de Pedido: " + pedido.getTipoPedido(), normalFont));
        invoiceHeaderTable.addCell(c1);

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(PdfPCell.NO_BORDER);
        c2.addElement(new Paragraph("CLIENTE:", titleSmallFont));
        if (pedido.getCliente() != null) {
            c2.addElement(new Paragraph(pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido(), normalFont));
            c2.addElement(new Paragraph("Email: " + pedido.getCliente().getEmail(), normalFont));
            if (pedido.getCliente().getTelefono() != null) {
                c2.addElement(new Paragraph("Teléfono: " + pedido.getCliente().getTelefono(), normalFont));
            }
        } else {
            c2.addElement(new Paragraph("Consumo en Local (Mesa " + (pedido.getMesa() != null ? pedido.getMesa().getNumero() : "N/A") + ")", normalFont));
        }
        invoiceHeaderTable.addCell(c2);

        document.add(invoiceHeaderTable);

        // If it's a delivery order, show delivery address
        if (pedido.getTipoPedido() == TipoPedido.DOMICILIO && pedido.getDomicilio() != null) {
            Paragraph addressHeader = new Paragraph("DIRECCIÓN DE ENTREGA:", titleSmallFont);
            addressHeader.setSpacingAfter(5);
            document.add(addressHeader);
            Paragraph addressVal = new Paragraph(pedido.getDomicilio().getDireccionEntrega(), normalFont);
            addressVal.setSpacingAfter(15);
            document.add(addressVal);
        }

        // Items Table
        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setSpacingAfter(20);
        itemsTable.setWidths(new float[]{3f, 1f, 1f, 1f});

        String[] headers = {"Plato", "Precio Unitario", "Cant.", "Subtotal"};
        for (String hText : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(hText, headerFont));
            hCell.setBackgroundColor(new Color(43, 45, 66));
            hCell.setPadding(6);
            if (!hText.equals("Plato")) {
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            }
            itemsTable.addCell(hCell);
        }

        for (DetallePedido dp : pedido.getDetalles()) {
            itemsTable.addCell(new PdfPCell(new Phrase(dp.getPlato().getNombre(), normalFont)));
            
            PdfPCell priceCell = new PdfPCell(new Phrase("$" + String.format("%.2f", dp.getPrecioUnitario()), normalFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(priceCell);

            PdfPCell cantCell = new PdfPCell(new Phrase(String.valueOf(dp.getCantidad()), normalFont));
            cantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cantCell);

            PdfPCell subCell = new PdfPCell(new Phrase("$" + String.format("%.2f", dp.getSubtotal()), normalFont));
            subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(subCell);
        }

        document.add(itemsTable);

        // Total
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        PdfPCell totLbl = new PdfPCell(new Phrase("TOTAL:", boldFont));
        totLbl.setBorder(PdfPCell.NO_BORDER);
        totalTable.addCell(totLbl);

        PdfPCell totVal = new PdfPCell(new Phrase("$" + String.format("%.2f", pedido.getTotal()), boldFont));
        totVal.setBorder(PdfPCell.NO_BORDER);
        totVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totVal);

        document.add(totalTable);

        Paragraph thankYou = new Paragraph("\n\n¡Gracias por su compra!\nEsperamos verle de nuevo pronto.", titleSmallFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        document.add(thankYou);

        document.close();
    }
}
