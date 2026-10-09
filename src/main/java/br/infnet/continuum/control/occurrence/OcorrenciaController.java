package br.infnet.continuum.control.occurrence;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/missoes/{missaoId}/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService service;

    public OcorrenciaController(OcorrenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<OcorrenciaDoc> listar(@PathVariable UUID missaoId,
                                      @RequestParam(required = false) String tipo) {
        return service.listar(missaoId, tipo);
    }

    @PostMapping
    @PreAuthorize("@missionSecurity.canWrite(#missaoId)")
    public ResponseEntity<OcorrenciaDoc> registrar(@PathVariable UUID missaoId,
                                                   @RequestBody Map<String, Object> body,
                                                   Authentication auth) {
        String tipo = (String) body.get("tipo");
        String descricao = (String) body.get("descricao");
        @SuppressWarnings("unchecked")
        Map<String, Object> dados = (Map<String, Object>) body.getOrDefault("dados", Map.of());
        OcorrenciaDoc doc = service.registrar(missaoId, tipo, descricao, dados,
                auth != null ? auth.getName() : "sistema");
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }
}
