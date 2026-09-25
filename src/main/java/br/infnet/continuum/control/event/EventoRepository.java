package br.infnet.continuum.control.event;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<EventoHistorico, UUID> {
    List<EventoHistorico> findByImportancia(ImportanciaEvento importancia);
    List<EventoHistorico> findByDataEventoBetween(LocalDate de, LocalDate ate);
}
