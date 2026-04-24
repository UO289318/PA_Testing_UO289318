-- 1. Tablas Independientes (Comunidades, Profesores y Profesionales)
INSERT INTO "Community" ("community_id", "communityName") VALUES
(1, 'College Members'),
(2, 'Uniovi Students'),
(3, 'General Public');

INSERT INTO "Teacher" ("teacher_id", "name", "fiscal_id", "email", "phone") VALUES
(1, 'Claudio', '11111111A', 'claudio@coiipa.com', '600111111'),
(2, 'Raquel', '22222222B', 'raquel@coiipa.com', '600222222');

INSERT INTO "Professional" ("professional_id", "name", "surname", "phone", "email", "community_id") VALUES
(1, 'Alice', 'Martínez', '611223344', 'alice@test.com', 3),
(2, 'Marta', 'Rodriguez', '611223355', 'marta@test.com', 3),
(3, 'Fanjul', 'García', '611223366', 'fanjul@test.com', 1),
(4, 'Mateo', 'Martínez', '611223377', 'mateo@test.com', 1);

-- 2. Acciones Formativas
INSERT INTO "FormativeAction" ("action_id", "name", "objectives", "mainContents", "spots", "startDate", "endDate", "numberOfHours", "inscriptionPeriodStart", "inscriptionPeriodEnd", "location", "status", "creationDate") VALUES
(1, 'New Testing Techniques', 'Learn new testing techniques', 'TDD, BDD, JUnit', 3, '2025-09-01', '2025-09-01', '8', '2025-07-01', '2025-07-31', 'Online', 'ACTIVE', '2025-01-01'),
(2, 'Neo4J Database Administration', 'Databases basics', 'Neo4J, Cypher', 4, '2026-04-20', '2026-04-24', '24', '2026-02-23', '2026-03-31', 'Aulario Sur', 'ACTIVE', '2026-01-01'),
(3, 'Neo4J Database Administration', 'Databases basics', 'Neo4J, Cypher', 4, '2026-12-01', '2026-12-04', '24', '2026-11-01', '2026-11-30', 'Aulario Sur', 'ACTIVE', '2026-04-23'),
(4, 'MongoDB Database Administration', 'NoSQL DB', 'MongoDB', 4, '2026-12-05', '2026-12-09', '24', '2026-11-01', '2026-11-30', 'Aulario Sur', 'ACTIVE', '2026-04-23');

-- 3. Tasas (Fees) asociadas a las acciones y comunidades
INSERT INTO "Fee" ("amount", "community_id", "action_id") VALUES
(200.00, 3, 1), 
(150.00, 1, 2), 
(300.00, 3, 2),
(150.00, 1, 3),
(300.00, 3, 3),
(150.00, 1, 4),
(300.00, 3, 4);

-- 4. Relación Profesores y Remuneración (Soporta múltiples profesores)
INSERT INTO "Teacher_FormativeAction" ("remuneration", "status", "action_id", "teacher_id") VALUES
(500.00, 'PAID', 1, 1),    
(200.00, 'PENDING', 2, 2), 
(100.00, 'PENDING', 2, 1),

(200.00, 'PENDING', 3, 2), 
(100.00, 'PENDING', 3, 1),
(200.00, 'PENDING', 4, 2), 
(100.00, 'PENDING', 4, 1);

-- 5. Inscripciones
INSERT INTO "Inscription" ("inscription_id", "inscription_date", "applied_fee", "state", "professional_id", "action_id") VALUES
(1, '2025-07-15', 200.00, 'CONFIRMED', 1, 1),
(2, '2025-07-30', 200.00, 'CONFIRMED', 2, 1),
(3, '2026-02-23', 300.00, 'CONFIRMED', 2, 2),
(4, '2026-02-25', 150.00, 'RECEIVED', 3, 2), 
(5, '2026-03-05', 150.00, 'RECEIVED', 4, 2); 

-- 6. Facturas (Invoices) de Profesores
INSERT INTO "Invoice" ("invoice_id", "invoice_date", "netAmount", "vat", "totalAmount", "status", "teacher_id", "action_id") VALUES
(1, '2025-09-03', 413.22, 86.78, 500.00, 'PAID', 1, 1);

-- 7. Movimientos de Dinero (Income & Expense)
-- Income: Pagos de Alumnos
INSERT INTO "MoneyMovement" ("movement_date", "amount", "status", "type", "inscription_id", "invoice_id") VALUES
('2025-07-16', 200.00, 'EXECUTED', 'PAYMENT', 1, NULL), 
('2025-08-01', 200.00, 'EXECUTED', 'PAYMENT', 2, NULL), 
('2026-02-24', 400.00, 'EXECUTED', 'PAYMENT', 3, NULL), 
('2026-03-01', 100.00, 'EXECUTED', 'PAYMENT', 4, NULL); 

-- Expense: Pago de factura a profesor
INSERT INTO "MoneyMovement" ("movement_date", "amount", "status", "type", "inscription_id", "invoice_id") VALUES
('2025-09-05', -500.00, 'EXECUTED', 'PAYMENT', NULL, 1); 
