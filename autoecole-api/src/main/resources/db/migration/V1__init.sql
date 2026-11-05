-- V1__init.sql — schéma initial (doc de cadrage §7, adaptations mineures : statuts et
-- codes d'annulation en anglais, cohérents avec l'API : CANCELLED_STUDENT, NO_SHOW…)
-- PG 16 : contraintes d'exclusion GiST nécessaires → extension btree_gist.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ===================== Tenant =====================
CREATE TABLE school (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  phone VARCHAR(30), email VARCHAR(150), address TEXT,
  settings JSONB NOT NULL DEFAULT '{}',   -- règles : délai annulation, min. heures examen, etc.
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  email VARCHAR(150) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER','SECRETARY','INSTRUCTOR','STUDENT','SUPER_ADMIN')),
  active BOOLEAN NOT NULL DEFAULT true,
  UNIQUE (school_id, email)
);

-- ===================== Référentiels =====================
CREATE TABLE license_category (          -- référentiel global (non multi-tenant)
  id SMALLINT PRIMARY KEY,
  code VARCHAR(10) NOT NULL UNIQUE,      -- B, A, C, D...
  label VARCHAR(80) NOT NULL,
  min_driving_hours SMALLINT NOT NULL DEFAULT 0
);

-- ===================== Élèves / Moniteurs / Véhicules =====================
CREATE TABLE student (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  user_id BIGINT REFERENCES app_user(id),
  first_name VARCHAR(80) NOT NULL, last_name VARCHAR(80) NOT NULL,
  birth_date DATE, phone VARCHAR(30), email VARCHAR(150), address TEXT,
  id_number VARCHAR(50), photo_url TEXT,
  emergency_contact VARCHAR(150),
  status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE instructor (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  user_id BIGINT REFERENCES app_user(id),
  first_name VARCHAR(80) NOT NULL, last_name VARCHAR(80) NOT NULL,
  phone VARCHAR(30), license_number VARCHAR(50),
  categories VARCHAR(10)[] NOT NULL DEFAULT '{}',
  active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE vehicle (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  plate VARCHAR(20) NOT NULL, brand VARCHAR(50), model VARCHAR(50),
  category_code VARCHAR(10) NOT NULL,
  transmission VARCHAR(10) DEFAULT 'MANUAL',
  insurance_expiry DATE, inspection_expiry DATE,
  active BOOLEAN NOT NULL DEFAULT true,
  UNIQUE (school_id, plate)
);

CREATE TABLE instructor_availability (   -- récurrente (doc §3.2)
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  instructor_id BIGINT NOT NULL REFERENCES instructor(id),
  day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
  start_time TIME NOT NULL, end_time TIME NOT NULL,
  CHECK (end_time > start_time)
);

CREATE TABLE time_off (                  -- congés moniteur OU indispo véhicule
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  instructor_id BIGINT REFERENCES instructor(id),
  vehicle_id BIGINT REFERENCES vehicle(id),
  period TSTZRANGE NOT NULL,
  reason VARCHAR(200),
  CHECK ((instructor_id IS NULL) <> (vehicle_id IS NULL))
);

-- ===================== Forfaits =====================
CREATE TABLE package (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  name VARCHAR(120) NOT NULL,
  category_code VARCHAR(10) NOT NULL,
  driving_hours NUMERIC(5,1) NOT NULL,
  code_sessions SMALLINT NOT NULL DEFAULT 0,
  price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  active BOOLEAN NOT NULL DEFAULT true
);

-- Dossier d'un élève (un élève peut avoir plusieurs dossiers : B puis A)
CREATE TABLE enrollment (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  student_id BIGINT NOT NULL REFERENCES student(id),
  package_id BIGINT NOT NULL REFERENCES package(id),
  category_code VARCHAR(10) NOT NULL,
  price NUMERIC(12,2) NOT NULL,          -- copie figée du prix au moment de l'inscription
  hours_included NUMERIC(5,1) NOT NULL,
  status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE',
  enrolled_on DATE NOT NULL DEFAULT current_date
);

-- ===================== Leçons =====================
-- Anti-chevauchement garanti par la base (doc §4.1 : "zéro chevauchement").
CREATE TABLE lesson (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  enrollment_id BIGINT NOT NULL REFERENCES enrollment(id),
  student_id BIGINT NOT NULL REFERENCES student(id),
  instructor_id BIGINT NOT NULL REFERENCES instructor(id),
  vehicle_id BIGINT REFERENCES vehicle(id),     -- null pour une séance de code
  lesson_type VARCHAR(15) NOT NULL CHECK (lesson_type IN ('DRIVING','CODE','CIRCUIT')),
  period TSTZRANGE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  -- un moniteur / un véhicule / un élève ne peut pas avoir 2 leçons actives qui se chevauchent
  EXCLUDE USING gist (instructor_id WITH =, period WITH &&)
    WHERE (status IN ('PLANNED','DONE')),
  EXCLUDE USING gist (vehicle_id WITH =, period WITH &&)
    WHERE (status IN ('PLANNED','DONE') AND vehicle_id IS NOT NULL),
  EXCLUDE USING gist (student_id WITH =, period WITH &&)
    WHERE (status IN ('PLANNED','DONE'))
);

-- ===================== Examens =====================
CREATE TABLE exam (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  enrollment_id BIGINT NOT NULL REFERENCES enrollment(id),
  exam_type VARCHAR(15) NOT NULL CHECK (exam_type IN ('CODE','CRENEAU','DRIVING')),
  attempt_no SMALLINT NOT NULL DEFAULT 1,
  scheduled_at TIMESTAMPTZ NOT NULL,
  location VARCHAR(200),
  result VARCHAR(10) CHECK (result IN ('PASSED','FAILED','ABSENT')),
  score SMALLINT, comment TEXT
);

-- ===================== Échéancier & paiements =====================
CREATE TABLE installment (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  enrollment_id BIGINT NOT NULL REFERENCES enrollment(id),
  seq_no SMALLINT NOT NULL,
  due_date DATE NOT NULL,
  amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
  status VARCHAR(10) NOT NULL DEFAULT 'UPCOMING',   -- UPCOMING / PARTIAL / PAID / OVERDUE
  UNIQUE (enrollment_id, seq_no)
);

CREATE TABLE payment (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  installment_id BIGINT NOT NULL REFERENCES installment(id),
  receipt_no VARCHAR(30) NOT NULL,
  amount NUMERIC(12,2) NOT NULL,
  method VARCHAR(15) NOT NULL CHECK (method IN ('CASH','CHECK','TRANSFER','CARD','ONLINE')),
  paid_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  received_by BIGINT REFERENCES app_user(id),
  status VARCHAR(10) NOT NULL DEFAULT 'VALID',       -- VALID / CANCELLED (jamais supprimé, doc §4.6)
  cancel_reason TEXT,
  UNIQUE (school_id, receipt_no)
);

-- ===================== Documents / Notifications / Audit =====================
CREATE TABLE document (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  student_id BIGINT NOT NULL REFERENCES student(id),
  doc_type VARCHAR(30) NOT NULL,
  storage_key TEXT NOT NULL,
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notification (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL REFERENCES school(id),
  recipient_student_id BIGINT REFERENCES student(id),
  channel VARCHAR(10) NOT NULL, template VARCHAR(40) NOT NULL,
  payload JSONB, scheduled_for TIMESTAMPTZ NOT NULL,
  sent_at TIMESTAMPTZ, status VARCHAR(10) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE audit_log (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  school_id BIGINT NOT NULL, user_id BIGINT,
  entity VARCHAR(40) NOT NULL, entity_id BIGINT,
  action VARCHAR(20) NOT NULL, details JSONB,
  at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ===================== Index (doc §7.3) =====================
CREATE INDEX idx_lesson_school_period ON lesson USING gist (school_id, period);
-- Relances : tranches "à traiter"
CREATE INDEX idx_installment_due_relance ON installment (due_date)
  WHERE status IN ('UPCOMING','PARTIAL','OVERDUE');
-- B-tree sur les FK tenant pour filtrer par auto-école efficacement
CREATE INDEX idx_app_user_school ON app_user (school_id);
CREATE INDEX idx_student_school ON student (school_id);
CREATE INDEX idx_student_enrollment_idx ON enrollment (student_id);
CREATE INDEX idx_enrollment_school ON enrollment (school_id);
CREATE INDEX idx_instructor_school ON instructor (school_id);
CREATE INDEX idx_vehicle_school ON vehicle (school_id);
CREATE INDEX idx_lesson_enrollment ON lesson (enrollment_id);
CREATE INDEX idx_lesson_student ON lesson (student_id);
CREATE INDEX idx_lesson_instructor ON lesson (instructor_id);
CREATE INDEX idx_exam_enrollment ON exam (enrollment_id);
CREATE INDEX idx_exam_school ON exam (school_id);
CREATE INDEX idx_installment_enrollment ON installment (enrollment_id);
CREATE INDEX idx_payment_installment ON payment (installment_id);
CREATE INDEX idx_payment_school_paid_at ON payment (school_id, paid_at);
CREATE INDEX idx_document_student ON document (student_id);
CREATE INDEX idx_notification_school_status ON notification (school_id, status);
CREATE INDEX idx_audit_log_school_time ON audit_log (school_id, at);
