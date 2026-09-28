package br.infnet.continuum.control.mission;

import br.infnet.continuum.control.agent.Agente;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intervencoes")
public class Intervencao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "missao_id", nullable = false, foreignKey = @ForeignKey(name = "fk_intervencoes_missao"))
    private Missao missao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_intervencoes_agente"))
    private Agente agente;

    @Column(nullable = false)
    private Instant instante;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String acao;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactoIntervencao impacto;

    @Column(columnDefinition = "TEXT")
    private String resultado;

    protected Intervencao() {}

    public Intervencao(Missao missao, Agente agente, String acao, String justificativa, ImpactoIntervencao impacto, String resultado) {
        this.id = UUID.randomUUID();
        this.missao = missao;
        this.agente = agente;
        this.instante = Instant.now();
        this.acao = acao;
        this.justificativa = justificativa;
        this.impacto = impacto;
        this.resultado = resultado;
    }

    public UUID getId() { return id; }
    public Missao getMissao() { return missao; }
    public Agente getAgente() { return agente; }
    public Instant getInstante() { return instante; }
    public String getAcao() { return acao; }
    public String getJustificativa() { return justificativa; }
    public ImpactoIntervencao getImpacto() { return impacto; }
    public String getResultado() { return resultado; }
}
