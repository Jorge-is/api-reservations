package com.edteam.reservations.controller;

import com.edteam.reservations.dto.ReservationDTO;
import com.edteam.reservations.enums.APIError;
import com.edteam.reservations.exception.ReservationException;
import com.edteam.reservations.security.JwtUtil;
import com.edteam.reservations.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService service;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void getReservations_authenticated_returns200() throws Exception {
        when(service.getReservations(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new ReservationDTO())));

        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void getReservationById_existingId_returns200() throws Exception {
        when(service.getReservationById(1L)).thenReturn(new ReservationDTO());

        mockMvc.perform(get("/reservation/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getReservationById_idLessThanOne_returns400() throws Exception {
        mockMvc.perform(get("/reservation/0")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getReservationById_notFound_returns404() throws Exception {
        when(service.getReservationById(99L))
                .thenThrow(new ReservationException(APIError.RESERVATION_NOT_FOUND));

        mockMvc.perform(get("/reservation/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_existingId_returns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/reservation/1").with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_notFound_returns404() throws Exception {
        doThrow(new ReservationException(APIError.RESERVATION_NOT_FOUND)).when(service).delete(99L);

        mockMvc.perform(delete("/reservation/99").with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    void getReservationById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/reservation/1")).andExpect(status().isUnauthorized());
    }
}
