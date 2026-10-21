package br.infnet.continuum.control.occurrence;

import br.infnet.continuum.control.common.exception.BusinessException;
import br.infnet.continuum.control.common.exception.ResourceNotFoundException;
import br.infnet.continuum.control.mission.MissaoRepository;
import br.infnet.continuum.control.mission.SituacaoMissao;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OcorrenciaService {

    private static final Set<String> TIPOS = Set.of(
            "OBSERVACAO_HISTORICA", "ALTERACAO_DETECTADA", "EVIDENCIA_TEMPORAL",
            "DISTORSAO_TEMPORAL", "ENCONTRO_INESPERADO", "INCIDENTE_OPERACIONAL");

    private final OcorrenciaRepository ocorrencias;
    private final HistoricoRepository historico;
    private final MissaoRepository missoes;
    private final ObjectMapper mapper;

    public OcorrenciaService(OcorrenciaRepository ocorrencias, HistoricoRepository historico,
                             MissaoRepository missoes, ObjectMapper mapper) {
        this.ocorrencias = ocorrencias;
        this.historico = historico;
        this.missoes = missoes;
        this.mapper = mapper;
    }

    public OcorrenciaDoc registrar(UUID missaoId, String tipo, String descricao,
                                   Map<String, Object> dados, String ator) {
        var missao = missoes.findById(missaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada"));
        if (missao.getSituacao() != SituacaoMissao.EM_EXECUCAO) {
            throw new BusinessException("Ocorrências só podem ser registradas em missões em execução");
        }
        if (!TIPOS.contains(tipo)) {
            throw new BusinessException("Tipo de ocorrência inválido: " + tipo);
        }
        OcorrenciaDados validados = validarDados(tipo, dados == null ? Map.of() : dados);
        OcorrenciaDoc doc = new OcorrenciaDoc(missaoId.toString(), tipo, ator, descricao, validados.brutos());
        OcorrenciaDoc salva = ocorrencias.save(doc);
        historico.save(new HistoricoAnomaliaDoc(missao.getAnomalia().getId().toString(), "OCORRENCIA", ator,
                Map.of("missaoId", missaoId.toString(), "tipo", tipo)));
        return salva;
    }

    public List<OcorrenciaDoc> listar(UUID missaoId, String tipo) {
        if (tipo != null) return ocorrencias.findByMissaoIdAndTipo(missaoId.toString(), tipo);
        return ocorrencias.findByMissaoId(missaoId.toString());
    }

    private OcorrenciaDados validarDados(String tipo, Map<String, Object> dados) {
        return switch (tipo) {
            case "DISTORSAO_TEMPORAL" -> {
                var d = converter(dados, DistorcaoRaw.class, tipo);
                if (d.intensidade() == null || d.intensidade() < 0 || d.intensidade() > 100) {
                    throw new BusinessException("Distorção temporal exige intensidade entre 0 e 100");
                }
                if (d.duracao() == null || d.duracao().isBlank()) {
                    throw new BusinessException("Distorção temporal exige duração");
                }
                yield new OcorrenciaDados.DistorcaoTemporal(d.intensidade(), d.duracao(), dados);
            }
            case "EVIDENCIA_TEMPORAL" -> {
                var d = converter(dados, EvidenciaRaw.class, tipo);
                if (d.objeto() == null || d.objeto().isBlank()) {
                    throw new BusinessException("Evidência temporal exige objeto");
                }
                if (d.confiabilidade() == null
                        || !Set.of("BAIXA", "MEDIA", "ALTA").contains(d.confiabilidade())) {
                    throw new BusinessException("Evidência temporal exige confiabilidade BAIXA, MEDIA ou ALTA");
                }
                yield new OcorrenciaDados.EvidenciaTemporal(d.objeto(), d.periodoEstimado(),
                        d.confiabilidade(), dados);
            }
            default -> new OcorrenciaDados.Generica(dados);
        };
    }

    private <T> T converter(Map<String, Object> dados, Class<T> alvo, String tipo) {
        try {
            return mapper.convertValue(dados, alvo);
        } catch (Exception e) {
            throw new BusinessException("Dados inválidos para ocorrência " + tipo);
        }
    }

    private record DistorcaoRaw(Double intensidade, String duracao) {}

    private record EvidenciaRaw(String objeto, String periodoEstimado, String confiabilidade) {}
}
