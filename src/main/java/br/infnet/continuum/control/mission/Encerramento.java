package br.infnet.continuum.control.mission;

import br.infnet.continuum.control.anomaly.SituacaoAnomalia;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "encerramentos")
public class Encerramento {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "missao_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_encerramentos_missao"))
    private Missao missao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultado;

    @Column(columnDefinition = "TEXT")
    private String resumo;

    @Column(name = "impacto_observado", columnDefinition = "TEXT")
    private String impactoObservado;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_final_anomalia", length = 20)
    private SituacaoAnomalia situacaoFinalAnomalia;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "encerrada_em", nullable = false)
    private Instant encerradaEm;

    protected Encerramento() {}

    public Encerramento(Missao missao, String resultado, String resumo, String impactoObservado, SituacaoAnomalia situacaoFinalAnomalia, String observacoes) {
        this.id = UUID.randomUUID();
        this.missao = missao;
        this.resultado = resultado;
        this.resumo = resumo;
        this.impactoObservado = impactoObservado;
        this.situacaoFinalAnomalia = situacaoFinalAnomalia;
        this.observacoes = observacoes;
        this.encerradaEm = Instant.now();
    }

    public UUID getId() { return id; }
    public Missao getMissao() { return missao; }
    public String getResultado() { return resultado; }
    public String getResumo() { return resumo; }
    public String getImpactoObservado() { return impactoObservado; }
    public SituacaoAnomalia getSituacaoFinalAnomalia() { return situacaoFinalAnomalia; }
    public String getObservacoes() { return observacoes; }
    public Instant getEncerradaEm() { return encerradaEm; }
}
