package br.infnet.continuum.control.mission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EncerramentoRepository extends JpaRepository<Encerramento, UUID> {
    Optional<Encerramento> findByMissao_Id(UUID missaoId);
}
