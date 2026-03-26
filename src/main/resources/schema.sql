-- Limpieza de tablas (Orden inverso para evitar conflictos de Foreign Keys)
DROP TABLE IF EXISTS "MoneyMovement";
DROP TABLE IF EXISTS "Invoice";
DROP TABLE IF EXISTS "Inscription";
DROP TABLE IF EXISTS "Teacher_FormativeAction";
DROP TABLE IF EXISTS "Fee";
DROP TABLE IF EXISTS "FormativeAction";
DROP TABLE IF EXISTS "Professional";
DROP TABLE IF EXISTS "Teacher";
DROP TABLE IF EXISTS "Community";

-- 1. Tablas Independientes
CREATE TABLE "Community" (
    "community_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "communityName" TEXT NOT NULL UNIQUE
);

CREATE TABLE "Teacher" (
    "teacher_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "fiscal_id" TEXT NOT NULL UNIQUE,
    "email" TEXT NOT NULL UNIQUE,
    "phone" TEXT NOT NULL UNIQUE
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
    "status" TEXT NOT NULL,
    "creationDate" TEXT 
);

-- 2. Tablas con dependencias
CREATE TABLE "Professional" (
    "professional_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "surname" TEXT NOT NULL,
    "phone" TEXT NOT NULL UNIQUE,
    "email" TEXT NOT NULL UNIQUE,
    "community_id" INTEGER,
    FOREIGN KEY("community_id") REFERENCES "Community"("community_id")
);

CREATE TABLE "Fee" (
    "fee_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "amount" REAL NOT NULL,
    "community_id" INTEGER,
    "action_id" INTEGER NOT NULL,
    FOREIGN KEY("community_id") REFERENCES "Community"("community_id"),
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id")
);

CREATE TABLE "Teacher_FormativeAction" (
    "id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "remuneration" REAL NOT NULL,
    "status" TEXT,
    "action_id" INTEGER NOT NULL,
    "teacher_id" INTEGER NOT NULL,
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id"),
    FOREIGN KEY("teacher_id") REFERENCES "Teacher"("teacher_id"),
    UNIQUE("action_id", "teacher_id")
);

CREATE TABLE "Inscription" (
    "inscription_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "inscription_date" TEXT NOT NULL,
    "applied_fee" REAL NOT NULL,
    "state" TEXT NOT NULL,
    "cancellation_date" TEXT,
    "professional_id" INTEGER NOT NULL,
    "action_id" INTEGER NOT NULL,
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id"),
    FOREIGN KEY("professional_id") REFERENCES "Professional"("professional_id")
);

CREATE TABLE "Invoice" (
    "invoice_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "invoice_date" TEXT NOT NULL,
    "netAmount" REAL NOT NULL,
    "vat" REAL NOT NULL,
    "totalAmount" REAL NOT NULL,
    "status" TEXT,
    "teacher_id" INTEGER NOT NULL,
    "action_id" INTEGER NOT NULL,
    FOREIGN KEY("action_id") REFERENCES "FormativeAction"("action_id"),
    FOREIGN KEY("teacher_id") REFERENCES "Teacher"("teacher_id")
);

CREATE TABLE "MoneyMovement" (
    "movement_id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "movement_date" TEXT NOT NULL,
    "amount" REAL NOT NULL,
    "status" TEXT NOT NULL,
    "type" TEXT,
    "inscription_id" INTEGER,
    "invoice_id" INTEGER,
    FOREIGN KEY("inscription_id") REFERENCES "Inscription"("inscription_id"),
    FOREIGN KEY("invoice_id") REFERENCES "Invoice"("invoice_id")
);