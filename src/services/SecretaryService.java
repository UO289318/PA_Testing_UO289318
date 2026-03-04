package g54.si26.services;

import java.time.LocalDate;

import g54.si26.dao.*;

public class SecretaryService {

    private final InvoiceDAO dao = new InvoiceDAO();

    public void recordInvoice(
            double netAmount,
            double vat,
            double totalAmount,
            int teacherId,
            int actionId
    ) {

        // ===== Rule 1: total = net + vat =====
        double calc = netAmount + vat;
        if (Math.abs(calc - totalAmount) > 0.01) {
            throw new IllegalArgumentException("❌ Invoice total != net + VAT");
        }

        // ===== Rule 2: commitment check =====
        double initialPayment = dao.getInitialPayment(actionId);

        if (Math.abs(initialPayment - totalAmount) > 0.01) {
            System.out.println("⚠ MISMATCH: Invoice does not match initial payment commitment");
            System.out.println("➡ Action required:");
            System.out.println("   - Teacher must issue corrective invoice");
            System.out.println("   - OR system must update initial commitment");
        }

        // ===== Save invoice =====
        dao.createInvoice(
                LocalDate.now().toString(),
                netAmount,
                vat,
                totalAmount,
                teacherId,
                actionId
        );

        System.out.println("✅ Invoice recorded successfully");
    }
}