package com.mantecasl.accommodationapp.business.config;

import static org.junit.jupiter.api.Assertions.*;

import com.mantecasl.accommodationapp.business.persistance.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PropertiesDAOConfigTest {

    @Test
    void constructor_y_getters_devuelven_los_DAOs_correctos() {

        // Arrange: mocks de todos los DAOs
        InmuebleDAO inmuebleDAO = Mockito.mock(InmuebleDAO.class);
        ReservaDAO reservaDAO = Mockito.mock(ReservaDAO.class);
        FavoritoDAO favoritoDAO = Mockito.mock(FavoritoDAO.class);
        InquilinoDAO inquilinoDAO = Mockito.mock(InquilinoDAO.class);
        DisponibilidadDAO disponibilidadDAO = Mockito.mock(DisponibilidadDAO.class);
        SolicitudReservaDAO solicitudReservaDAO = Mockito.mock(SolicitudReservaDAO.class);
        NotificacionDAO notificacionDAO = Mockito.mock(NotificacionDAO.class);

        // Act: crear el config
        PropertiesDAOConfig config = new PropertiesDAOConfig(
                inmuebleDAO,
                reservaDAO,
                favoritoDAO,
                inquilinoDAO,
                disponibilidadDAO,
                solicitudReservaDAO,
                notificacionDAO);

        // Assert: todos los getters devuelven exactamente lo inyectado
        assertSame(inmuebleDAO, config.getInmuebleDAO());
        assertSame(reservaDAO, config.getReservaDAO());
        assertSame(favoritoDAO, config.getFavoritoDAO());
        assertSame(inquilinoDAO, config.getInquilinoDAO());
        assertSame(disponibilidadDAO, config.getDisponibilidadDAO());
        assertSame(solicitudReservaDAO, config.getSolicitudReservaDAO());
        assertSame(notificacionDAO, config.getNotificacionDAO());
    }
}
