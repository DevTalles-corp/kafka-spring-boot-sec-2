package com.bistro.reservations.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ReservationCreationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldConfirmReservation() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ana García",
                                  "customerEmail": "ana@example.com",
                                  "reservationTime": "2026-08-20T19:30:00",
                                  "partySize": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.assignedTableId").exists());
    }

    @Test
    void shouldRejectReservationWhenNoCapacity() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Carlos López",
                                  "customerEmail": "carlos@example.com",
                                  "reservationTime": "2026-08-20T20:00:00",
                                  "partySize": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").isNotEmpty())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.assignedTableId").doesNotExist());
    }

    @Test
    void shouldReturn400WhenPartySizeExceeds12() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Marta Ruiz",
                                  "customerEmail": "marta@example.com",
                                  "reservationTime": "2026-08-20T20:00:00",
                                  "partySize": 13
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "",
                                  "customerEmail": "not-an-email",
                                  "reservationTime": null,
                                  "partySize": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
