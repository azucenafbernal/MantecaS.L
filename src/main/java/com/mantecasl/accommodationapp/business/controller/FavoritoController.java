package com.mantecasl.accommodationapp.business.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mantecasl.accommodationapp.business.entity.Favorito;
import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.FavoritoDAO;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/favoritos")
public class FavoritoController {

    private static final String ATTR_USUARIO = "usuario";
    private static final String REDIRECT_LOGIN_REQUIRED = "redirect:/login?loginRequerido=true";
    private static final String REDIRECT_FAVORITOS = "redirect:/favoritos";
    private static final String REDIRECT_CATALOGO = "redirect:/catalogo";

    private final FavoritoDAO favoritoDAO;
    private final InmuebleDAO inmuebleDAO;

    public FavoritoController(FavoritoDAO favoritoDAO, InmuebleDAO inmuebleDAO) {
        this.favoritoDAO = favoritoDAO;
        this.inmuebleDAO = inmuebleDAO;
    }

    // MOSTRAR LISTA DE FAVORITOS 
    @GetMapping
    public String mostrarFavoritos(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);

        if (usuario == null) {
            return REDIRECT_LOGIN_REQUIRED;
        }

        // Obtener todos los favoritos del usuario
        List<Favorito> todosFavoritos = favoritoDAO.findByUsuario(usuario);
        
        // Filtrar solo los favoritos donde el inmueble todavía existe y tiene propietario
        List<Favorito> favoritosValidos = todosFavoritos.stream()
                .filter(fav -> {
                    try {
                        Inmueble inmueble = fav.getInmueble();
                        // Verificar que el inmueble existe, tiene propietario y el propietario tiene usuario
                        return inmueble != null && 
                               inmueble.getPropietario() != null && 
                               inmueble.getPropietario().getUsuario() != null &&
                               inmuebleDAO.existsById(inmueble.getId());
                    } catch (Exception e) {
                        // Si hay cualquier error, excluir este favorito
                        return false;
                    }
                })
                .toList();

        model.addAttribute("favoritos", favoritosValidos);
        return "lista-deseos";
    }


    // AGREGAR FAVORITO
    @PostMapping("/agregar/{idInmueble}")
    public String agregarFavorito(@PathVariable Long idInmueble, HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) {
            return REDIRECT_LOGIN_REQUIRED;
        }

        Inmueble inmueble = inmuebleDAO.findById(idInmueble).orElse(null);

        if (inmueble == null) {
            return REDIRECT_CATALOGO;
        }

        // Evitar duplicados
        if (favoritoDAO.existsByUsuarioAndInmueble(usuario, inmueble)) {
            return REDIRECT_FAVORITOS;
        }

        Favorito fav = new Favorito(usuario, inmueble);
        favoritoDAO.save(fav);

        return REDIRECT_FAVORITOS;
    }

    // ELIMINAR FAVORITO
    @PostMapping("/eliminar/{id}")
    public String eliminarFavorito(@PathVariable Long id, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute(ATTR_USUARIO);
        if (usuario == null) {
            return REDIRECT_LOGIN_REQUIRED;
        }

        favoritoDAO.deleteById(id);
        return REDIRECT_FAVORITOS;
    }
}