package br.infnet.continuum.control.mission;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "autorizacoes")
public class Autorizacao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "missao_id", nullable = false, foreignKey = @ForeignKey(name = "fk_autorizacoes_missao"))
    private Missao missao;

    @Column(nullable = false)
    private String responsavel;

    @Column(nullable = false)
    private Instant instante;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    protected Autorizacao() {}

    public Autorizacao(Missao missao, String responsavel, String observacao) {
        this.id = UUID.randomUUID();
        this.missao = missao;
        this.responsavel = responsavel;
        this.instante = Instant.now();
        this.observacao = observacao;
    }

    public UUID getId() { return id; }
    public Missao getMissao() { return missao; }
    public String getResponsavel() { return responsavel; }
    public Instant getInstante() { return instante; }
    public String getObservacao() { return observacao; }
}
