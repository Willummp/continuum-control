package br.infnet.continuum.control;

import br.infnet.continuum.control.auth.Perfil;
import br.infnet.continuum.control.auth.Usuario;
import br.infnet.continuum.control.auth.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ApiContratoTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    String tokenOperador;
    String tokenAdmin;

    @BeforeEach
    void setup() throws Exception {
        usuarios.findByUsername("t-operador").ifPresentOrElse(u -> {}, () ->
                usuarios.save(new Usuario("t-operador", encoder.encode("123456"), Perfil.OPERADOR, null)));
        usuarios.findByUsername("t-admin").ifPresentOrElse(u -> {}, () ->
                usuarios.save(new Usuario("t-admin", encoder.encode("123456"), Perfil.ADMINISTRADOR, null)));
        tokenOperador = login("t-operador");
        tokenAdmin = login("t-admin");
    }

    String login(String user) throws Exception {
        var res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("username", user, "password", "123456"))))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/agentes/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void payloadInvalidoRetorna400() throws Exception {
        mvc.perform(post("/eventos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void operadorNaoCriaAgenteRetorna403() throws Exception {
        mvc.perform(post("/agentes")
                        .header("Authorization", "Bearer " + tokenOperador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("nome", "X", "codinome", "Y", "especialidade", "Z"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCriaAgenteRetorna201() throws Exception {
        mvc.perform(post("/agentes")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("nome", "A", "codinome", "C-" + System.nanoTime(), "especialidade", "E"))))
                .andExpect(status().isCreated());
    }
}
