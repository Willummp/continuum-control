package br.infnet.continuum.control.seed;

import br.infnet.continuum.control.agent.Agente;
import br.infnet.continuum.control.agent.AgenteRepository;
import br.infnet.continuum.control.anomaly.Anomalia;
import br.infnet.continuum.control.anomaly.AnomaliaRepository;
import br.infnet.continuum.control.anomaly.RiscoAnomalia;
import br.infnet.continuum.control.auth.Perfil;
import br.infnet.continuum.control.auth.Usuario;
import br.infnet.continuum.control.auth.UsuarioRepository;
import br.infnet.continuum.control.event.EventoHistorico;
import br.infnet.continuum.control.event.EventoRepository;
import br.infnet.continuum.control.event.ImportanciaEvento;
import br.infnet.continuum.control.mission.Missao;
import br.infnet.continuum.control.mission.MissaoRepository;
import br.infnet.continuum.control.mission.PrioridadeMissao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile({"dev", "docker", "default"})
public class DataSeeder implements CommandLineRunner {

    private final boolean enabled;
    private final AgenteRepository agentes;
    private final EventoRepository eventos;
    private final AnomaliaRepository anomalias;
    private final MissaoRepository missoes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DataSeeder(@Value("${continuum.seed.enabled:true}") boolean enabled,
                      AgenteRepository agentes, EventoRepository eventos,
                      AnomaliaRepository anomalias, MissaoRepository missoes,
                      UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.enabled = enabled;
        this.agentes = agentes;
        this.eventos = eventos;
        this.anomalias = anomalias;
        this.missoes = missoes;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled || agentes.count() > 0) return;

        Agente a1 = agentes.save(new Agente("Elena Voss", "CRONOS-01", "Física temporal"));
        Agente a2 = agentes.save(new Agente("Marcus Reed", "CRONOS-02", "História"));
        Agente a3 = agentes.save(new Agente("Iris Chen", "CRONOS-03", "Engenharia"));
        agentes.save(new Agente("Dario Holt", "CRONOS-04", "Campo"));
        agentes.save(new Agente("Sofia Marques", "CRONOS-05", "Análise"));
        agentes.save(new Agente("Theo Aldane", "CRONOS-06", "Contenção"));

        EventoHistorico e1 = eventos.save(new EventoHistorico("Assinatura do Tratado de Genebra",
                "Acordo internacional monitorado", LocalDate.of(1954, 7, 21), "Genebra", ImportanciaEvento.ALTA));
        EventoHistorico e2 = eventos.save(new EventoHistorico("Primeiro transistor funcional",
                "Marco tecnológico", LocalDate.of(1947, 12, 23), "Bell Labs", ImportanciaEvento.MODERADA));
        eventos.save(new EventoHistorico("Queda do Muro de Berlim",
                "Evento político crítico", LocalDate.of(1989, 11, 9), "Berlim", ImportanciaEvento.CRITICA));
        EventoHistorico e4 = eventos.save(new EventoHistorico("Lançamento Apollo 11",
                "Marco científico", LocalDate.of(1969, 7, 16), "Cabo Canaveral", ImportanciaEvento.ALTA));
        eventos.save(new EventoHistorico("Feira Mundial de 1900",
                "Evento social", LocalDate.of(1900, 4, 14), "Paris", ImportanciaEvento.BAIXA));

        Anomalia an1 = anomalias.save(new Anomalia(e1, "Data de ratificação diverge 2 dias do registro esperado", RiscoAnomalia.ALTO));
        Anomalia an2 = anomalias.save(new Anomalia(e2, "Protótipo aparece 1 semana antes do esperado", RiscoAnomalia.MODERADO));
        Anomalia an3 = anomalias.save(new Anomalia(e4, "Sinal de telemetria com origem desconhecida", RiscoAnomalia.CRITICO));

        missoes.save(new Missao("MSN-001", "Investigar divergência do tratado", an1, "1954-07-21/Genebra", PrioridadeMissao.ALTA));
        missoes.save(new Missao("MSN-002", "Corrigir anomalia crítica Apollo", an3, "1969-07-16/Cabo Canaveral", PrioridadeMissao.EMERGENCIA));

        String pass = encoder.encode("continuum123");
        usuarios.save(new Usuario("operador", pass, Perfil.OPERADOR, null));
        usuarios.save(new Usuario("agente1", pass, Perfil.AGENTE, a1));
        usuarios.save(new Usuario("agente2", pass, Perfil.AGENTE, a2));
        usuarios.save(new Usuario("supervisor", pass, Perfil.SUPERVISOR, null));
        usuarios.save(new Usuario("admin", pass, Perfil.ADMINISTRADOR, a3));
    }
}
