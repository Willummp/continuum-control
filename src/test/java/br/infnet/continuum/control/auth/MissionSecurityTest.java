package br.infnet.continuum.control.auth;

import br.infnet.continuum.control.TestcontainersConfiguration;
import br.infnet.continuum.control.agent.Agente;
import br.infnet.continuum.control.agent.AgenteRepository;
import br.infnet.continuum.control.anomaly.Anomalia;
import br.infnet.continuum.control.anomaly.AnomaliaRepository;
import br.infnet.continuum.control.anomaly.RiscoAnomalia;
import br.infnet.continuum.control.event.EventoHistorico;
import br.infnet.continuum.control.event.EventoRepository;
import br.infnet.continuum.control.event.ImportanciaEvento;
import br.infnet.continuum.control.mission.MissaoRepository;
import br.infnet.continuum.control.mission.PrioridadeMissao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MissionSecurityTest {

    @Autowired br.infnet.continuum.control.mission.MissaoService missoes;
    @Autowired MissionSecurity security;
    @Autowired AgenteRepository agentes;
    @Autowired EventoRepository eventos;
    @Autowired AnomaliaRepository anomalias;
    @Autowired MissaoRepository missaoRepo;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired br.infnet.continuum.control.mission.EncerramentoRepository encerramentos;
    @Autowired br.infnet.continuum.control.mission.IntervencaoRepository intervencoes;
    @Autowired br.infnet.continuum.control.mission.AutorizacaoRepository autorizacoes;

    Agente a1;
    Agente a2;

    @BeforeEach
    void setup() {
        jdbc.execute("DELETE FROM missao_agentes");
        encerramentos.deleteAll();
        intervencoes.deleteAll();
        autorizacoes.deleteAll();
        missaoRepo.deleteAll();
        usuarios.deleteAll();
        anomalias.deleteAll();
        eventos.deleteAll();
        agentes.deleteAll();
        a1 = agentes.save(new Agente("Agente Um", "SEC-" + UUID.randomUUID(), "Campo"));
        a2 = agentes.save(new Agente("Agente Dois", "SEC-" + UUID.randomUUID(), "Campo"));
        usuarios.save(new Usuario("sec-agente", encoder.encode("123456"), Perfil.AGENTE, a1));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    void authAs(String username, String perfil) {
        var auth = new UsernamePasswordAuthenticationToken(username, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void agenteEscreveSoNaPropriaMissao() {
        var evento = eventos.save(new EventoHistorico("E", "D",
                LocalDate.of(2001, 1, 1), "L", ImportanciaEvento.BAIXA));
        var an1 = anomalias.save(new Anomalia(evento, "d1", RiscoAnomalia.BAIXO));
        var an2 = anomalias.save(new Anomalia(evento, "d2", RiscoAnomalia.BAIXO));
        var m1 = missoes.criar(new br.infnet.continuum.control.mission.MissaoService.CreateMissao("SEC-1", "o", an1.getId(), "2001", PrioridadeMissao.NORMAL), "t");
        var m2 = missoes.criar(new br.infnet.continuum.control.mission.MissaoService.CreateMissao("SEC-2", "o", an2.getId(), "2001", PrioridadeMissao.NORMAL), "t");
        missoes.designarAgente(m1.getId(), a1.getId());
        missoes.designarAgente(m2.getId(), a2.getId());
        authAs("sec-agente", "AGENTE");
        assertTrue(security.canWrite(m1.getId()));
        assertFalse(security.canWrite(m2.getId()));
    }

    @Test
    void supervisorEscreveEmQualquerMissao() {
        var evento = eventos.save(new EventoHistorico("E", "D",
                LocalDate.of(2001, 1, 1), "L", ImportanciaEvento.BAIXA));
        var an = anomalias.save(new Anomalia(evento, "d", RiscoAnomalia.BAIXO));
        var m = missoes.criar(new br.infnet.continuum.control.mission.MissaoService.CreateMissao("SEC-3", "o", an.getId(), "2001", PrioridadeMissao.NORMAL), "t");
        authAs("qualquer", "SUPERVISOR");
        assertTrue(security.canWrite(m.getId()));
    }
}
