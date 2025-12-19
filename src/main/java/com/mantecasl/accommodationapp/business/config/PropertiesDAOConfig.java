package com.mantecasl.accommodationapp.business.config;

import org.springframework.stereotype.Component;

import com.mantecasl.accommodationapp.business.persistance.DisponibilidadDAO;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.InquilinoDAO;
import com.mantecasl.accommodationapp.business.persistance.NotificacionDAO;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;
import com.mantecasl.accommodationapp.business.persistance.SolicitudReservaDAO;

/**
 * Agrupa todos los DAOs relacionados con propiedades para reducir el número de parámetros
 * en constructores y mejorar la mantenibilidad del código.
 */
@Component
public class PropertiesDAOConfig {
    
    private InmuebleDAO inmuebleDAO;
    private ReservaDAO reservaDAO;
    private FavoritoDAO favoritoDAO;
    private InquilinoDAO inquilinoDAO;
    private DisponibilidadDAO disponibilidadDAO;
    private SolicitudReservaDAO solicitudReservaDAO;
    private NotificacionDAO notificacionDAO;
    
    public PropertiesDAOConfig(InmuebleDAO inmuebleDAO,
                               ReservaDAO reservaDAO,
                               FavoritoDAO favoritoDAO,
                               InquilinoDAO inquilinoDAO,
                               DisponibilidadDAO disponibilidadDAO,
                               SolicitudReservaDAO solicitudReservaDAO,
                               NotificacionDAO notificacionDAO) {
        this.inmuebleDAO = inmuebleDAO;
        this.reservaDAO = reservaDAO;
        this.favoritoDAO = favoritoDAO;
        this.inquilinoDAO = inquilinoDAO;
        this.disponibilidadDAO = disponibilidadDAO;
        this.solicitudReservaDAO = solicitudReservaDAO;
        this.notificacionDAO = notificacionDAO;
    }
    
    public InmuebleDAO getInmuebleDAO() {
        return inmuebleDAO;
    }
    
    public ReservaDAO getReservaDAO() {
        return reservaDAO;
    }
    
    public FavoritoDAO getFavoritoDAO() {
        return favoritoDAO;
    }
    
    public InquilinoDAO getInquilinoDAO() {
        return inquilinoDAO;
    }
    
    public DisponibilidadDAO getDisponibilidadDAO() {
        return disponibilidadDAO;
    }
    
    public SolicitudReservaDAO getSolicitudReservaDAO() {
        return solicitudReservaDAO;
    }
    
    public NotificacionDAO getNotificacionDAO() {
        return notificacionDAO;
    }
}
