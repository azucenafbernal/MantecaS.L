package com.mantecasl.accommodationapp.business.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.ReservaDAO;

import jakarta.servlet.http.HttpSession;

@Controller
public class MisPropiedadesController {

    private static final String ATTR_USUARIO = "usuario";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_MIS_PROPIEDADES = "redirect:/mis-propiedades";

    private final InmuebleDAO inmuebleDAO;
    private final ReservaDAO reservaDAO;
    private final FavoritoDAO favoritoDAO;

    public MisPropiedadesController(InmuebleDAO inmuebleDAO,
                                   ReservaDAO reservaDAO,
                                   FavoritoDAO favoritoDAO) {
        this.inmuebleDAO = inmuebleDAO;
        this.reservaDAO = reservaDAO;
        this.favoritoDAO = favoritoDAO;
    }

    @GetMapping("/mis-propiedades")
    public String mostrarMisPropiedades(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        List<Inmueble> misPropiedades = inmuebleDAO.findByPropietarioUsuarioId(usuario.getId());
        
        model.addAttribute("propiedades", misPropiedades);
        model.addAttribute(ATTR_USUARIO, usuario);
        
        return "modificar-propiedad";
    }

    @GetMapping("/eliminar-propiedad/{id}")
    @Transactional
    public String eliminarPropiedad(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble != null && inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            try {
                // Eliminar reservas, favoritos y pagos asociados
                reservaDAO.deleteByInmuebleId(id);
                favoritoDAO.deleteByInmuebleId(id);                
                // Finalmente eliminar la propiedad
                inmuebleDAO.delete(inmueble);
                
            } catch (Exception e) {
                // Si hay error, ocultar la propiedad
                try {
                    inmueble.setPropietario(null);
                    inmuebleDAO.save(inmueble);
                } catch (Exception e2) {
                    return REDIRECT_MIS_PROPIEDADES;
                }
            }
        }

        return REDIRECT_MIS_PROPIEDADES;
    }

    @GetMapping("/editar-propiedad/{id}")
    public String mostrarEditarPropiedad(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
        }

        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble == null || !inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            return REDIRECT_MIS_PROPIEDADES;
        }

        model.addAttribute("inmueble", inmueble);
        model.addAttribute(ATTR_USUARIO, usuario);
        
        return "editar-propiedad";
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
        
        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        
        if (usuario == null) {
            return REDIRECT_LOGIN;
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

        return REDIRECT_MIS_PROPIEDADES;
    }
}