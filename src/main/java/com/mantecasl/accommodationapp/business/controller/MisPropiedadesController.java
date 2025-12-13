package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional; 
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class MisPropiedadesController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private FavoritoDAO favoritoDAO;

    @Autowired
    private GestorNotificaciones gestorNotificaciones;

    @Autowired
    private InquilinoDAO inquilinoDAO;

    @Autowired
    private DisponibilidadDAO disponibilidadDAO;

    @Autowired
    private SolicitudReservaDAO solicitudReservaDAO;

    @Autowired
    private NotificacionDAO notificacionDAO;

    @GetMapping("/mis-propiedades")
    public String mostrarMisPropiedades(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Inmueble> misPropiedades = inmuebleDAO.findByPropietarioUsuarioId(usuario.getId());
        
        model.addAttribute("propiedades", misPropiedades);
        model.addAttribute("usuario", usuario);
        
        return "modificar-propiedad";
    }

    @GetMapping("/eliminar-propiedad/{id}")
    public String eliminarPropiedad(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            eliminarPropiedadTransactional(id, usuario);
        } catch (Exception e) {
            System.err.println("Error en eliminarPropiedad: " + e.getMessage());
            e.printStackTrace();
            try {
                Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
                if (inmueble != null) {
                    inmueble.setPropietario(null);
                    inmuebleDAO.save(inmueble);
                }
            } catch (Exception e2) {
                System.err.println("Error en fallback: " + e2.getMessage());
            }
        }

        return "redirect:/mis-propiedades";
    }

    @Transactional
    public void eliminarPropiedadTransactional(Long id, Usuario usuario) {
        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble != null && inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            
            // Guardar información del inmueble antes de eliminarlo
            String direccionInmueble = inmueble.getDireccion();
            
            // 1. Notificar reservas
            List<Reserva> reservas = reservaDAO.findByInmuebleId(id);
            for (Reserva reserva : reservas) {
                String motivo = "Propiedad eliminada por el propietario";
                gestorNotificaciones.crearNotificacionReservaCanceladaPorPropietario(
                    reserva, motivo, direccionInmueble
                );
            }
            
            // 2. Notificar solicitudes pendientes
            List<SolicitudReserva> solicitudes = solicitudReservaDAO.findByInmuebleId(id);
            for (SolicitudReserva solicitud : solicitudes) {
                gestorNotificaciones.crearNotificacionSolicitudRechazadaPorEliminacion(
                    solicitud, direccionInmueble
                );
            }
            
            // 3. Ahora eliminar todo (en el orden correcto)
            notificacionDAO.deleteByInmuebleId(id);
            solicitudReservaDAO.deleteByInmuebleId(id);
            disponibilidadDAO.deleteByInmuebleId(id);
            favoritoDAO.deleteByInmuebleId(id);
            
            List<Inquilino> inquilinos = inquilinoDAO.findByInmuebleId(id);
            for (Inquilino inquilino : inquilinos) {
                inquilino.setInmueble(null);
                inquilinoDAO.save(inquilino);
            }
            
            reservaDAO.deleteByInmuebleId(id);
            inmuebleDAO.delete(inmueble);
        }
    }

    @PostMapping("/actualizar-propiedad")
    public String actualizarPropiedad(@RequestParam Long id,
                                        @RequestParam String calle,
                                        @RequestParam String numero,
                                        @RequestParam String ciudad,
                                        @RequestParam String codigoPostal,
                                        @RequestParam double precioNoche,
                                        @RequestParam Integer capacidad,
                                        @RequestParam String descripcion,
                                        HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }

        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble != null && inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            inmueble.setCalle(calle);
            inmueble.setNumero(numero);
            inmueble.setCiudad(ciudad);
            inmueble.setCodigoPostal(codigoPostal);
            inmueble.setPrecioNoche(precioNoche);
            inmueble.setCapacidad(capacidad);
            inmueble.setDescripcion(descripcion);
            
            inmuebleDAO.save(inmueble);
        }

        return "redirect:/mis-propiedades";
    }
}