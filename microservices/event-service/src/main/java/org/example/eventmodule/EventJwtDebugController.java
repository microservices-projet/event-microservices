package org.example.eventmodule;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * STEP 6 — Keycloak JWT debug: exposes {@code realm_access} from the bearer token
 * (USER / ADMIN roles). Placed under {@code /api/events/me/...} to avoid collision with {@code /{id}}.
 */
@RestController
@RequestMapping("/api/events/me")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class EventJwtDebugController {

    @GetMapping("/roles")
    public ResponseEntity<Object> roles(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Object realmAccess = jwt.getClaim("realm_access");
        return ResponseEntity.ok(realmAccess != null ? realmAccess : Map.of());
    }
}
