
INSERT INTO "Teacher" ("teacher_id", "name", "fiscal_id", "email", "phone") VALUES
(1, 'Claudio', '11111111A', 'claudio@coiipa.com', '600111111'),
(2, 'Raquel', '22222222B', 'raquel@coiipa.com', '600222222');

INSERT INTO "Professional" ("professional_id", "name", "surname", "phone", "email") VALUES
(1, 'Alice', 'Martínez', '611223344', 'alice@test.com'),
(2, 'Marta', 'Rodriguez', '611223355', 'marta@test.com'),
(3, 'Fanjul', 'García', '611223366', 'fanjul@test.com'),
(4, 'Mateo', 'Martínez', '611223377', 'mateo@test.com');

INSERT INTO "FormativeAction" ("action_id", "name", "objectives", "mainContents", "spots", "startDate", "endDate", "numberOfHours", "inscriptionPeriodStart", "inscriptionPeriodEnd", "location", "fee", "status", "initialPayment", "teacher_id") VALUES
(1, 'New Testing Techniques', 'Learn new testing techniques', 'TDD, BDD, JUnit', 4, '2025-09-01', '2025-09-02', '24', '2025-07-01', '2025-07-31', 'Online', 200.00, 'CLOSED', 200.00, 1),
(2, 'Neo4J Database Administration', 'Databases basics', 'Neo4J, Cypher', 3, '2026-03-27', '2026-03-28', '24', '2026-02-23', '2026-03-13', 'Aulario Sur', 150.00, 'ACTIVE', 150.00, 2);

INSERT INTO "Teacher_FormativeAction" ("remuneration", "status", "action_id", "teacher_id") VALUES
(500.00, 'PAID', 1, 1),
(300.00, 'PENDING', 2, 2);

INSERT INTO "Inscription" ("inscription_id", "inscription_date", "fee", "state", "professional_id", "action_id") VALUES
(1, '2025-07-15', 200.00, 'CONFIRMED', 1, 1),
(2, '2025-07-30', 200.00, 'CONFIRMED', 2, 1),
(3, '2026-02-23', 150.00, 'CONFIRMED', 2, 2),
(4, '2026-02-25', 150.00, 'CONFIRMED', 3, 2),
(5, '2026-03-05', 150.00, 'RECEIVED', 4, 2);

INSERT INTO "Payment" ("payment_id", "amountPaid", "payment_date", "inscription_id") VALUES
(1, 200.00, '2025-07-16', 1),
(2, 200.00, '2025-08-01', 2),
(3, 150.00, '2026-02-24', 3),
(4, 150.00, '2026-03-01', 4);

INSERT INTO "Invoice" ("invoice_id", "invoice_date", "netAmount", "vat", "totalAmount", "status", "teacher_id", "action_id") VALUES
(1, '2025-09-03', 413.22, 86.78, 500.00, 'PAID', 1, 1);

INSERT INTO "MoneyMovement" ("movement_id", "movement_date", "amount", "invoice_id") VALUES
(1, '2025-09-05', 500.00, 1);