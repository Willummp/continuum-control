package br.infnet.continuum.control.event;

import br.infnet.continuum.control.common.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EventoService {

    public record CreateEvento(@NotBlank String titulo, String descricao,
                               @NotNull LocalDate dataEvento, String localizacao,
                               @NotNull ImportanciaEvento importancia) {}

    private final EventoRepository eventos;

    public EventoService(EventoRepository eventos) {
        this.eventos = eventos;
    }

    public EventoHistorico criar(CreateEvento req) {
        return eventos.save(new EventoHistorico(req.titulo(), req.descricao(),
                req.dataEvento(), req.localizacao(), req.importancia()));
    }

    public List<EventoHistorico> listar(ImportanciaEvento importancia, LocalDate de, LocalDate ate) {
        if (importancia != null) return eventos.findByImportancia(importancia);
        if (de != null && ate != null) return eventos.findByDataEventoBetween(de, ate);
        return eventos.findAll();
    }

    public EventoHistorico buscar(UUID id) {
        return eventos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
    }
}
