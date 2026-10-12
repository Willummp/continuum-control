package br.infnet.continuum.control.stability;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OpsController {

    private final StabilityService stability;

    public OpsController(StabilityService stability) {
        this.stability = stability;
    }

    @GetMapping("/ops/overview")
    public Map<String, Object> overview() {
        return stability.overview();
    }

    @GetMapping("/stability")
    public Map<String, Object> stability() {
        return stability.stability();
    }
}
