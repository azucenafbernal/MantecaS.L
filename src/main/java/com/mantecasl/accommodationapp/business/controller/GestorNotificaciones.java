package com.mantecasl.accommodationapp.business.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Notificacion;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.NotificacionDAO;

@Service
public class GestorNotificaciones {
    
    // Constantes para URLs
    private static final String URL_RESERVAS = "/reservas/";
    private static final String URL_RESERVAS_CANCELADAS = "/reservas/canceladas";
    private static final String URL_PROPIETARIO_RESERVAS = "/propietario/reservas/";
    private static final String URL_PROPIETARIO_SOLICITUDES = "/propietario/solicitudes/";
    
    // Constantes para formatos de mensaje
    private static final String MSG_MENSAJE_PROPIETARIO = "Mensaje sobre tu reserva en %s:%n\n%s";
    
    private NotificacionDAO notificacionDAO;
    private ObjectProvider<GestorNotificaciones> selfProvider;

    public GestorNotificaciones(NotificacionDAO notificacionDAO, ObjectProvider<GestorNotificaciones> selfProvider) {
        this.notificacionDAO = notificacionDAO;
        this.selfProvider = selfProvider;
    }

    @Transactional
    public Notificacion crearNotificacionSolicitudNueva(SolicitudReserva solicitud) {
        Usuario propietario = solicitud.getInmueble().getPropietario().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(propietario);
        notificacion.setTitulo("📩 Nueva solicitud de reserva");
        notificacion.setMensaje(String.format(
            "El usuario %s ha solicitado reservar tu propiedad en %s del %s al %s",
            solicitud.getInquilino().getUsuario().getNombre(),
            solicitud.getInmueble().getDireccion(),
            solicitud.getFechaInicio(),
            solicitud.getFechaFin()
        ));
        notificacion.setTipo(Notificacion.SOLICITUD_NUEVA);
        notificacion.setInmueble(solicitud.getInmueble());
        notificacion.setSolicitud(solicitud);
        notificacion.setAccionUrl(URL_PROPIETARIO_SOLICITUDES + solicitud.getId());
        
        return notificacionDAO.save(notificacion);
    }
    
    @Transactional
    public Notificacion crearNotificacionSolicitudAprobada(SolicitudReserva solicitud) {
        Usuario inquilino = solicitud.getInquilino().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(inquilino);
        notificacion.setTitulo("✅ Solicitud de reserva APROBADA");
        notificacion.setMensaje(String.format(
            "¡Tu solicitud para reservar %s ha sido APROBADA! Las fechas son del %s al %s. " +
            "Tu pago ha sido procesado y la reserva está confirmada.",
            solicitud.getInmueble().getDireccion(),
            solicitud.getFechaInicio(),
            solicitud.getFechaFin()
        ));
        notificacion.setTipo(Notificacion.SOLICITUD_APROBADA); 
        notificacion.setInmueble(solicitud.getInmueble());
        notificacion.setSolicitud(solicitud);
        notificacion.setReserva(solicitud.getReserva());
        notificacion.setAccionUrl(URL_RESERVAS + solicitud.getReserva().getId());
        
        return notificacionDAO.save(notificacion);
    }
    
    @Transactional
    public Notificacion crearNotificacionSolicitudRechazada(SolicitudReserva solicitud, String motivo) {
        Usuario inquilino = solicitud.getInquilino().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(inquilino);
        notificacion.setTitulo("❌ Solicitud de reserva RECHAZADA");
        notificacion.setMensaje(String.format(
            "Tu solicitud para reservar %s del %s al %s ha sido RECHAZADA. " +
            "Motivo: %s",
            solicitud.getInmueble().getDireccion(),
            solicitud.getFechaInicio(),
            solicitud.getFechaFin(),
            motivo != null ? motivo : "No especificado"
        ));
        notificacion.setTipo(Notificacion.SOLICITUD_RECHAZADA);
        notificacion.setInmueble(solicitud.getInmueble());
        notificacion.setSolicitud(solicitud);
        
        return notificacionDAO.save(notificacion);
    }
    
    @Transactional
    public void notificarEliminacionPropiedad(Inmueble inmueble, List<Reserva> reservasFuturas) {
        // Notificar al propietario
        Notificacion notifPropietario = new Notificacion();
        notifPropietario.setUsuario(inmueble.getPropietario().getUsuario());
        notifPropietario.setTitulo("🏠 Propiedad eliminada");
        notifPropietario.setMensaje(String.format(
            "Has eliminado la propiedad en %s. Se han cancelado %d reservas futuras.",
            inmueble.getDireccion(),
            reservasFuturas.size()
        ));
        notifPropietario.setTipo(Notificacion.PROPIEDAD_ELIMINADA); 
        notifPropietario.setInmueble(inmueble);
        notificacionDAO.save(notifPropietario);
        
        // Notificar a cada inquilino con reservas futuras
        for (Reserva reserva : reservasFuturas) {
            Usuario inquilino = reserva.getInquilino().getUsuario();
            
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(inquilino);
            notificacion.setTitulo("⚠️ Reserva CANCELADA - Propiedad eliminada");
            notificacion.setMensaje(String.format(
                "Lamentamos informarte que tu reserva en %s del %s al %s ha sido CANCELADA " +
                "porque el propietario ha eliminado la propiedad. Tu pago ha sido reembolsado.",
                inmueble.getDireccion(),
                reserva.getFechaInicio(),
                reserva.getFechaFin()
            ));
            notificacion.setTipo(Notificacion.RESERVA_CANCELADA);
            notificacion.setInmueble(inmueble);
            notificacion.setReserva(reserva);
            notificacion.setAccionUrl(URL_RESERVAS_CANCELADAS);
            
            notificacionDAO.save(notificacion);
        }
    }
    
    @Transactional
    public Notificacion crearNotificacionPagoDevuelto(Reserva reserva, double monto) {
        Usuario inquilino = reserva.getInquilino().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(inquilino);
        notificacion.setTitulo("💰 Pago reembolsado");
        notificacion.setMensaje(String.format(
            "Se ha reembolsado €%.2f por la cancelación de tu reserva en %s. " +
            "El dinero llegará a tu cuenta en 3-5 días hábiles.",
            monto,
            reserva.getInmueble().getDireccion()
        ));
        notificacion.setTipo(Notificacion.PAGO_DEVUELTO); 
        notificacion.setInmueble(reserva.getInmueble());
        notificacion.setReserva(reserva);
        
        return notificacionDAO.save(notificacion);
    }
    
    @Transactional
    public void crearNotificacionReservaDirecta(Reserva reserva) {
        Usuario inquilino = reserva.getInquilino().getUsuario();
        Usuario propietario = reserva.getInmueble().getPropietario().getUsuario();
        
        // Notificación para el inquilino
        Notificacion notifInquilino = new Notificacion();
        notifInquilino.setUsuario(inquilino);
        notifInquilino.setTitulo("✅ Reserva confirmada");
        notifInquilino.setMensaje(String.format(
            "¡Tu reserva en %s ha sido confirmada! Fechas: %s al %s. " +
            "Pago de €%.2f procesado correctamente.",
            reserva.getInmueble().getDireccion(),
            reserva.getFechaInicio(),
            reserva.getFechaFin(),
            reserva.getPrecioTotal()
        ));
        notifInquilino.setTipo(Notificacion.RESERVA_CONFIRMADA);
        notifInquilino.setInmueble(reserva.getInmueble());
        notifInquilino.setReserva(reserva);
        notifInquilino.setAccionUrl(URL_RESERVAS + reserva.getId());
        notificacionDAO.save(notifInquilino);
        
        // Notificación para el propietario
        Notificacion notifPropietario = new Notificacion();
        notifPropietario.setUsuario(propietario);
        notifPropietario.setTitulo("💰 Nueva reserva directa");
        notifPropietario.setMensaje(String.format(
            "El usuario %s ha realizado una reserva directa en %s del %s al %s. " +
            "Importe: €%.2f",
            inquilino.getNombre(),
            reserva.getInmueble().getDireccion(),
            reserva.getFechaInicio(),
            reserva.getFechaFin(),
            reserva.getPrecioTotal()
        ));
        notifPropietario.setTipo(Notificacion.RESERVA_CONFIRMADA); 
        notifPropietario.setInmueble(reserva.getInmueble());
        notifPropietario.setReserva(reserva);
        notifPropietario.setAccionUrl(URL_PROPIETARIO_RESERVAS + reserva.getId());
        notificacionDAO.save(notifPropietario);
    }
    
    @Transactional
    public Notificacion crearNotificacionReservaCanceladaPorPropietario(Reserva reserva, String motivo, String direccionInmueble) {
        Usuario inquilino = reserva.getInquilino().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(inquilino);
        notificacion.setTitulo("⚠️ Reserva CANCELADA por el propietario");
        notificacion.setMensaje(String.format(
            "Tu reserva en %s del %s al %s ha sido CANCELADA por el propietario. " +
            "Motivo: %s. Tu pago será reembolsado en un plazo inferior a 48 horas.",
            direccionInmueble,
            reserva.getFechaInicio(),
            reserva.getFechaFin(),
            motivo != null ? motivo : "No especificado"
        ));
        notificacion.setTipo(Notificacion.RESERVA_CANCELADA);
        notificacion.setAccionUrl(URL_RESERVAS_CANCELADAS);
        
        // No establecer relaciones que ya no existirán
        notificacion.setInmueble(null);
        notificacion.setReserva(null);
        
        // No establecer relaciones que ya no existirán
        notificacion.setInmueble(null);
        notificacion.setReserva(null);
        
        return notificacionDAO.save(notificacion);
    }
    
    @Transactional
    public Notificacion crearNotificacionSolicitudRechazadaPorEliminacion(SolicitudReserva solicitud, String direccionInmueble) {
        Usuario solicitante = solicitud.getInquilino().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(solicitante);
        notificacion.setTitulo("❌ Solicitud de reserva RECHAZADA");
        notificacion.setMensaje(String.format(
            "Tu solicitud de reserva para %s ha sido rechazada porque la propiedad ha sido eliminada por el propietario.",
            direccionInmueble
        ));
        notificacion.setTipo(Notificacion.SOLICITUD_RECHAZADA);
        notificacion.setAccionUrl("/mis-solicitudes");
        
        notificacion.setInmueble(null);
        notificacion.setReserva(null);
        
        return notificacionDAO.save(notificacion);
    }

    
    @Transactional
    public Notificacion crearNotificacionMensajePropietario(Reserva reserva, String mensaje) {
        Usuario inquilino = reserva.getInquilino().getUsuario();
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(inquilino);
        notificacion.setTitulo("💬 Mensaje del propietario");
        notificacion.setMensaje(String.format(
            MSG_MENSAJE_PROPIETARIO,
            reserva.getInmueble().getDireccion(),
            mensaje
        ));
        notificacion.setTipo(Notificacion.MENSAJE_PROPIETARIO);
        notificacion.setInmueble(reserva.getInmueble());
        notificacion.setReserva(reserva);
        notificacion.setAccionUrl(URL_RESERVAS + reserva.getId());
        
        return notificacionDAO.save(notificacion);
    }
    
    public List<Notificacion> obtenerNotificacionesUsuario(Usuario usuario) {
        return notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }
    
    public List<Notificacion> obtenerNotificacionesNoLeidas(Usuario usuario) {
        return notificacionDAO.findByUsuarioAndLeidaFalseOrderByFechaCreacionDesc(usuario);
    }

    public List<Notificacion> obtenerNotificacionesRecientes(Usuario usuario, int dias) {
        Date fechaLimite = new Date(System.currentTimeMillis() - (dias * 24 * 60 * 60 * 1000L));
        return notificacionDAO.findByUsuarioOrderByFechaCreacionDesc(usuario)
                .stream()
                .filter(n -> n.getFechaCreacion().isAfter(
                        fechaLimite.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .toList();
    }
    
    public long contarNotificacionesNoLeidas(Usuario usuario) {
        return notificacionDAO.countByUsuarioAndLeidaFalse(usuario);
    }
    
    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        notificacionDAO.findById(notificacionId).ifPresent(notificacion -> {
            notificacion.setLeida(true);
            notificacionDAO.save(notificacion);
        });
    }
    
    @Transactional
    public void marcarTodasComoLeidas(Usuario usuario) {
        List<Notificacion> noLeidas = selfProvider.getObject().obtenerNotificacionesNoLeidas(usuario);
        for (Notificacion notificacion : noLeidas) {
            notificacion.setLeida(true);
        }
        notificacionDAO.saveAll(noLeidas);
    }
    
    @Transactional
    public void eliminarNotificacion(Long notificacionId) {
        notificacionDAO.deleteById(notificacionId);
    }
    
    @Transactional
    public void eliminarTodasLasNotificaciones(Usuario usuario) {
        List<Notificacion> notificaciones = selfProvider.getObject().obtenerNotificacionesUsuario(usuario);
        notificacionDAO.deleteAll(notificaciones);
    }
    
    public boolean tieneNotificacionesNuevas(Usuario usuario) {
        return contarNotificacionesNoLeidas(usuario) > 0;
    }
    
    public List<Notificacion> obtenerUltimasNotificaciones(Usuario usuario, int limite) {
        List<Notificacion> todas = obtenerNotificacionesUsuario(usuario);
        if (todas.size() > limite) {
            return todas.subList(0, limite);
        }
        return todas;
    }
    
    @Transactional
    public Notificacion notificarNuevaSolicitudPropietario(SolicitudReserva solicitud) {
        return selfProvider.getObject().crearNotificacionSolicitudNueva(solicitud);
    }
    
    @Transactional
    public Notificacion notificarAprobacionInquilino(SolicitudReserva solicitud) {
        return selfProvider.getObject().crearNotificacionSolicitudAprobada(solicitud);
    }
    
    @Transactional
    public Notificacion notificarRechazoInquilino(SolicitudReserva solicitud, String motivo) {
        return selfProvider.getObject().crearNotificacionSolicitudRechazada(solicitud, motivo);
    }
}