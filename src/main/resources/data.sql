-- 1. Maestras
INSERT INTO "Community" ("community_id", "communityName") VALUES
(1, 'COIIPA'),
(2, 'CITIPA'),
(3, 'Externo');

INSERT INTO "Teacher" ("teacher_id", "name", "fiscal_id", "email", "phone") VALUES
(1, 'Claudio', '11111111A', 'claudio@coiipa.com', '600111111'),
(2, 'Raquel', '22222222B', 'raquel@coiipa.com', '600222222');

INSERT INTO "Professional" ("professional_id", "name", "surname", "phone", "email", "community_id") VALUES
(1, 'Alice', 'Martínez', '611223344', 'alice@test.com', 1),
(2, 'Marta', 'Rodriguez', '611223355', 'marta@test.com', 2),
(3, 'Fanjul', 'García', '611223366', 'fanjul@test.com', 3),
(4, 'Mateo', 'Martínez', '611223377', 'mateo@test.com', 1);

-- 2. Acciones Formativas (Sin la columna obsoleta teacher_id ni fee)
INSERT INTO "FormativeAction" ("action_id", "name", "objectives", "mainContents", "spots", "startDate", "endDate", "numberOfHours", "inscriptionPeriodStart", "inscriptionPeriodEnd", "location", "status") VALUES
(1, 'New Testing Techniques', 'Learn new testing techniques', 'TDD, BDD, JUnit', 4, '2025-09-01', '2025-09-02', '24', '2025-07-01', '2025-07-31', 'Online', 'CLOSED'),
(2, 'Neo4J Database Administration', 'Databases basics', 'Neo4J, Cypher', 3, '2026-03-27', '2026-03-28', '24', '2026-02-23', '2026-03-13', 'Aulario Sur', 'ACTIVE');

-- 3. Tasas (Fees) asociadas a las acciones y comunidades
INSERT INTO "Fee" ("amount", "community_id", "action_id") VALUES
(200.00, 1, 1),
(150.00, 2, 2);

-- 4. Relación Profesores y Remuneración
INSERT INTO "Teacher_FormativeAction" ("remuneration", "status", "action_id", "teacher_id") VALUES
(500.00, 'PAID', 1, 1),
(300.00, 'PENDING', 2, 2);

-- 5. Inscripciones (applied_fee en lugar de fee)
INSERT INTO "Inscription" ("inscription_id", "inscription_date", "applied_fee", "state", "professional_id", "action_id") VALUES
(1, '2025-07-15', 200.00, 'CONFIRMED', 1, 1),
(2, '2025-07-30', 200.00, 'CONFIRMED', 2, 1),
(3, '2026-02-23', 150.00, 'CONFIRMED', 2, 2),
(4, '2026-02-25', 150.00, 'RECEIVED', 3, 2),
(5, '2026-03-05', 150.00, 'RECEIVED', 4, 2);

-- 6. Facturas (Invoices)
INSERT INTO "Invoice" ("invoice_id", "invoice_date", "netAmount", "vat", "totalAmount", "status", "teacher_id", "action_id") VALUES
(1, '2025-09-03', 413.22, 86.78, 500.00, 'PAID', 1, 1),
(2, '2026-03-28', 260.00, 40.00, 300.00, 'PENDING', 2, 2);

-- 7. Movimientos de Dinero (Sustituye a la tabla Payment antigua)
-- Para inscripciones (Alumnos)
INSERT INTO "MoneyMovement" ("movement_date", "amount", "status", "type", "inscription_id", "invoice_id") VALUES
('2025-07-16', 200.00, 'EXECUTED', 'PAYMENT', 1, NULL),
('2025-08-01', 200.00, 'EXECUTED', 'PAYMENT', 2, NULL),
('2026-02-24', 150.00, 'EXECUTED', 'PAYMENT', 3, NULL),
('2026-03-01', 150.00, 'EXECUTED', 'PAYMENT', 4, NULL);

-- Para facturas (Profesores)
INSERT INTO "MoneyMovement" ("movement_date", "amount", "status", "type", "inscription_id", "invoice_id") VALUES
('2025-09-05', 500.00, 'EXECUTED', 'PAYMENT', NULL, 1);