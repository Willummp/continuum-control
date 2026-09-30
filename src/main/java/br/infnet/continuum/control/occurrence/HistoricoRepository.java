package br.infnet.continuum.control.occurrence;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface HistoricoRepository extends MongoRepository<HistoricoAnomaliaDoc, String> {
    List<HistoricoAnomaliaDoc> findByAnomaliaIdOrderByInstanteAsc(String anomaliaId);
}
