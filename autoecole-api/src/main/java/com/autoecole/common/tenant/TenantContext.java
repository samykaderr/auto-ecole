package com.autoecole.common.tenant;

import java.util.Optional;

/**
 * Identité du tenant (auto-école) pour le thread courant — doc §5.2 et §9 :
 * le school_id est injecté depuis le JWT, JAMAIS depuis le client.
 * Posé par JwtAuthFilter à chaque requête authentifiée.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private TenantContext() { }

    public static void set(Long schoolId) {
        CURRENT.set(schoolId);
    }

    public static Optional<Long> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    /** school_id du tenant courant — requis pour toute requête métier authentifiée. */
    public static Long require() {
        Long value = CURRENT.get();
        if (value == null) {
            throw new IllegalStateException("Aucun tenant dans le contexte (requête non authentifiée ?)");
        }
        return value;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
