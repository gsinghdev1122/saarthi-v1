-- Core operational schema for Canteen SAARTHI.
-- Mirrors the original Drizzle/Postgres schema from the Node prototype 1:1.

CREATE TABLE imports (
    id           BIGSERIAL PRIMARY KEY,
    filename     TEXT NOT NULL,
    file_type    TEXT NOT NULL,
    canteen      TEXT NOT NULL,
    status       TEXT NOT NULL DEFAULT 'processed',
    row_count    INTEGER NOT NULL DEFAULT 0,
    message      TEXT,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE inventory (
    id             BIGSERIAL PRIMARY KEY,
    index_no       TEXT NOT NULL,
    name           TEXT NOT NULL,
    division       TEXT NOT NULL,
    closing_stock  INTEGER NOT NULL DEFAULT 0,
    reorder_level  INTEGER NOT NULL DEFAULT 10,
    value          NUMERIC(12,2) NOT NULL DEFAULT 0,
    trend          TEXT NOT NULL DEFAULT 'stable',
    status         TEXT NOT NULL DEFAULT 'healthy',
    canteen        TEXT NOT NULL DEFAULT 'Delhi Cantt'
);

CREATE TABLE employees (
    id             BIGSERIAL PRIMARY KEY,
    name           TEXT NOT NULL,
    employee_code  TEXT NOT NULL UNIQUE,
    category       TEXT NOT NULL,
    designation    TEXT NOT NULL,
    attendance     NUMERIC(5,2) NOT NULL DEFAULT 0,
    contract_end   DATE,
    status         TEXT NOT NULL DEFAULT 'active',
    canteen        TEXT NOT NULL DEFAULT 'Delhi Cantt'
);

CREATE TABLE expenses (
    id            BIGSERIAL PRIMARY KEY,
    category      TEXT NOT NULL,
    vendor        TEXT NOT NULL,
    amount        NUMERIC(12,2) NOT NULL,
    date          DATE NOT NULL,
    status        TEXT NOT NULL DEFAULT 'draft',
    submitted_by  TEXT NOT NULL DEFAULT 'Canteen Manager',
    canteen       TEXT NOT NULL DEFAULT 'Delhi Cantt'
);

CREATE TABLE approvals (
    id            BIGSERIAL PRIMARY KEY,
    type          TEXT NOT NULL,
    reference     TEXT NOT NULL,
    amount        NUMERIC(12,2) NOT NULL DEFAULT 0,
    submitted_by  TEXT NOT NULL,
    submitted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    status        TEXT NOT NULL DEFAULT 'pending'
);

CREATE TABLE activity (
    id         BIGSERIAL PRIMARY KEY,
    title      TEXT NOT NULL,
    detail     TEXT NOT NULL,
    kind       TEXT NOT NULL,
    timestamp  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_division ON inventory (division);
CREATE INDEX idx_inventory_status ON inventory (status);
CREATE INDEX idx_expenses_date ON expenses (date DESC);
CREATE INDEX idx_approvals_status ON approvals (status);
CREATE INDEX idx_activity_timestamp ON activity (timestamp DESC);
CREATE INDEX idx_imports_uploaded_at ON imports (uploaded_at DESC);
