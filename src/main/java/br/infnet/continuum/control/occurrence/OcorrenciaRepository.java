package br.infnet.continuum.control.occurrence;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OcorrenciaRepository extends MongoRepository<OcorrenciaDoc, String> {
    List<OcorrenciaDoc> findByMissaoId(String missaoId);
    List<OcorrenciaDoc> findByMissaoIdAndTipo(String missaoId, String tipo);
}
