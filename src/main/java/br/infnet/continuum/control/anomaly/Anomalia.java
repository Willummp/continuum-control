package br.infnet.continuum.control.anomaly;

import br.infnet.continuum.control.event.EventoHistorico;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anomalias")
public class Anomalia {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "evento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_anomalias_evento"))
    private EventoHistorico evento;

    @Column(name = "divergencia", nullable = false, columnDefinition = "TEXT")
    private String divergencia;

    @Column(name = "detectada_em", nullable = false)
    private Instant detectadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiscoAnomalia risco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAnomalia situacao;

    private String origem;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Anomalia() {}

    public Anomalia(EventoHistorico evento, String divergencia, RiscoAnomalia risco) {
        this.id = UUID.randomUUID();
        this.evento = evento;
        this.divergencia = divergencia;
        this.detectadaEm = Instant.now();
        this.risco = risco;
        this.situacao = SituacaoAnomalia.DETECTADA;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void touch() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public EventoHistorico getEvento() { return evento; }
    public String getDivergencia() { return divergencia; }
    public void setDivergencia(String divergencia) { this.divergencia = divergencia; }
    public Instant getDetectadaEm() { return detectadaEm; }
    public RiscoAnomalia getRisco() { return risco; }
    public void setRisco(RiscoAnomalia risco) { this.risco = risco; }
    public SituacaoAnomalia getSituacao() { return situacao; }
    public void setSituacao(SituacaoAnomalia situacao) { this.situacao = situacao; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
