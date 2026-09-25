package br.infnet.continuum.control.mission;

import br.infnet.continuum.control.agent.Agente;
import br.infnet.continuum.control.anomaly.Anomalia;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "missoes", uniqueConstraints = @UniqueConstraint(name = "uk_missoes_codigo", columnNames = "codigo"))
public class Missao {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objetivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "anomalia_id", nullable = false, foreignKey = @ForeignKey(name = "fk_missoes_anomalia"))
    private Anomalia anomalia;

    @Column(name = "destino_temporal")
    private String destinoTemporal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeMissao prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoMissao situacao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "missao_agentes",
        joinColumns = @JoinColumn(name = "missao_id", foreignKey = @ForeignKey(name = "fk_missao_agentes_missao")),
        inverseJoinColumns = @JoinColumn(name = "agente_id", foreignKey = @ForeignKey(name = "fk_missao_agentes_agente")))
    private Set<Agente> agentes = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Missao() {}

    public Missao(String codigo, String objetivo, Anomalia anomalia, String destinoTemporal, PrioridadeMissao prioridade) {
        this.id = UUID.randomUUID();
        this.codigo = codigo;
        this.objetivo = objetivo;
        this.anomalia = anomalia;
        this.destinoTemporal = destinoTemporal;
        this.prioridade = prioridade;
        this.situacao = SituacaoMissao.PLANEJADA;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void touch() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public Anomalia getAnomalia() { return anomalia; }
    public String getDestinoTemporal() { return destinoTemporal; }
    public void setDestinoTemporal(String destinoTemporal) { this.destinoTemporal = destinoTemporal; }
    public PrioridadeMissao getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeMissao prioridade) { this.prioridade = prioridade; }
    public SituacaoMissao getSituacao() { return situacao; }
    public void setSituacao(SituacaoMissao situacao) { this.situacao = situacao; }
    public Set<Agente> getAgentes() { return agentes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
