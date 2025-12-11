package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Reserva;
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
            // Intentar ocultar como fallback
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
            
            System.out.println("1. Eliminando notificaciones...");
            notificacionDAO.deleteByInmuebleId(id);
            
            System.out.println("2. Eliminando solicitudes de reserva...");
            solicitudReservaDAO.deleteByInmuebleId(id);
            
            System.out.println("3. Eliminando disponibilidad...");
            disponibilidadDAO.deleteByInmuebleId(id);
            
            System.out.println("4. Eliminando favoritos...");
            favoritoDAO.deleteByInmuebleId(id);
            
            System.out.println("5. Actualizando inquilinos...");
            List<Inquilino> inquilinos = inquilinoDAO.findByInmuebleId(id);
            for (Inquilino inquilino : inquilinos) {
                inquilino.setInmueble(null);
                inquilinoDAO.save(inquilino);
            }
            
            System.out.println("6. Obteniendo reservas...");
            List<Reserva> reservas = reservaDAO.findByInmuebleId(id);
            
            System.out.println("7. Creando notificaciones para " + reservas.size() + " reservas...");
            for (Reserva reserva : reservas) {
                String motivo = "Propiedad eliminada por el propietario";
                gestorNotificaciones.crearNotificacionReservaCanceladaPorPropietario(reserva, motivo);
            }
            
            System.out.println("8. Eliminando reservas...");
            reservaDAO.deleteByInmuebleId(id);
            
            System.out.println("9. Eliminando inmueble...");
            inmuebleDAO.delete(inmueble);
            
            System.out.println("¡Eliminación completada!");
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