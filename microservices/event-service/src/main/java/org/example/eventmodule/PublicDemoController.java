package org.example.eventmodule;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demo endpoint without JWT — for Postman / cours (STEP 6).
 */
@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class PublicDemoController {

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        return Map.of(
                "message", "OK — no Bearer token required",
                "hint", "Use GET /api/events/me/roles with Authorization: Bearer <token> to see realm_access"
        );
    }
}
