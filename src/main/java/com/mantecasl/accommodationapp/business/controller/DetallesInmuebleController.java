package com.mantecasl.accommodationapp.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.entity.Inmueble;

@Controller
public class DetallesInmuebleController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @GetMapping("/gestor/propiedad/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        Inmueble inmueble = inmuebleDAO.findById(id).orElse(null);

        if (inmueble == null) {
            // Redirige a la lista si no se encuentra el inmueble
            return "redirect:/lista-propiedades";
        }

        model.addAttribute("inmueble", inmueble);
        return "detalle-inmueble"; // Thymeleaf buscará detalle-inmueble.html
    }
}
