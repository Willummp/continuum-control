package br.infnet.continuum.control;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired MockMvc mvc;

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/agentes")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginInvalidoRetorna401Ou403() throws Exception {
        mvc.perform(get("/stability")).andExpect(status().isUnauthorized());
    }
}
