package br.infnet.continuum.control.event;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EventoHistorico> listar(@RequestParam(required = false) ImportanciaEvento importancia,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.listar(importancia, de, ate);
    }

    @GetMapping("/{id}")
    public EventoHistorico buscar(@PathVariable UUID id) {
        return service.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR','SUPERVISOR','ADMINISTRADOR')")
    public ResponseEntity<EventoHistorico> criar(@Valid @RequestBody EventoService.CreateEvento req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }
}
