package g54.si26.financeConsulting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class FinancialConsultingModel {

    // TODO: Ajusta la URL de tu base de datos SQLite según dónde esté tu archivo .db
    private static final String URL_DB = "jdbc:sqlite:src/main/resources/BD.db";

    public List<Object[]> obtenerReporteFinanciero(String fechaInicio, String fechaFin, String estadoFiltro) {
        List<Object[]> resultados = new ArrayList<>();

        // Consulta SQL para obtener ingresos y gastos confirmados vs potenciales
        String sql = "SELECT " +
                "fa.startDate as fecha, " +
                "fa.name as nombre, " +
                "fa.status as estado, " +
                "(fa.spots * fa.fee) as ingresos_potenciales, " +
                "(SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p INNER JOIN Inscription i ON p.inscription_id = i.inscription_id WHERE i.action_id = fa.action_id) as ingresos_confirmados, " +
                "(SELECT COALESCE(SUM(tfa.remuneration), 0) FROM Teacher_FormativeAction tfa WHERE tfa.action_id = fa.action_id) as gastos_potenciales, " +
                "(SELECT COALESCE(SUM(mm.amount), 0) FROM MoneyMovement mm INNER JOIN Invoice inv ON mm.invoice_id = inv.invoice_id WHERE inv.action_id = fa.action_id) as gastos_confirmados " +
                "FROM FormativeAction fa " +
                "WHERE fa.startDate >= ? AND fa.startDate <= ?";

        if (!"Todos".equals(estadoFiltro)) {
            sql += " AND fa.status = ?";
        }

        try (Connection conn = DriverManager.getConnection(URL_DB);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, fechaFin);

            if (!"Todos".equals(estadoFiltro)) {
                pstmt.setString(3, estadoFiltro);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String estado = rs.getString("estado");
                double ingPotenciales = rs.getDouble("ingresos_potenciales");
                double ingConfirmados = rs.getDouble("ingresos_confirmados");
                double gasPotenciales = rs.getDouble("gastos_potenciales");
                double gasConfirmados = rs.getDouble("gastos_confirmados");

                double ingEstimados = 0;
                double gasEstimados = 0;

                // Lógica de negocio: Si está activo, el estimado es lo potencial menos lo ya confirmado
                if (!"Cerrado".equalsIgnoreCase(estado)) {
                    ingEstimados = Math.max(0, ingPotenciales - ingConfirmados);
                    gasEstimados = Math.max(0, gasPotenciales - gasConfirmados);
                }

                double totalIngresos = ingEstimados + ingConfirmados;
                double totalGastos = gasEstimados + gasConfirmados;
                double balance = totalIngresos - totalGastos;

                // Formatear la fila de la tabla
                Object[] fila = new Object[]{
                        rs.getString("fecha"),
                        rs.getString("nombre"),
                        estado,
                        formatearMonto(ingEstimados, estado),
                        formatearMonto(ingConfirmados, estado),
                        "$" + totalIngresos,
                        formatearMonto(gasEstimados, estado),
                        formatearMonto(gasConfirmados, estado),
                        "$" + totalGastos,
                        "$" + balance
                };
                resultados.add(fila);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al consultar la base de datos: " + e.getMessage());
        }
        return resultados;
    }

    // Método auxiliar para poner "-" si el curso está cerrado, o "$X" si está activo
    private String formatearMonto(double monto, String estado) {
        if ("Cerrado".equalsIgnoreCase(estado)) {
            return "-";
        }
        return "$" + monto;
    }
}