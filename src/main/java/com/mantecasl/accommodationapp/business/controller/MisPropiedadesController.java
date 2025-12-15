package com.mantecasl.accommodationapp.business.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.config.PropertiesDAOConfig;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Inquilino;
import com.mantecasl.accommodationapp.business.entity.Reserva;
import com.mantecasl.accommodationapp.business.entity.SolicitudReserva;
import com.mantecasl.accommodationapp.business.entity.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
public class MisPropiedadesController {
    
    private static final Logger logger = LoggerFactory.getLogger(MisPropiedadesController.class);
    
    // Constantes para atributos del modelo
    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_PROPIEDADES = "propiedades";
    
    // Constantes para vistas
    private static final String VIEW_MODIFICAR_PROPIEDAD = "modificar-propiedad";
    
    // Constantes para redirects
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_MIS_PROPIEDADES = "redirect:/mis-propiedades";
    
    private PropertiesDAOConfig daoConfig;
    private GestorNotificaciones gestorNotificaciones;
    private ObjectProvider<MisPropiedadesController> selfProvider;

    public MisPropiedadesController(PropertiesDAOConfig daoConfig,
                                    GestorNotificaciones gestorNotificaciones,
                                    ObjectProvider<MisPropiedadesController> selfProvider) {
        this.daoConfig = daoConfig;
        this.gestorNotificaciones = gestorNotificaciones;
        this.selfProvider = selfProvider;
    }

    @GetMapping("/mis-propiedades")
    public String mostrarMisPropiedades(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        List<Inmueble> misPropiedades = daoConfig.getInmuebleDAO().findByPropietarioUsuarioId(usuario.getId());
        
        model.addAttribute(ATTR_PROPIEDADES, misPropiedades);
        model.addAttribute(ATTR_USUARIO, usuario);
        
        return VIEW_MODIFICAR_PROPIEDAD;
    }

    @GetMapping("/editar-propiedad/{id}")
    public String editarPropiedad(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        Inmueble inmueble = daoConfig.getInmuebleDAO().findById(id).orElse(null);
        if (inmueble == null || !inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            return REDIRECT_MIS_PROPIEDADES;
        }

        model.addAttribute("inmueble", inmueble);
        return "editar-propiedad";
    }

    @GetMapping("/eliminar-propiedad/{id}")
    public String eliminarPropiedad(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        try {
            selfProvider.getObject().eliminarPropiedadTransactional(id, usuario);
        } catch (Exception e) {
            logger.error("Error en eliminarPropiedad: {}", e.getMessage(), e);
            try {
                Inmueble inmueble = daoConfig.getInmuebleDAO().findById(id).orElse(null);
                if (inmueble != null) {
                    inmueble.setPropietario(null);
                    daoConfig.getInmuebleDAO().save(inmueble);
                }
            } catch (Exception e2) {
                logger.error("Error en fallback: {}", e2.getMessage(), e2);
            }
        }

        return REDIRECT_MIS_PROPIEDADES;
    }

    @Transactional
    public void eliminarPropiedadTransactional(Long id, Usuario usuario) {
        Inmueble inmueble = daoConfig.getInmuebleDAO().findById(id).orElse(null);
        if (inmueble != null && inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            
            // Guardar información del inmueble antes de eliminarlo
            String direccionInmueble = inmueble.getDireccion();
            
            // 1. Notificar reservas
            List<Reserva> reservas = daoConfig.getReservaDAO().findByInmuebleId(id);
            for (Reserva reserva : reservas) {
                String motivo = "Propiedad eliminada por el propietario";
                gestorNotificaciones.crearNotificacionReservaCanceladaPorPropietario(
                    reserva, motivo, direccionInmueble
                );
            }
            
            // 2. Notificar solicitudes pendientes
            List<SolicitudReserva> solicitudes = daoConfig.getSolicitudReservaDAO().findByInmuebleId(id);
            for (SolicitudReserva solicitud : solicitudes) {
                gestorNotificaciones.crearNotificacionSolicitudRechazadaPorEliminacion(
                    solicitud, direccionInmueble
                );
            }
            daoConfig.getNotificacionDAO().deleteByInmuebleId(id);
            daoConfig.getSolicitudReservaDAO().deleteByInmuebleId(id);
            daoConfig.getDisponibilidadDAO().deleteByInmuebleId(id);
            daoConfig.getFavoritoDAO().deleteByInmuebleId(id);
            
            List<Inquilino> inquilinos = daoConfig.getInquilinoDAO().findByInmuebleId(id);
            for (Inquilino inquilino : inquilinos) {
                inquilino.setInmueble(null);
                daoConfig.getInquilinoDAO().save(inquilino);
            }
            
            daoConfig.getReservaDAO().deleteByInmuebleId(id);
            daoConfig.getInmuebleDAO().delete(inmueble);
        }
    }

    @PostMapping("/actualizar-propiedad")
    @Transactional
    public String actualizarPropiedad(@RequestParam Long id,
                                        @RequestParam String calle,
                                        @RequestParam String numero,
                                        @RequestParam String ciudad,
                                        @RequestParam String codigoPostal,
                                        @RequestParam double precioNoche,
                                        @RequestParam Integer capacidad,
                                        @RequestParam(required = false, defaultValue = "") String descripcion,
                                        @RequestParam String politicaCancelacion,
                                        @RequestParam(required = false) String[] comodidades,
                                        HttpSession session,
                                        Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            logger.warn("Intento de actualizar propiedad sin usuario autenticado");
            return REDIRECT_LOGIN;
        }

        try {
            Inmueble inmueble = daoConfig.getInmuebleDAO().findById(id).orElse(null);
            
            if (inmueble == null) {
                logger.error("Propiedad no encontrada: {}", id);
                model.addAttribute("error", "Propiedad no encontrada");
                model.addAttribute("inmueble", new Inmueble());
                return "editar-propiedad";
            }
            
            if (!inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
                logger.warn("Usuario {} intenta actualizar propiedad que no le pertenece", usuario.getId());
                model.addAttribute("error", "No tienes permiso para actualizar esta propiedad");
                model.addAttribute("inmueble", inmueble);
                return "editar-propiedad";
            }
            
            inmueble.setCalle(calle);
            inmueble.setNumero(numero);
            inmueble.setCiudad(ciudad);
            inmueble.setCodigoPostal(codigoPostal);
            inmueble.setPrecioNoche(precioNoche);
            inmueble.setCapacidad(capacidad);
            inmueble.setDescripcion(descripcion);
            inmueble.setPoliticaCancelacion(politicaCancelacion);
            
            // Actualizar comodidades (limpiar valores vacíos y nulos)
            if (comodidades != null) {
                java.util.List<String> comodidadesLimpias = new java.util.ArrayList<>();
                for (String c : comodidades) {
                    if (c != null && !c.trim().isEmpty()) {
                        comodidadesLimpias.add(c.trim());
                    }
                }
                inmueble.setComodidades(comodidadesLimpias);
            } else {
                inmueble.setComodidades(new java.util.ArrayList<>());
            }
            
            daoConfig.getInmuebleDAO().save(inmueble);
            logger.info("Propiedad {} actualizada exitosamente por usuario {}", id, usuario.getId());
            
            // Redirigir con mensaje de éxito
            return "redirect:/mis-propiedades?success=true";
            
        } catch (Exception e) {
            logger.error("Error actualizando propiedad {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error al actualizar la propiedad: " + e.getMessage());
            Inmueble inmueble = daoConfig.getInmuebleDAO().findById(id).orElse(new Inmueble());
            model.addAttribute("inmueble", inmueble);
            return "editar-propiedad";
        }
    }
}