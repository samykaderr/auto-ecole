package com.autoecole.security;

import com.autoecole.user.Role;

/**
 * Identité authentifiée, portée par le JWT : sub = id utilisateur, claims = tenant + rôle.
 */
public record UserPrincipal(Long id, Long schoolId, String email, Role role) {
}
