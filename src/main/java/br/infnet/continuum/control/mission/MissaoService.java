package br.infnet.continuum.control.mission;

import br.infnet.continuum.control.agent.Agente;
import br.infnet.continuum.control.agent.AgenteRepository;
import br.infnet.continuum.control.agent.SituacaoAgente;
import br.infnet.continuum.control.anomaly.Anomalia;
import br.infnet.continuum.control.anomaly.AnomaliaRepository;
import br.infnet.continuum.control.anomaly.RiscoAnomalia;
import br.infnet.continuum.control.anomaly.SituacaoAnomalia;
import br.infnet.continuum.control.common.exception.BusinessException;
import br.infnet.continuum.control.common.exception.ResourceNotFoundException;
import br.infnet.continuum.control.occurrence.HistoricoAnomaliaDoc;
import br.infnet.continuum.control.occurrence.HistoricoRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MissaoService {

    public record CreateMissao(@NotBlank String codigo, @NotBlank String objetivo,
                               @NotNull UUID anomaliaId, String destinoTemporal,
                               @NotNull PrioridadeMissao prioridade) {}
    public record AutorizarReq(@NotBlank String responsavel, String observacao) {}
    public record ConcluirReq(@NotBlank String resultado, String resumo,
                              String impactoObservado, SituacaoAnomalia situacaoFinalAnomalia, String observacoes) {}

    private final MissaoRepository missoes;
    private final AnomaliaRepository anomalias;
    private final AgenteRepository agentes;
    private final AutorizacaoRepository autorizacoes;
    private final IntervencaoRepository intervencoes;
    private final EncerramentoRepository encerramentos;
    private final HistoricoRepository historico;
    private final StringRedisTemplate redis;
    private final Counter iniciadas;
    private final Counter concluidas;
    private final Counter falhas;

    public MissaoService(MissaoRepository missoes, AnomaliaRepository anomalias, AgenteRepository agentes,
                         AutorizacaoRepository autorizacoes, IntervencaoRepository intervencoes,
                         EncerramentoRepository encerramentos, HistoricoRepository historico,
                         StringRedisTemplate redis, MeterRegistry metrics) {
        this.missoes = missoes;
        this.anomalias = anomalias;
        this.agentes = agentes;
        this.autorizacoes = autorizacoes;
        this.intervencoes = intervencoes;
        this.encerramentos = encerramentos;
        this.historico = historico;
        this.redis = redis;
        this.iniciadas = metrics.counter("continuum.missoes.iniciadas");
        this.concluidas = metrics.counter("continuum.missoes.concluidas");
        this.falhas = metrics.counter("continuum.missoes.falhas");
    }

    public Missao criar(CreateMissao req, String ator) {
        Anomalia anomalia = anomalias.findById(req.anomaliaId())
                .orElseThrow(() -> new ResourceNotFoundException("Anomalia não encontrada"));
        if (anomalia.getSituacao() == SituacaoAnomalia.IRREVERSIVEL) {
            throw new BusinessException("Anomalia irreversível não pode receber novas missões de correção");
        }
        Missao m = new Missao(req.codigo(), req.objetivo(), anomalia, req.destinoTemporal(), req.prioridade());
        Missao salva = missoes.save(m);
        historico.save(new HistoricoAnomaliaDoc(anomalia.getId().toString(), "MISSAO_CRIADA", ator,
                Map.of("missaoId", salva.getId().toString(), "codigo", salva.getCodigo())));
        invalidateCache();
        return salva;
    }

    public Page<Missao> listar(SituacaoMissao situacao, UUID anomaliaId, UUID agenteId, Pageable pageable) {
        if (agenteId != null) return missoes.findByAgentes_Id(agenteId, pageable);
        if (situacao != null) return missoes.findBySituacao(situacao, pageable);
        if (anomaliaId != null) return missoes.findByAnomalia_Id(anomaliaId, pageable);
        return missoes.findAll(pageable);
    }

    public Page<Missao> emExecucao(Pageable pageable) {
        return missoes.findBySituacao(SituacaoMissao.EM_EXECUCAO, pageable);
    }

    public Missao buscar(UUID id) {
        return missoes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada"));
    }

    @Transactional
    public Missao designarAgente(UUID missaoId, UUID agenteId) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() == SituacaoMissao.CONCLUIDA
                || m.getSituacao() == SituacaoMissao.CANCELADA
                || m.getSituacao() == SituacaoMissao.FALHOU) {
            throw new BusinessException("Missão encerrada não pode receber agentes");
        }
        Agente a = agentes.findById(agenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Agente não encontrado"));
        if (a.getSituacao() != SituacaoAgente.DISPONIVEL) {
            throw new BusinessException("Somente agentes disponíveis podem ser designados");
        }
        // impede agente em outra missão em execução
        boolean emOutra = missoes.findBySituacao(SituacaoMissao.EM_EXECUCAO).stream()
                .anyMatch(outra -> outra.getAgentes().stream().anyMatch(ag -> ag.getId().equals(agenteId)));
        if (emOutra) {
            throw new BusinessException("Agente já está em outra missão em execução");
        }
        m.getAgentes().add(a);
        return missoes.save(m);
    }

    @Transactional
    public Missao autorizar(UUID missaoId, AutorizarReq req, String ator) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() != SituacaoMissao.PLANEJADA) {
            throw new BusinessException("Somente missões planejadas podem ser autorizadas");
        }
        autorizacoes.save(new Autorizacao(m, req.responsavel(), req.observacao()));
        m.setSituacao(SituacaoMissao.AUTORIZADA);
        Missao salva = missoes.save(m);
        historico.save(new HistoricoAnomaliaDoc(m.getAnomalia().getId().toString(), "AUTORIZACAO", ator,
                Map.of("missaoId", m.getId().toString(), "responsavel", req.responsavel())));
        invalidateCache();
        return salva;
    }

    private boolean exigeAutorizacao(Missao m) {
        return m.getPrioridade() == PrioridadeMissao.EMERGENCIA
                || m.getAnomalia().getRisco() == RiscoAnomalia.CRITICO;
    }

    @Transactional
    public Missao iniciar(UUID missaoId, String ator) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() != SituacaoMissao.PLANEJADA && m.getSituacao() != SituacaoMissao.AUTORIZADA) {
            throw new BusinessException("Missão só pode ser iniciada a partir de PLANEJADA ou AUTORIZADA");
        }
        if (exigeAutorizacao(m) && m.getSituacao() != SituacaoMissao.AUTORIZADA) {
            throw new BusinessException("Missão de emergência/crítica exige autorização antes da execução");
        }
        if (m.getAgentes().isEmpty()) {
            throw new BusinessException("Missão não pode ser iniciada sem ao menos um agente");
        }
        for (Agente a : m.getAgentes()) {
            Agente fresh = agentes.findById(a.getId()).orElseThrow();
            if (fresh.getSituacao() == SituacaoAgente.SUSPENSO || fresh.getSituacao() == SituacaoAgente.INATIVO) {
                throw new BusinessException("Agentes suspensos ou inativos não podem iniciar missões");
            }
            if (fresh.getSituacao() != SituacaoAgente.DISPONIVEL) {
                throw new BusinessException("Agente " + fresh.getCodinome() + " não está disponível");
            }
        }
        // transição atômica: missão + agentes + anomalia
        m.setSituacao(SituacaoMissao.EM_EXECUCAO);
        missoes.save(m);
        for (Agente a : m.getAgentes()) {
            Agente fresh = agentes.findById(a.getId()).orElseThrow();
            fresh.setSituacao(SituacaoAgente.EM_MISSAO);
            agentes.save(fresh);
        }
        Anomalia an = m.getAnomalia();
        if (an.getSituacao() == SituacaoAnomalia.CONFIRMADA
                || an.getSituacao() == SituacaoAnomalia.DETECTADA
                || an.getSituacao() == SituacaoAnomalia.EM_ANALISE) {
            an.setSituacao(SituacaoAnomalia.EM_CORRECAO);
            anomalias.save(an);
        }
        historico.save(new HistoricoAnomaliaDoc(an.getId().toString(), "MISSAO_INICIADA", ator,
                Map.of("missaoId", m.getId().toString())));
        iniciadas.increment();
        invalidateCache();
        return m;
    }

    @Transactional
    public Missao concluir(UUID missaoId, ConcluirReq req, String ator) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() != SituacaoMissao.EM_EXECUCAO) {
            throw new BusinessException("Somente missões em execução podem ser concluídas");
        }
        m.setSituacao(SituacaoMissao.CONCLUIDA);
        missoes.save(m);
        encerramentos.save(new Encerramento(m, req.resultado(), req.resumo(),
                req.impactoObservado(), req.situacaoFinalAnomalia(), req.observacoes()));
        if (req.situacaoFinalAnomalia() != null) {
            Anomalia an = m.getAnomalia();
            an.setSituacao(req.situacaoFinalAnomalia());
            anomalias.save(an);
        }
        liberarAgentes(m, false);
        historico.save(new HistoricoAnomaliaDoc(m.getAnomalia().getId().toString(), "MISSAO_CONCLUIDA", ator,
                Map.of("missaoId", m.getId().toString(), "resultado", req.resultado())));
        concluidas.increment();
        invalidateCache();
        return m;
    }

    @Transactional
    public Missao falhar(UUID missaoId, ConcluirReq req, String ator) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() != SituacaoMissao.EM_EXECUCAO) {
            throw new BusinessException("Somente missões em execução podem falhar");
        }
        m.setSituacao(SituacaoMissao.FALHOU);
        missoes.save(m);
        encerramentos.save(new Encerramento(m, req.resultado(), req.resumo(),
                req.impactoObservado(), null, req.observacoes()));
        // falha não auto-resolve a anomalia
        liberarAgentes(m, true);
        historico.save(new HistoricoAnomaliaDoc(m.getAnomalia().getId().toString(), "MISSAO_FALHOU", ator,
                Map.of("missaoId", m.getId().toString())));
        falhas.increment();
        invalidateCache();
        return m;
    }

    @Transactional
    public Missao cancelar(UUID missaoId, String ator) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() == SituacaoMissao.CONCLUIDA
                || m.getSituacao() == SituacaoMissao.CANCELADA
                || m.getSituacao() == SituacaoMissao.FALHOU) {
            throw new BusinessException("Missão já encerrada não pode ser cancelada");
        }
        m.setSituacao(SituacaoMissao.CANCELADA);
        missoes.save(m);
        liberarAgentes(m, false);
        historico.save(new HistoricoAnomaliaDoc(m.getAnomalia().getId().toString(), "MISSAO_CANCELADA", ator,
                Map.of("missaoId", m.getId().toString())));
        invalidateCache();
        return m;
    }

    private void liberarAgentes(Missao m, boolean aposFalha) {
        for (Agente a : m.getAgentes()) {
            Agente fresh = agentes.findById(a.getId()).orElseThrow();
            if (fresh.getSituacao() == SituacaoAgente.EM_MISSAO) {
                fresh.setSituacao(SituacaoAgente.DISPONIVEL);
                agentes.save(fresh);
            }
        }
    }

    @Transactional
    public Intervencao intervir(UUID missaoId, UUID agenteId, String acao, String justificativa,
                                ImpactoIntervencao impacto, String resultado, String ator) {
        Missao m = buscar(missaoId);
        if (m.getSituacao() != SituacaoMissao.EM_EXECUCAO) {
            throw new BusinessException("Somente missões em execução podem receber intervenções");
        }
        Agente a = agentes.findById(agenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Agente não encontrado"));
        boolean participante = m.getAgentes().stream().anyMatch(ag -> ag.getId().equals(agenteId));
        if (!participante) {
            throw new BusinessException("Agente não participa desta missão");
        }
        Intervencao interv = intervencoes.save(new Intervencao(m, a, acao, justificativa, impacto, resultado));
        historico.save(new HistoricoAnomaliaDoc(m.getAnomalia().getId().toString(), "INTERVENCAO", ator,
                Map.of("missaoId", m.getId().toString(), "impacto", impacto.name())));
        invalidateCache();
        return interv;
    }

    private void invalidateCache() {
        try {
            redis.delete("stability:index");
            redis.delete("ops:overview");
        } catch (Exception ignored) {}
    }
}
