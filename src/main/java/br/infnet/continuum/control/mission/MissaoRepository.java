package br.infnet.continuum.control.mission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MissaoRepository extends JpaRepository<Missao, UUID> {
    List<Missao> findBySituacao(SituacaoMissao situacao);
    List<Missao> findByAnomalia_Id(UUID anomaliaId);
    Page<Missao> findBySituacao(SituacaoMissao situacao, Pageable pageable);
    Page<Missao> findByAnomalia_Id(UUID anomaliaId, Pageable pageable);
    Page<Missao> findByAgentes_Id(UUID agenteId, Pageable pageable);
}
