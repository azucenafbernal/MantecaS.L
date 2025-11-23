package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class MisPropiedadesController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @GetMapping("/mis-propiedades")
    public String mostrarMisPropiedades(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener todas las propiedades del usuario actual
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

        // Verificar que la propiedad pertenece al usuario
        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble != null && inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            inmuebleDAO.delete(inmueble);
        }

        return "redirect:/mis-propiedades";
    }

    @GetMapping("/editar-propiedad/{id}")
    public String mostrarEditarPropiedad(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }

        // Verificar que la propiedad pertenece al usuario
        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble == null || !inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/mis-propiedades";
        }

        model.addAttribute("inmueble", inmueble);
        model.addAttribute("usuario", usuario);
        
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
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return "redirect:/login";
        }

        // Verificar que la propiedad pertenece al usuario
        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);
        if (inmueble != null && inmueble.getPropietario().getUsuario().getId().equals(usuario.getId())) {
            // Actualizar los datos
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