package br.infnet.continuum.control.event;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "eventos")
public class EventoHistorico {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_evento", nullable = false)
    private LocalDate dataEvento;

    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImportanciaEvento importancia;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected EventoHistorico() {}

    public EventoHistorico(String titulo, String descricao, LocalDate dataEvento, String localizacao, ImportanciaEvento importancia) {
        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataEvento = dataEvento;
        this.localizacao = localizacao;
        this.importancia = importancia;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public ImportanciaEvento getImportancia() { return importancia; }
    public void setImportancia(ImportanciaEvento importancia) { this.importancia = importancia; }
    public Instant getCreatedAt() { return createdAt; }
}
