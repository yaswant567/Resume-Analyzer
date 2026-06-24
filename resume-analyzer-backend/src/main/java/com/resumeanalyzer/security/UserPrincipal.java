package com.resumeanalyzer.security;

import java.util.UUID;

/**
 * Lightweight authenticated-user principal placed in the SecurityContext.
 * Avoids an extra DB lookup on every request just to know "who is this".
 */
public record UserPrincipal(UUID id, String email) {
}
