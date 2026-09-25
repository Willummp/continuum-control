package br.infnet.continuum.control.occurrence;

import br.infnet.continuum.control.common.exception.BusinessException;
import br.infnet.continuum.control.common.exception.ResourceNotFoundException;
import br.infnet.continuum.control.mission.MissaoRepository;
import br.infnet.continuum.control.mission.SituacaoMissao;
import org.springframework.stereotype.Service;

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

    public OcorrenciaService(OcorrenciaRepository ocorrencias, HistoricoRepository historico,
                             MissaoRepository missoes) {
        this.ocorrencias = ocorrencias;
        this.historico = historico;
        this.missoes = missoes;
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
        if ("DISTORSAO_TEMPORAL".equals(tipo) && (dados == null || !dados.containsKey("intensidade"))) {
            throw new BusinessException("Distorção temporal exige intensidade e duração");
        }
        OcorrenciaDoc doc = new OcorrenciaDoc(missaoId.toString(), tipo, ator, descricao, dados);
        OcorrenciaDoc salva = ocorrencias.save(doc);
        historico.save(new HistoricoAnomaliaDoc(missao.getAnomalia().getId().toString(), "OCORRENCIA", ator,
                Map.of("missaoId", missaoId.toString(), "tipo", tipo)));
        return salva;
    }

    public List<OcorrenciaDoc> listar(UUID missaoId, String tipo) {
        if (tipo != null) return ocorrencias.findByMissaoIdAndTipo(missaoId.toString(), tipo);
        return ocorrencias.findByMissaoId(missaoId.toString());
    }
}
