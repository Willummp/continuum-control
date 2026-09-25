package br.infnet.continuum.control.stability;

import br.infnet.continuum.control.agent.AgenteRepository;
import br.infnet.continuum.control.agent.SituacaoAgente;
import br.infnet.continuum.control.anomaly.AnomaliaRepository;
import br.infnet.continuum.control.anomaly.RiscoAnomalia;
import br.infnet.continuum.control.anomaly.SituacaoAnomalia;
import br.infnet.continuum.control.mission.IntervencaoRepository;
import br.infnet.continuum.control.mission.ImpactoIntervencao;
import br.infnet.continuum.control.mission.MissaoRepository;
import br.infnet.continuum.control.mission.SituacaoMissao;
import tools.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StabilityService {

    private final AnomaliaRepository anomalias;
    private final MissaoRepository missoes;
    private final AgenteRepository agentes;
    private final IntervencaoRepository intervencoes;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    private final long ttlSeconds;
    private final int pesoCritica;
    private final int pesoAtiva;
    private final int pesoExecucao;
    private final int pesoFalha;
    private final int pesoSevera;
    private final AtomicInteger indiceAtual = new AtomicInteger(100);

    public StabilityService(AnomaliaRepository anomalias, MissaoRepository missoes,
                            AgenteRepository agentes, IntervencaoRepository intervencoes,
                            StringRedisTemplate redis, ObjectMapper mapper, MeterRegistry metrics,
                            @Value("${continuum.stability.ttl-seconds:60}") long ttlSeconds,
                            @Value("${continuum.stability.peso-critica:15}") int pesoCritica,
                            @Value("${continuum.stability.peso-ativa:5}") int pesoAtiva,
                            @Value("${continuum.stability.peso-execucao:3}") int pesoExecucao,
                            @Value("${continuum.stability.peso-falha-7d:10}") int pesoFalha,
                            @Value("${continuum.stability.peso-severa-7d:8}") int pesoSevera) {
        this.anomalias = anomalias;
        this.missoes = missoes;
        this.agentes = agentes;
        this.intervencoes = intervencoes;
        this.redis = redis;
        this.mapper = mapper;
        this.ttlSeconds = ttlSeconds;
        this.pesoCritica = pesoCritica;
        this.pesoAtiva = pesoAtiva;
        this.pesoExecucao = pesoExecucao;
        this.pesoFalha = pesoFalha;
        this.pesoSevera = pesoSevera;
        Gauge.builder("continuum.estabilidade.indice", indiceAtual, AtomicInteger::get)
                .description("Indice de Estabilidade Temporal 0-100")
                .register(metrics);
    }

    public Map<String, Object> overview() {
        try {
            String cached = redis.opsForValue().get("ops:overview");
            if (cached != null) {
                return mapper.readValue(cached, Map.class);
            }
        } catch (Exception ignored) {}
        long disponiveis = agentes.findBySituacao(SituacaoAgente.DISPONIVEL).size();
        long emMissao = agentes.findBySituacao(SituacaoAgente.EM_MISSAO).size();
        long execucao = missoes.findBySituacao(SituacaoMissao.EM_EXECUCAO).size();
        long ativas = anomalias.findAtivas().size();
        long criticas = anomalias.findByRisco(RiscoAnomalia.CRITICO).stream()
                .filter(a -> a.getSituacao() != SituacaoAnomalia.ESTABILIZADA
                        && a.getSituacao() != SituacaoAnomalia.IRREVERSIVEL).count();
        Map<String, Object> out = Map.of(
                "agentesDisponiveis", disponiveis,
                "agentesEmMissao", emMissao,
                "missoesEmExecucao", execucao,
                "anomaliasAtivas", ativas,
                "anomaliasCriticas", criticas);
        try {
            redis.opsForValue().set("ops:overview", mapper.writeValueAsString(out),
                    Duration.ofSeconds(ttlSeconds));
        } catch (Exception ignored) {}
        return out;
    }

    public Map<String, Object> stability() {
        try {
            String cached = redis.opsForValue().get("stability:index");
            if (cached != null) {
                return mapper.readValue(cached, Map.class);
            }
        } catch (Exception ignored) {}
        long ativas = anomalias.findAtivas().size();
        long criticas = anomalias.findByRisco(RiscoAnomalia.CRITICO).stream()
                .filter(a -> a.getSituacao() != SituacaoAnomalia.ESTABILIZADA
                        && a.getSituacao() != SituacaoAnomalia.IRREVERSIVEL).count();
        long execucao = missoes.findBySituacao(SituacaoMissao.EM_EXECUCAO).size();
        long falhou = missoes.findBySituacao(SituacaoMissao.FALHOU).size();
        long severas = intervencoes.findAll().stream()
                .filter(i -> i.getImpacto() == ImpactoIntervencao.SEVERO).count();

        int penalidade = (int) (criticas * pesoCritica
                + Math.max(0, ativas - criticas) * pesoAtiva
                + execucao * pesoExecucao
                + falhou * pesoFalha
                + severas * pesoSevera);
        int indice = Math.max(0, Math.min(100, 100 - penalidade));
        indiceAtual.set(indice);
        Map<String, Object> out = Map.of(
                "indice", indice,
                "fatores", Map.of(
                        "anomaliasAtivas", ativas,
                        "anomaliasCriticas", criticas,
                        "missoesEmExecucao", execucao,
                        "missoesFalhou", falhou,
                        "intervencoesSeveras", severas));
        try {
            redis.opsForValue().set("stability:index", mapper.writeValueAsString(out),
                    Duration.ofSeconds(ttlSeconds));
        } catch (Exception ignored) {}
        return out;
    }
}
