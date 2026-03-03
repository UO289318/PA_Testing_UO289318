
--For giis.demo.tkrun:
drop table if exists Carreras;
create table Carreras (idinteger primary key not null, inicio date not null, fin date not null, fecha date not null, descr varchar(32), check(inicio<=fin), check(fin<fecha));

--For the g54.utils DataBases
DROP TABLE IF EXISTS "MoneyMovement";
DROP TABLE IF EXISTS "Payment";
DROP TABLE IF EXISTS "Invoice";
DROP TABLE IF EXISTS "Inscription";
DROP TABLE IF EXISTS "FormativeAction";
DROP TABLE IF EXISTS "Professional";
DROP TABLE IF EXISTS "Teacher";
DROP TABLE IF EXISTS "Teacher_FormativeAction";



CREATE TABLE "Teacher" (
    "teacher_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "fiscal_id" TEXT NOT NULL UNIQUE,
    "email" TEXT NOT NULL UNIQUE,
    "phone" TEXT NOT NULL UNIQUE
);

CREATE TABLE "Professional" (
    "professional_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "surname" TEXT NOT NULL,
    "phone" TEXT NOT NULL UNIQUE,
    "email" TEXT NOT NULL UNIQUE
);

CREATE TABLE "Teacher_FormativeAction" (
    "id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "remuneration" REAL NOT NULL,
    "action_id" INTEGER NOT NULL,
    "teacher_id" INTEGER NOT NULL,
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id"),
    FOREIGN KEY("teacher_id") REFERENCES "Teacher"("teacher_id"),
    UNIQUE("action_id", "teacher_id")
);

CREATE TABLE "FormativeAction" (
    "action_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "objectives" TEXT,
    "mainContents" TEXT,
    "spots" INTEGER NOT NULL,
    "startDate" TEXT NOT NULL,
    "endDate" TEXT NOT NULL,
    "numberOfHours" TEXT NOT NULL,
    "inscriptionPeriodStart" TEXT NOT NULL,
    "inscriptionPeriodEnd" TEXT NOT NULL,
    "location" TEXT NOT NULL,
    "fee" REAL NOT NULL,
    "status" TEXT NOT NULL,
    "initialPayment" REAL,
    "teacher_id" INTEGER NOT NULL,
    FOREIGN KEY("teacher_id") REFERENCES "Teacher"("teacher_id")
);

CREATE TABLE "Inscription" (
    "inscription_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "inscription_date" TEXT NOT NULL,
    "fee" REAL NOT NULL,
    "state" TEXT NOT NULL,
    "professional_id" INTEGER NOT NULL,
    "action_id" INTEGER NOT NULL,
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id"),
    FOREIGN KEY("professional_id") REFERENCES "Professional"("professional_id")
);


CREATE TABLE "Payment" (
    "payment_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "amountPaid" REAL NOT NULL,
    "payment_date" TEXT NOT NULL,
    "inscription_id" INTEGER NOT NULL,
    FOREIGN KEY("inscription_id") REFERENCES "Inscription"("inscription_id")
);

CREATE TABLE "Invoice" (
    "invoice_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "invoice_date" TEXT NOT NULL,
    "netAmount" REAL NOT NULL,
    "vat" REAL NOT NULL,
    "totalAmount" REAL NOT NULL,
    "teacher_id" INTEGER NOT NULL,
    "action_id" INTEGER NOT NULL,
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id"),
    FOREIGN KEY("teacher_id") REFERENCES "Teacher"("teacher_id")
);

CREATE TABLE "MoneyMovement" (
    "movement_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "movement_date" TEXT NOT NULL,
    "amount" REAL NOT NULL,
    "invoice_id" INTEGER NOT NULL,
    FOREIGN KEY("invoice_id") REFERENCES "Invoice"("invoice_id")
);

