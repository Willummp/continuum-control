package br.infnet.continuum.control.anomaly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface AnomaliaRepository extends JpaRepository<Anomalia, UUID> {
    List<Anomalia> findBySituacao(SituacaoAnomalia situacao);
    List<Anomalia> findByRisco(RiscoAnomalia risco);

    @Query("select a from Anomalia a where a.situacao not in (br.infnet.continuum.control.anomaly.SituacaoAnomalia.ESTABILIZADA, br.infnet.continuum.control.anomaly.SituacaoAnomalia.IRREVERSIVEL)")
    List<Anomalia> findAtivas();

    List<Anomalia> findByRiscoAndSituacaoNot(RiscoAnomalia risco, SituacaoAnomalia situacao);
}
