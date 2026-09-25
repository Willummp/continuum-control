package br.infnet.continuum.control.mission;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/missoes")
public class MissaoController {

    private final MissaoService service;
    private final IntervencaoRepository intervencoes;

    public MissaoController(MissaoService service, IntervencaoRepository intervencoes) {
        this.service = service;
        this.intervencoes = intervencoes;
    }

    @GetMapping
    public List<Missao> listar(@RequestParam(required = false) SituacaoMissao situacao,
                               @RequestParam(required = false) UUID anomaliaId,
                               @RequestParam(required = false) UUID agenteId) {
        List<Missao> base = service.listar(situacao, anomaliaId);
        if (agenteId != null) {
            return base.stream()
                    .filter(m -> m.getAgentes().stream().anyMatch(a -> a.getId().equals(agenteId)))
                    .toList();
        }
        return base;
    }

    @GetMapping("/em-execucao")
    public List<Missao> emExecucao() {
        return service.emExecucao();
    }

    @GetMapping("/{id}")
    public Missao buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR','OPERADOR')")
    public ResponseEntity<Missao> criar(@Valid @RequestBody MissaoService.CreateMissao req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(req, auth != null ? auth.getName() : "sistema"));
    }

    @PostMapping("/{id}/agentes")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Missao designar(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return service.designarAgente(id, UUID.fromString(body.get("agenteId")));
    }

    @PostMapping("/{id}/autorizacoes")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Missao autorizar(@PathVariable UUID id, @RequestBody MissaoService.AutorizarReq req, Authentication auth) {
        return service.autorizar(id, req, auth != null ? auth.getName() : "sistema");
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Missao iniciar(@PathVariable UUID id, Authentication auth) {
        return service.iniciar(id, auth != null ? auth.getName() : "sistema");
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Missao concluir(@PathVariable UUID id, @RequestBody MissaoService.ConcluirReq req, Authentication auth) {
        return service.concluir(id, req, auth != null ? auth.getName() : "sistema");
    }

    @PostMapping("/{id}/falhar")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Missao falhar(@PathVariable UUID id, @RequestBody MissaoService.ConcluirReq req, Authentication auth) {
        return service.falhar(id, req, auth != null ? auth.getName() : "sistema");
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMINISTRADOR')")
    public Missao cancelar(@PathVariable UUID id, Authentication auth) {
        return service.cancelar(id, auth != null ? auth.getName() : "sistema");
    }

    @PostMapping("/{id}/intervencoes")
    @PreAuthorize("@missionSecurity.canWrite(#id)")
    public ResponseEntity<Intervencao> intervir(@PathVariable UUID id,
                                                @RequestBody Map<String, String> body,
                                                Authentication auth) {
        Intervencao interv = service.intervir(id,
                UUID.fromString(body.get("agenteId")),
                body.get("acao"), body.get("justificativa"),
                ImpactoIntervencao.valueOf(body.get("impacto")),
                body.get("resultado"),
                auth != null ? auth.getName() : "sistema");
        return ResponseEntity.status(HttpStatus.CREATED).body(interv);
    }

    @GetMapping("/{id}/intervencoes")
    public List<Intervencao> intervencoes(@PathVariable UUID id) {
        service.buscar(id);
        return intervencoes.findByMissao_Id(id);
    }
}
