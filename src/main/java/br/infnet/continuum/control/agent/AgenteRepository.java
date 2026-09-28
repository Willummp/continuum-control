package br.infnet.continuum.control.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AgenteRepository extends JpaRepository<Agente, UUID> {
    List<Agente> findBySituacao(SituacaoAgente situacao);
    List<Agente> findByEspecialidadeIgnoreCase(String especialidade);
}
