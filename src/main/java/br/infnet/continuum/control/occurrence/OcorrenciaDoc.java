package br.infnet.continuum.control.occurrence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "ocorrencias")
public class OcorrenciaDoc {

    @Id
    private String id;
    private String missaoId;
    private String tipo;
    private Instant registradaEm;
    private String registradaPor;
    private String descricao;
    private Map<String, Object> dados;

    public OcorrenciaDoc() {}

    public OcorrenciaDoc(String missaoId, String tipo, String registradaPor, String descricao, Map<String, Object> dados) {
        this.missaoId = missaoId;
        this.tipo = tipo;
        this.registradaEm = Instant.now();
        this.registradaPor = registradaPor;
        this.descricao = descricao;
        this.dados = dados;
    }

    public String getId() { return id; }
    public String getMissaoId() { return missaoId; }
    public String getTipo() { return tipo; }
    public Instant getRegistradaEm() { return registradaEm; }
    public String getRegistradaPor() { return registradaPor; }
    public String getDescricao() { return descricao; }
    public Map<String, Object> getDados() { return dados; }
}
