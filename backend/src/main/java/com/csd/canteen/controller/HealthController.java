package com.csd.canteen.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/** Simple liveness endpoint used by Docker HEALTHCHECK, the Nginx upstream check,
 *  and load balancer health checks in AWS. Separate from Spring Actuator's
 *  /actuator/health so it stays dependency-free and always fast. */
@RestController
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/api/healthz")
    public Map<String, Object> healthz() {
        return Map.of("status", "UP", "timestamp", OffsetDateTime.now().toString());
    }
}
