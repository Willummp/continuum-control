package br.infnet.continuum.control.agent;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agentes", uniqueConstraints = @UniqueConstraint(name = "uk_agentes_codinome", columnNames = "codinome"))
public class Agente {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String codinome;

    @Column(nullable = false)
    private String especialidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAgente situacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Agente() {}

    public Agente(String nome, String codinome, String especialidade) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.codinome = codinome;
        this.especialidade = especialidade;
        this.situacao = SituacaoAgente.DISPONIVEL;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodinome() { return codinome; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public SituacaoAgente getSituacao() { return situacao; }
    public void setSituacao(SituacaoAgente situacao) { this.situacao = situacao; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
