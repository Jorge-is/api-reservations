package com.edteam.reservations.service;

import com.edteam.reservations.connector.CatalogConnector;
import com.edteam.reservations.connector.response.CityDTO;
import com.edteam.reservations.dto.ItineraryDTO;
import com.edteam.reservations.dto.ReservationDTO;
import com.edteam.reservations.dto.SegmentDTO;
import com.edteam.reservations.enums.APIError;
import com.edteam.reservations.exception.ReservationException;
import com.edteam.reservations.model.Reservation;
import com.edteam.reservations.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private ConversionService conversionService;

    @Mock
    private CatalogConnector catalogConnector;

    @InjectMocks
    private ReservationService service;

    @Test
    void getReservationById_existingId_returnsDTO() {
        Reservation entity = new Reservation();
        ReservationDTO dto = new ReservationDTO();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(conversionService.convert(entity, ReservationDTO.class)).thenReturn(dto);

        assertEquals(dto, service.getReservationById(1L));
        verify(repository).findById(1L);
    }

    @Test
    void getReservationById_nonExistingId_throwsNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ReservationException ex = assertThrows(ReservationException.class,
                () -> service.getReservationById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(APIError.RESERVATION_NOT_FOUND.getMessage(), ex.getDescription());
    }

    @Test
    void save_dtoWithId_throwsBadRequestException() {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1L);

        ReservationException ex = assertThrows(ReservationException.class, () -> service.save(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void save_validReservation_returnsPersistedDTO() {
        ReservationDTO dto = buildReservationDTO();
        Reservation entity = new Reservation();
        CityDTO city = buildCityDTO("MAD");

        when(catalogConnector.getCity("MAD")).thenReturn(city);
        when(catalogConnector.getCity("BCN")).thenReturn(buildCityDTO("BCN"));
        when(conversionService.convert(dto, Reservation.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(conversionService.convert(entity, ReservationDTO.class)).thenReturn(dto);

        assertEquals(dto, service.save(dto));
        verify(repository).save(entity);
    }

    @Test
    void save_cityWithNullCode_throwsCityNotFoundException() {
        ReservationDTO dto = buildReservationDTO();

        when(catalogConnector.getCity("MAD")).thenReturn(new CityDTO());
        when(catalogConnector.getCity("BCN")).thenReturn(buildCityDTO("BCN"));

        ReservationException ex = assertThrows(ReservationException.class, () -> service.save(dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void update_nonExistingId_throwsNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationException.class,
                () -> service.update(99L, new ReservationDTO()));

        verify(repository, never()).save(any());
    }

    @Test
    void delete_existingId_callsDeleteById() {
        Reservation entity = new Reservation();
        ReservationDTO dto = new ReservationDTO();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(conversionService.convert(entity, ReservationDTO.class)).thenReturn(dto);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationException.class, () -> service.delete(99L));

        verify(repository, never()).deleteById(any());
    }

    private ReservationDTO buildReservationDTO() {
        SegmentDTO segment = new SegmentDTO();
        segment.setOrigin("MAD");
        segment.setDestination("BCN");

        ItineraryDTO itinerary = new ItineraryDTO();
        itinerary.setSegments(List.of(segment));

        ReservationDTO dto = new ReservationDTO();
        dto.setItinerary(itinerary);
        return dto;
    }

    private CityDTO buildCityDTO(String code) {
        CityDTO city = new CityDTO();
        city.setCode(code);
        return city;
    }
}
