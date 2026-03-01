package g54.si26.dao;

import java.time.LocalDate;

public class FormativeActionDAO {

    public final FormativeActionDAO dao = new FormativeActionDAO();

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
            System.out.println(" WARNING: Enrollment starts less than 3 weeks before the course date.");
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

	public void createFormativeAction(String name, String objectives, String mainContents, int spots, String string,
			String string2, String valueOf, String string3, String string4, String location, double fee, String string5,
			double teacherRemuneration, int teacherId) {
		// TODO Auto-generated method stub
		
	}
}