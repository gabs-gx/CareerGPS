package com.careergps;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertaApiTest {

    @Autowired
    private MockMvc mockMvc;

    private String crearEstudiante() throws Exception {
        String respuesta = mockMvc.perform(post("/api/estudiantes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carrera\":\"Sistemas\"}"))
                .andReturn().getResponse().getContentAsString();
        return respuesta.replaceAll(".*\"id\":(\\d+).*", "$1");
    }

    @Test
    void guardaLeeMarcaComoLeidasYBorraLasAlertas() throws Exception {
        String id = crearEstudiante();
        String base = "/api/estudiantes/" + id + "/alertas";

        mockMvc.perform(get(base))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshot").doesNotExist())
                .andExpect(jsonPath("$.alertas", hasSize(0)));

        mockMvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"snapshot\":\"{}\",\"nuevas\":[{\"tipo\":\"sube\",\"texto\":\"Subio\"},{\"tipo\":\"meta\",\"texto\":\"Meta\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshot").value("{}"))
                .andExpect(jsonPath("$.alertas", hasSize(2)))
                .andExpect(jsonPath("$.alertas[0].texto").value("Meta"))
                .andExpect(jsonPath("$.alertas[0].leida").value(false));

        mockMvc.perform(put(base + "/leidas")).andExpect(status().isNoContent());
        mockMvc.perform(get(base)).andExpect(jsonPath("$.alertas[0].leida").value(true));

        mockMvc.perform(delete(base)).andExpect(status().isNoContent());
        mockMvc.perform(get(base)).andExpect(jsonPath("$.alertas", hasSize(0)))
                .andExpect(jsonPath("$.snapshot").value("{}"));
    }

    @Test
    void rechazaTiposInvalidosYEstudiantesInexistentes() throws Exception {
        String id = crearEstudiante();
        mockMvc.perform(post("/api/estudiantes/" + id + "/alertas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevas\":[{\"tipo\":\"spam\",\"texto\":\"x\"}]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/estudiantes/99999/alertas")).andExpect(status().isNotFound());
    }
}
