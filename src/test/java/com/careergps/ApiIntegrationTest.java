package com.careergps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
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
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void demandaDeDataAnalystCoincideConElMockupDelAvance1() throws Exception {
        mockMvc.perform(get("/api/puestos/1/demanda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puestoNombre").value("Data Analyst"))
                .andExpect(jsonPath("$.totalOfertas").value(10))
                .andExpect(jsonPath("$.habilidades[0].nombre").value("SQL"))
                .andExpect(jsonPath("$.habilidades[0].porcentaje").value(90.0))
                .andExpect(jsonPath("$.habilidades[1].nombre").value("Excel"))
                .andExpect(jsonPath("$.habilidades[1].porcentaje").value(80.0))
                .andExpect(jsonPath("$.habilidades[2].nombre").value("Power BI"))
                .andExpect(jsonPath("$.habilidades[2].porcentaje").value(70.0))
                .andExpect(jsonPath("$.habilidades[3].nombre").value("Python"))
                .andExpect(jsonPath("$.habilidades[3].porcentaje").value(50.0));
    }

    @Test
    void demandaDeUnPuestoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/puestos/999/demanda"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void demandaConMenosDeCincoOfertasDevuelve422() throws Exception {
        mockMvc.perform(get("/api/puestos/3/demanda"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value(containsString("al menos 5 ofertas")));
    }

    @Test
    void alineacionSeparaCoincidenciasDeHabilidadesAFortalecer() throws Exception {
        mockMvc.perform(get("/api/estudiantes/1/alineacion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coincidencias", hasItem("SQL")))
                .andExpect(jsonPath("$.coincidencias", hasItem("Excel")))
                .andExpect(jsonPath("$.aFortalecer", hasItem("Python")))
                .andExpect(jsonPath("$.aFortalecer", hasItem("Power BI")));
    }

    @Test
    void registrarEstudianteSinCarreraDevuelve400() throws Exception {
        mockMvc.perform(post("/api/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carrera\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La carrera es obligatoria"));
    }

    @Test
    void crudDeOfertaCrearLeerActualizarYEliminar() throws Exception {
        String respuesta = mockMvc.perform(post("/api/ofertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"OF-TEST-01\",\"sector\":\"Retail\",\"puestoId\":3,"
                                + "\"descripcion\":\"Se busca diseñador\",\"habilidadIdsConfirmadas\":[1]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("OF-TEST-01"))
                .andReturn().getResponse().getContentAsString();
        String id = respuesta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/ofertas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sector").value("Retail"));

        mockMvc.perform(put("/api/ofertas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"OF-TEST-01\",\"sector\":\"Banca\",\"puestoId\":3,"
                                + "\"descripcion\":\"Se busca diseñador\",\"habilidadIdsConfirmadas\":[2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sector").value("Banca"));

        mockMvc.perform(delete("/api/ofertas/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ofertas/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void crudDeEstudianteCrearActualizarYEliminar() throws Exception {
        String respuesta = mockMvc.perform(post("/api/estudiantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carrera\":\"Administracion\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = respuesta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(post("/api/estudiantes/" + id + "/habilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"habilidadId\":1,\"nivel\":\"basico\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/estudiantes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carrera\":\"Economia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carrera").value("Economia"));

        mockMvc.perform(delete("/api/estudiantes/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/estudiantes/" + id))
                .andExpect(status().isNotFound());
    }
}
