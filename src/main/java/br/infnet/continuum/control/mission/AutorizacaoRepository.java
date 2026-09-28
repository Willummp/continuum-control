package br.infnet.continuum.control.mission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AutorizacaoRepository extends JpaRepository<Autorizacao, UUID> {
    List<Autorizacao> findByMissao_Id(UUID missaoId);
}
