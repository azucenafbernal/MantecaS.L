package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Favorito;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoDAO favoritoDAO;

    @Autowired
    private InmuebleDAO inmuebleDAO;

    // ==========================================
    // MOSTRAR LISTA DE FAVORITOS
    // ==========================================
    @GetMapping
    public String mostrarFavoritos(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login?loginRequerido=true";
        }

        model.addAttribute("favoritos", favoritoDAO.findByUsuario(usuario));
        return "lista-deseos";
    }

    // ==========================================
    // AGREGAR FAVORITO
    // ==========================================
    @PostMapping("/agregar/{idInmueble}")
    public String agregarFavorito(@PathVariable Long idInmueble, HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login?loginRequerido=true";
        }

        Inmueble inmueble = inmuebleDAO.findById(idInmueble).orElse(null);

        if (inmueble == null) {
            return "redirect:/catalogo";
        }

        // Evitar duplicados
        if (favoritoDAO.existsByUsuarioAndInmueble(usuario, inmueble)) {
            return "redirect:/favoritos";
        }

        Favorito fav = new Favorito(usuario, inmueble);
        favoritoDAO.save(fav);

        return "redirect:/favoritos";
    }

    // ==========================================
    // ELIMINAR FAVORITO
    // ==========================================
    @PostMapping("/eliminar/{id}")
    public String eliminarFavorito(@PathVariable Long id, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login?loginRequerido=true";
        }

        favoritoDAO.deleteById(id);
        return "redirect:/favoritos";
    }
}
