--Population of the DB with examples.

-- Teachers
INSERT INTO "Teacher" ("teacher_id", "name", "fiscal_id", "email", "phone") VALUES 
(1, 'Paco', '11111111A', 'pepe@uni.ovi.es', '600111222'),
(2, 'Mortadelo', '22222222B', 'Mortadelo@uni.ovi.es', '600333444');

-- Professionals
INSERT INTO "Professional" ("professional_id", "name", "surname", "phone", "email") VALUES 
(1, 'Pepe', 'viyuela', '611222333', 'pepe@viyuela.com'),
(2, 'Francisco', 'Ibáñez', '622333444', 'francisco@ibañez.com'),
(3, 'Filemón', 'Pi', '633444555', 'filemon@pi.com');

--Formative Actions
INSERT INTO "FormativeAction" ("action_id", "name", "objectives", "mainContents", "spots", "startDate", "endDate", "numberOfHours", "inscriptionPeriodStart", "inscriptionPeriodEnd", "location", "fee", "status", "initialPayment", "teacher_id") VALUES 
(1, 'Software Engineering', 'Basics of Software Quality', 'Requirements Engineering, Domain Modeling, Process Models', 20, '2026-05-01', '2026-05-15', '40', '2026-03-01', '2026-04-20', 'Online', 150.00, 'ACTIVE', 800.00, 1),
(2, 'Web Technologies', 'Basics of Web Technologies', 'HTML, CSS, JavaScript, PHP', 15, '2026-01-10', '2026-01-15', '20', '2025-12-01', '2025-12-31', 'Aulario Norte', 100.00, 'CLOSED', 500.00, 2),
(3, 'Testing course', 'Try to repeat the enroll', 'HTML, CSS, JavaScript, PHP', 15, '2026-04-10', '2026-05-15', '20', '2025-12-01', '2026-3-31', 'Aulario Norte', 100.00, 'ACTIVE', 500.00, 2),
(4, 'Testing course', 'Should only have 1 spot left', 'Lo q tu quieras manin, este curso tien d to', 2, '2026-04-10', '2026-05-15', '20', '2025-12-01', '2026-3-31', 'Aulario Norte', 100.00, 'ACTIVE', 500.00, 2),
(5, 'Testing course, 0 spots', 'Should only have 0 spot left', 'Lo q tu quieras manin, este curso tien d to', 0, '2026-04-10', '2026-05-15', '20', '2025-12-01', '2026-3-31', 'Aulario Norte', 100.00, 'ACTIVE', 500.00, 2),
(6, 'Testing course, -1 spots', 'Should only have -1 spot left', 'Lo q tu quieras manin, este curso tien d to', -1, '2026-04-10', '2026-05-15', '20', '2025-12-01', '2026-3-31', 'Aulario Norte', 100.00, 'ACTIVE', 500.00, 2);

--teacherFormativeAction
INSERT INTO "Teacher_FormativeAction" ("remuneration", "action_id", "teacher_id") 
VALUES (500.00, 1, 1), 
(300.00, 1, 2);


--Inscriptions
INSERT INTO "Inscription" ("inscription_id", "inscription_date", "fee", "state", "professional_id", "action_id") VALUES 
(1, '2026-03-01', 150.00, 'RECEIVED', 1, 1),
(2, '2026-03-01', 150.00, 'CONFIRMED', 2, 1),
(3, '2025-12-15', 100.00, 'CONFIRMED', 3, 2),
(4, '2026-03-02', 150.00, 'RECEIVED', 2, 1),
(5, '2026-03-03', 150.00, 'RECEIVED', 3, 1);

--Payments
INSERT INTO "Payment" ("payment_id", "amountPaid", "inscription_id", "payment_date") VALUES 
(1, 150.00, 2, '2026-03-01'),
(2, 100.00, 3, '2025-12-15');

-- invoices
INSERT INTO "Invoice" ("invoice_id", "invoice_date", "netAmount", "vat", "totalAmount", "teacher_id", "action_id") VALUES 
(1, '2026-01-16', 413.22, 86.78, 500.00, 2, 2),
(2, '2026-03-01', 800.00, 168.00, 968.00, 1, 1),
(3, '2026-03-05', 300.00, 63.00, 363.00, 2, 1);

-- TeacherPayments (Money Movements)
INSERT INTO "MoneyMovement" ("movement_id", "movement_date", "amount", "invoice_id") VALUES 
(1, '2026-01-20 10:00:00', 500.00, 1),
(2, '2026-03-10', 200.00, 3);
