package com.careergps;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String REGISTRO = "{\"nombres\":\"Luz\",\"apellidos\":\"Rojas\",\"correo\":\"luz@correo.com\","
            + "\"password\":\"clave-segura-1\",\"carrera\":\"Sistemas\"}";

    @Test
    void registrarIngresarYNoFiltrarDatosPersonalesPorId() throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombres").value("Luz"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = respuesta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"LUZ@correo.com\",\"password\":\"clave-segura-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Integer.parseInt(id)));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"luz@correo.com\",\"password\":\"equivocada-1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(containsString("incorrectos")));

        mockMvc.perform(get("/api/estudiantes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrera").value("Sistemas"))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("luz@correo.com"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("passwordHash"))));
    }

    @Test
    void registroConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Ana\",\"apellidos\":\"Gil\",\"correo\":\"no-es-correo\","
                                + "\"password\":\"corta\",\"carrera\":\"Sistemas\"}"))
                .andExpect(status().isBadRequest());
    }
}
