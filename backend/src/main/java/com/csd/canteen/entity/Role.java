package com.csd.canteen.entity;

/**
 * Application roles. Kept intentionally small and specific to CSD canteen
 * operations rather than generic "USER/ADMIN" — each maps to a real desk.
 *
 *  ADMIN               - full access, incl. user management
 *  CANTEEN_MANAGER      - full operational access (no user management)
 *  STORE_SUPERVISOR     - inventory & imports read/write; everything else read-only
 *  FINANCE_REVIEWER     - expenses & approvals read/write; everything else read-only
 *  AUDITOR               - read-only everywhere
 */
public enum Role {
    ADMIN,
    CANTEEN_MANAGER,
    STORE_SUPERVISOR,
    FINANCE_REVIEWER,
    AUDITOR
}
