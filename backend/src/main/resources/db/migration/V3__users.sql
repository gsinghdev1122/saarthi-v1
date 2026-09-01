-- Application users for authentication/RBAC. Seeded at application startup
-- (see UserSeeder.java) rather than here, so the initial admin password is
-- generated via Spring's PasswordEncoder instead of a hardcoded hash in SQL.

CREATE TABLE app_users (
    id             BIGSERIAL PRIMARY KEY,
    username       TEXT NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    display_name   TEXT NOT NULL,
    role           TEXT NOT NULL,
    enabled        BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_app_users_username ON app_users (username);
