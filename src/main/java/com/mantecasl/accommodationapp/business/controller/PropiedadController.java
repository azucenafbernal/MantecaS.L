package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PropiedadController {

    @Autowired
    private InmuebleDAO inmuebleDAO;

    @GetMapping("/catalogo")
    public String verCatalogo(Model model) {
        // Obtenemos todos los inmuebles de la base de datos
        List<Inmueble> lista = inmuebleDAO.findAll();
        model.addAttribute("propiedades", lista);
        return "lista-propiedades";
    }
}
