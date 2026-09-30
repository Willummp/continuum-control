package br.infnet.continuum.control.occurrence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "historico_anomalia")
public class HistoricoAnomaliaDoc {

    @Id
    private String id;
    private String anomaliaId;
    private Instant instante;
    private String tipoEvento;
    private String ator;
    private Map<String, Object> detalhe;

    public HistoricoAnomaliaDoc() {}

    public HistoricoAnomaliaDoc(String anomaliaId, String tipoEvento, String ator, Map<String, Object> detalhe) {
        this.anomaliaId = anomaliaId;
        this.instante = Instant.now();
        this.tipoEvento = tipoEvento;
        this.ator = ator;
        this.detalhe = detalhe;
    }

    public String getId() { return id; }
    public String getAnomaliaId() { return anomaliaId; }
    public Instant getInstante() { return instante; }
    public String getTipoEvento() { return tipoEvento; }
    public String getAtor() { return ator; }
    public Map<String, Object> getDetalhe() { return detalhe; }
}
