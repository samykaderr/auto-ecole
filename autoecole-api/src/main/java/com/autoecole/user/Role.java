package com.autoecole.user;

/**
 * Rôles applicatifs (doc de cadrage §2).
 * SUPER_ADMIN = l'opérateur de la plateforme (multi-tenant), pas un utilisateur d'école.
 */
public enum Role {
    OWNER,
    SECRETARY,
    INSTRUCTOR,
    STUDENT,
    SUPER_ADMIN
}
