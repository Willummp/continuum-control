package br.infnet.continuum.control.anomaly;

import br.infnet.continuum.control.occurrence.HistoricoAnomaliaDoc;
import br.infnet.continuum.control.occurrence.HistoricoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/anomalias")
public class AnomaliaController {

    private final AnomaliaService service;
    private final HistoricoRepository historico;

    public AnomaliaController(AnomaliaService service, HistoricoRepository historico) {
        this.service = service;
        this.historico = historico;
    }

    @GetMapping
    public List<Anomalia> listar(@RequestParam(required = false) SituacaoAnomalia situacao,
                                 @RequestParam(required = false) RiscoAnomalia risco) {
        return service.listar(situacao, risco);
    }

    @GetMapping("/ativas")
    public List<Anomalia> ativas() {
        return service.ativas();
    }

    @GetMapping("/criticas")
    public List<Anomalia> criticas() {
        return service.criticas();
    }

    @GetMapping("/{id}")
    public Anomalia buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoAnomaliaDoc> historico(@PathVariable UUID id) {
        service.buscar(id);
        return historico.findByAnomaliaIdOrderByInstanteAsc(id.toString());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR','SUPERVISOR','ADMINISTRADOR')")
    public ResponseEntity<Anomalia> criar(@Valid @RequestBody AnomaliaService.CreateAnomalia req,
                                          Authentication auth) {
        String ator = auth != null ? auth.getName() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req, ator));
    }

    @PatchMapping("/{id}/situacao")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Anomalia situacao(@PathVariable UUID id, @RequestBody java.util.Map<String, String> body,
                             Authentication auth) {
        return service.mudarSituacao(id, SituacaoAnomalia.valueOf(body.get("situacao")),
                auth != null ? auth.getName() : "sistema");
    }

    @PatchMapping("/{id}/risco")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Anomalia risco(@PathVariable UUID id, @RequestBody java.util.Map<String, String> body,
                          Authentication auth) {
        return service.mudarRisco(id, RiscoAnomalia.valueOf(body.get("risco")),
                auth != null ? auth.getName() : "sistema");
    }
}
