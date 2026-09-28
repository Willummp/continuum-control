package br.infnet.continuum.control.mission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IntervencaoRepository extends JpaRepository<Intervencao, UUID> {
    List<Intervencao> findByMissao_Id(UUID missaoId);
}
