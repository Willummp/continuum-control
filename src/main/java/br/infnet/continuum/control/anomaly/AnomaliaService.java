package br.infnet.continuum.control.anomaly;

import br.infnet.continuum.control.common.exception.BusinessException;
import br.infnet.continuum.control.common.exception.ResourceNotFoundException;
import br.infnet.continuum.control.event.EventoHistorico;
import br.infnet.continuum.control.event.EventoRepository;
import br.infnet.continuum.control.occurrence.HistoricoAnomaliaDoc;
import br.infnet.continuum.control.occurrence.HistoricoRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnomaliaService {

    public record CreateAnomalia(@NotNull UUID eventoId, @NotBlank String divergencia,
                                 @NotNull RiscoAnomalia risco, String origem, String observacoes) {}

    private final AnomaliaRepository anomalias;
    private final EventoRepository eventos;
    private final HistoricoRepository historico;

    public AnomaliaService(AnomaliaRepository anomalias, EventoRepository eventos, HistoricoRepository historico) {
        this.anomalias = anomalias;
        this.eventos = eventos;
        this.historico = historico;
    }

    public Anomalia criar(CreateAnomalia req, String ator) {
        EventoHistorico evento = eventos.findById(req.eventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
        Anomalia a = new Anomalia(evento, req.divergencia(), req.risco());
        a.setOrigem(req.origem());
        a.setObservacoes(req.observacoes());
        Anomalia salva = anomalias.save(a);
        historico.save(new HistoricoAnomaliaDoc(salva.getId().toString(), "ANOMALIA_DETECTADA", ator,
                Map.of("risco", salva.getRisco().name(), "eventoId", evento.getId().toString())));
        return salva;
    }

    public Page<Anomalia> listar(SituacaoAnomalia situacao, RiscoAnomalia risco, Pageable pageable) {
        if (situacao != null) return anomalias.findBySituacao(situacao, pageable);
        if (risco != null) return anomalias.findByRisco(risco, pageable);
        return anomalias.findAll(pageable);
    }

    public List<Anomalia> ativas() {
        return anomalias.findAtivas();
    }

    public List<Anomalia> criticas() {
        return anomalias.findByRisco(RiscoAnomalia.CRITICO).stream()
                .filter(a -> a.getSituacao() != SituacaoAnomalia.ESTABILIZADA
                        && a.getSituacao() != SituacaoAnomalia.IRREVERSIVEL)
                .toList();
    }

    public Anomalia buscar(UUID id) {
        return anomalias.findById(id).orElseThrow(() -> new ResourceNotFoundException("Anomalia não encontrada"));
    }

    public Anomalia mudarSituacao(UUID id, SituacaoAnomalia nova, String ator) {
        Anomalia a = buscar(id);
        a.setSituacao(nova);
        Anomalia salva = anomalias.save(a);
        historico.save(new HistoricoAnomaliaDoc(id.toString(), "MUDANCA_SITUACAO", ator,
                Map.of("nova", nova.name())));
        return salva;
    }

    public Anomalia mudarRisco(UUID id, RiscoAnomalia novo, String ator) {
        Anomalia a = buscar(id);
        if (a.getSituacao() == SituacaoAnomalia.IRREVERSIVEL) {
            throw new BusinessException("Anomalia irreversível não pode ter risco alterado para nova correção");
        }
        a.setRisco(novo);
        Anomalia salva = anomalias.save(a);
        historico.save(new HistoricoAnomaliaDoc(id.toString(), "MUDANCA_RISCO", ator,
                Map.of("novo", novo.name())));
        return salva;
    }
}
