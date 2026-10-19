package br.infnet.continuum.control.agent;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/agentes")
public class AgenteController {

    private final AgenteService service;

    public AgenteController(AgenteService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Agente> listar(@RequestParam(required = false) SituacaoAgente situacao,
                               @RequestParam(required = false) String especialidade,
                               @PageableDefault(size = 20) Pageable pageable) {
        return service.listar(situacao, especialidade, pageable);
    }

    @GetMapping("/disponiveis")
    public Page<Agente> disponiveis(@PageableDefault(size = 20) Pageable pageable) {
        return service.listar(SituacaoAgente.DISPONIVEL, null, pageable);
    }

    @GetMapping("/{id}")
    public Agente buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public ResponseEntity<Agente> criar(@Valid @RequestBody AgenteService.CreateAgente req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
    public Agente atualizar(@PathVariable UUID id, @RequestBody AgenteService.UpdateAgente req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
