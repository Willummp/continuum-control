package br.infnet.continuum.control.mission;

import br.infnet.continuum.control.TestcontainersConfiguration;
import br.infnet.continuum.control.agent.Agente;
import br.infnet.continuum.control.agent.AgenteRepository;
import br.infnet.continuum.control.agent.SituacaoAgente;
import br.infnet.continuum.control.anomaly.Anomalia;
import br.infnet.continuum.control.anomaly.AnomaliaRepository;
import br.infnet.continuum.control.anomaly.RiscoAnomalia;
import br.infnet.continuum.control.anomaly.SituacaoAnomalia;
import br.infnet.continuum.control.common.exception.BusinessException;
import br.infnet.continuum.control.event.EventoHistorico;
import br.infnet.continuum.control.event.EventoRepository;
import br.infnet.continuum.control.event.ImportanciaEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MissaoRegrasTest {

    @Autowired MissaoService missoes;
    @Autowired MissaoRepository missaoRepo;
    @Autowired AgenteRepository agentes;
    @Autowired EventoRepository eventos;
    @Autowired AnomaliaRepository anomalias;
    @Autowired AutorizacaoRepository autorizacoes;
    @Autowired IntervencaoRepository intervencoesRepo;
    @Autowired EncerramentoRepository encerramentos;
    @Autowired br.infnet.continuum.control.auth.UsuarioRepository usuariosRepo;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    EventoHistorico evento;
    Agente agente;

    @BeforeEach
    void setup() {
        jdbc.execute("DELETE FROM missao_agentes");
        encerramentos.deleteAll();
        intervencoesRepo.deleteAll();
        autorizacoes.deleteAll();
        missaoRepo.deleteAll();
        usuariosRepo.deleteAll();
        anomalias.deleteAll();
        eventos.deleteAll();
        agentes.deleteAll();
        evento = eventos.save(new EventoHistorico("Evento T", "Desc",
                LocalDate.of(2000, 1, 1), "Local", ImportanciaEvento.ALTA));
        agente = agentes.save(new Agente("Nome", "COD-" + UUID.randomUUID(), "Campo"));
    }

    @Test
    void naoIniciaSemAgente() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.BAIXO));
        Missao m = missoes.criar(new MissaoService.CreateMissao("C1", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste");
        assertThrows(BusinessException.class, () -> missoes.iniciar(m.getId(), "teste"));
    }

    @Test
    void emergenciaExigeAutorizacao() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.BAIXO));
        Missao m = missoes.criar(new MissaoService.CreateMissao("C2", "obj", an.getId(), "2000", PrioridadeMissao.EMERGENCIA), "teste");
        missoes.designarAgente(m.getId(), agente.getId());
        assertThrows(BusinessException.class, () -> missoes.iniciar(m.getId(), "teste"));
        missoes.autorizar(m.getId(), new MissaoService.AutorizarReq("sup", "ok"), "teste");
        Missao iniciada = missoes.iniciar(m.getId(), "teste");
        assertEquals(SituacaoMissao.EM_EXECUCAO, iniciada.getSituacao());
    }

    @Test
    void criticaExigeAutorizacao() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.CRITICO));
        Missao m = missoes.criar(new MissaoService.CreateMissao("C3", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste");
        missoes.designarAgente(m.getId(), agente.getId());
        assertThrows(BusinessException.class, () -> missoes.iniciar(m.getId(), "teste"));
    }

    @Test
    void irreversivelNaoRecebeMissao() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.ALTO));
        an.setSituacao(SituacaoAnomalia.IRREVERSIVEL);
        anomalias.save(an);
        assertThrows(BusinessException.class,
                () -> missoes.criar(new MissaoService.CreateMissao("C4", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste"));
    }

    @Test
    void agenteIndisponivelNaoDesigna() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.BAIXO));
        Missao m = missoes.criar(new MissaoService.CreateMissao("C5", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste");
        agente.setSituacao(SituacaoAgente.SUSPENSO);
        agentes.save(agente);
        assertThrows(BusinessException.class, () -> missoes.designarAgente(m.getId(), agente.getId()));
    }

    @Test
    void iniciarMoveAgenteEAnomalia() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.BAIXO));
        an.setSituacao(SituacaoAnomalia.CONFIRMADA);
        anomalias.save(an);
        Missao m = missoes.criar(new MissaoService.CreateMissao("C6", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste");
        missoes.designarAgente(m.getId(), agente.getId());
        missoes.iniciar(m.getId(), "teste");
        assertEquals(SituacaoAgente.EM_MISSAO, agentes.findById(agente.getId()).orElseThrow().getSituacao());
        assertEquals(SituacaoAnomalia.EM_CORRECAO, anomalias.findById(an.getId()).orElseThrow().getSituacao());
    }

    @Test
    void falhaNaoResolveAnomalia() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.ALTO));
        Missao m = missoes.criar(new MissaoService.CreateMissao("C7", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste");
        missoes.designarAgente(m.getId(), agente.getId());
        missoes.iniciar(m.getId(), "teste");
        missoes.falhar(m.getId(), new MissaoService.ConcluirReq("falhou", "res", "imp", null, "obs"), "teste");
        SituacaoAnomalia sit = anomalias.findById(an.getId()).orElseThrow().getSituacao();
        assertNotEquals(SituacaoAnomalia.ESTABILIZADA, sit);
        assertEquals(SituacaoAgente.DISPONIVEL, agentes.findById(agente.getId()).orElseThrow().getSituacao());
    }

    @Test
    void intervencaoSoEmExecucao() {
        Anomalia an = anomalias.save(new Anomalia(evento, "div", RiscoAnomalia.BAIXO));
        Missao m = missoes.criar(new MissaoService.CreateMissao("C8", "obj", an.getId(), "2000", PrioridadeMissao.NORMAL), "teste");
        missoes.designarAgente(m.getId(), agente.getId());
        assertThrows(BusinessException.class, () -> missoes.intervir(m.getId(), agente.getId(),
                "acao", "just", ImpactoIntervencao.MINIMO, "res", "teste"));
    }
}
