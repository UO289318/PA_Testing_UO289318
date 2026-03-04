package g54.si26.services;

import java.time.LocalDate;
import g54.si26.dao.*;
public class TrainingManagerService {

    private final FormativeActionDAO dao = new FormativeActionDAO();

    public void planFormativeAction(
            String name,
            String objectives,
            String mainContents,
            int spots,
            LocalDate startDate,
            LocalDate endDate,
            int hours,
            LocalDate inscriptionStart,
            LocalDate inscriptionEnd,
            String location,
            double fee,
            double teacherRemuneration,
            int teacherId
    ) {

        // ===== Business rule: enrollment >= 3 weeks before =====
        LocalDate minDate = startDate.minusWeeks(3);
        if (inscriptionStart.isAfter(minDate)) {
            System.out.println("⚠ WARNING: Enrollment starts less than 3 weeks before the course date.");
        }

        // ===== Fee rule =====
        if (fee < 0) {
            throw new IllegalArgumentException("Fee cannot be negative");
        }

        // ===== Insert into DB =====
        dao.createFormativeAction(
                name,
                objectives,
                mainContents,
                spots,
                startDate.toString(),
                endDate.toString(),
                String.valueOf(hours),
                inscriptionStart.toString(),
                inscriptionEnd.toString(),
                location,
                fee,
                "ACTIVE",
                teacherRemuneration,
                teacherId
        );

        System.out.println("✅ Formative Action created successfully");
    }
}
