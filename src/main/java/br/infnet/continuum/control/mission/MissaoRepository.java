package br.infnet.continuum.control.mission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MissaoRepository extends JpaRepository<Missao, UUID> {
    List<Missao> findBySituacao(SituacaoMissao situacao);
    List<Missao> findByAnomalia_Id(UUID anomaliaId);
}
