package g54.si.tmConsulting.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import g54.si26.tmConsulting.TMConsultingModel;

import java.util.List;

public class TMConsultingModelTest {

    private TMConsultingModel model;

    @BeforeEach
    public void setUp() {
        model = new TMConsultingModel();
        
        // Limpieza y recarga de BD asegurada
        g54.si26.utils.Database db = new g54.si26.utils.Database();
        db.createDatabase(true); 
        
        db.executeUpdate("DELETE FROM MoneyMovement");
        db.executeUpdate("DELETE FROM Invoice");
        db.executeUpdate("DELETE FROM Payment");
        db.executeUpdate("DELETE FROM Inscription");
        db.executeUpdate("DELETE FROM Teacher_FormativeAction");
        db.executeUpdate("DELETE FROM FormativeAction");
        db.executeUpdate("DELETE FROM Professional");
        db.executeUpdate("DELETE FROM Teacher");
        
        db.loadDatabase();
    }

    @Test
    public void testGetReportData_AllStatus() {
        String startDate = "2020-01-01";
        String endDate = "2030-12-31";

        List<Object[]> report = model.getReportData(startDate, endDate, "All", "2026-04-19");
        
        assertNotNull(report, "El reporte no debe ser nulo");
        assertTrue(report.size() > 0, "Debería encontrar datos con el filtro 'All' y un rango amplio");
        
        Object[] firstRow = report.get(0);
        // CAMBIO AQUÍ: Ahora esperamos 9 columnas porque hemos añadido 'fa.spots'
        assertEquals(9, firstRow.length, "La fila del reporte debe contener 9 columnas calculadas");
    }

    @Test
    public void testGetReportData_ActiveFilter() {
        String startDate = "2020-01-01";
        String endDate = "2030-12-31";
        
        List<Object[]> report = model.getReportData(startDate, endDate, "Active", "2026-04-19");
        assertNotNull(report, "El reporte no debe ser nulo");
        
        for (Object[] row : report) {
            String status = (String) row[2]; // Posición 2 es el estado
            assertEquals("ACTIVE", status.toUpperCase(), "El filtro 'Active' falló. Se coló un curso distinto.");
        }
    }

    @Test
    public void testGetReportData_ClosedFilter() {
        String startDate = "2020-01-01";
        String endDate = "2030-12-31";
        
        List<Object[]> report = model.getReportData(startDate, endDate, "Closed", "2026-04-19");
        assertNotNull(report, "El reporte no debe ser nulo");
        
        for (Object[] row : report) {
            String status = (String) row[2];
            assertTrue(status.equalsIgnoreCase("CLOSED") || status.equalsIgnoreCase("CANCELLED"), 
                "El curso debería estar CLOSED o CANCELLED");
        }
    }

    @Test
    public void testGetReportData_NoResultsDateRange() {
        // Año lejano sin datos
        List<Object[]> report = model.getReportData("2099-01-01", "2099-12-31", "All", "2026-04-19");
        
        assertNotNull(report, "El reporte no debe ser nulo aunque no haya resultados");
        assertEquals(0, report.size(), "No debería haber cursos en el año 2099");
    }
}