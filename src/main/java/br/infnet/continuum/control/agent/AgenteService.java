package br.infnet.continuum.control.agent;

import br.infnet.continuum.control.common.exception.BusinessException;
import br.infnet.continuum.control.common.exception.ResourceNotFoundException;
import br.infnet.continuum.control.mission.MissaoRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgenteService {

    public record CreateAgente(@NotBlank String nome, @NotBlank String codinome, @NotBlank String especialidade) {}
    public record UpdateAgente(String nome, String especialidade) {}

    private final AgenteRepository agentes;
    private final MissaoRepository missoes;

    public AgenteService(AgenteRepository agentes, MissaoRepository missoes) {
        this.agentes = agentes;
        this.missoes = missoes;
    }

    public Agente criar(CreateAgente req) {
        return agentes.save(new Agente(req.nome(), req.codinome(), req.especialidade()));
    }

    public Page<Agente> listar(SituacaoAgente situacao, String especialidade, Pageable pageable) {
        if (situacao != null) return agentes.findBySituacao(situacao, pageable);
        if (especialidade != null) return agentes.findByEspecialidadeIgnoreCase(especialidade, pageable);
        return agentes.findAll(pageable);
    }

    public Agente buscar(UUID id) {
        return agentes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Agente não encontrado"));
    }

    public Agente atualizar(UUID id, UpdateAgente req) {
        Agente a = buscar(id);
        if (req.nome() != null) a.setNome(req.nome());
        if (req.especialidade() != null) a.setEspecialidade(req.especialidade());
        return agentes.save(a);
    }

    public void remover(UUID id) {
        Agente a = buscar(id);
        boolean temHistorico = missoes.findAll().stream()
                .anyMatch(m -> m.getAgentes().stream().anyMatch(ag -> ag.getId().equals(id)));
        if (temHistorico) {
            throw new BusinessException("Agente com histórico em missões não pode ser removido");
        }
        agentes.delete(a);
    }

    public Agente alterarSituacao(UUID id, SituacaoAgente nova) {
        Agente a = buscar(id);
        a.setSituacao(nova);
        return agentes.save(a);
    }
}
