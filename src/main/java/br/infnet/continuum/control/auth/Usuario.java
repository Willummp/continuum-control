package br.infnet.continuum.control.auth;

import br.infnet.continuum.control.agent.Agente;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_username", columnNames = "username"))
public class Usuario {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", foreignKey = @ForeignKey(name = "fk_usuarios_agente"))
    private Agente agente;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Usuario() {}

    public Usuario(String username, String password, Perfil perfil, Agente agente) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.perfil = perfil;
        this.agente = agente;
        this.ativo = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Perfil getPerfil() { return perfil; }
    public Agente getAgente() { return agente; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
