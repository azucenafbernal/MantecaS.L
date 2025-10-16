package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.Inmueble;
import com.mantecasl.accommodationapp.business.entity.Usuario;
import com.mantecasl.accommodationapp.business.persistance.InmuebleDAO;
import com.mantecasl.accommodationapp.business.persistance.UsuarioDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GestorInmuebles {
    @Autowired
    private InmuebleDAO inmuebleDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    //Mostrar formulario de registro de propiedad
    @GetMapping("/propiedades/registro")
    public String mostrarFormulario(Model model){
        model.addAttribute("inmueble", new Inmueble());
        return "registro-propiedad";
    }
    
    //Completar los campos del registro de propiedad
    @PostMapping("/propiedades/registrar")
public String registrarPropiedad(
    @RequestParam String direccion,
    @RequestParam double precioNoche,
    @RequestParam String descripcion,
    @RequestParam Integer capacidad,
    @RequestParam String emailPropietario,
    @RequestParam String telefonoContacto, 
    @RequestParam String cuentaBancaria,
    Model model) {    

    //Buscar el usuario por email
    Usuario usuario = usuarioDAO.findByEmail(emailPropietario);
    
    if (usuario == null) {
        model.addAttribute("error", "No se encontró un usuario con ese email. Debe registrarse primero.");
        return "registro-propiedad";
    }
    
    //Si el usuario no es propietario, actualizar sus datos
    if (!usuario.esPropietario()) {
        usuario.setTelefonoContacto(telefonoContacto);
        usuario.setCuentaBancaria(cuentaBancaria);
        usuario = usuarioDAO.save(usuario);
    }
    
    //Crear y guardar el inmueble
    Inmueble inmueble = new Inmueble(direccion, precioNoche, descripcion, capacidad, usuario);
    Inmueble nuevoInmueble = inmuebleDAO.save(inmueble);
        
    model.addAttribute("inmueble", nuevoInmueble);
    model.addAttribute("usuario", usuario);
    model.addAttribute("mensaje", "¡Propiedad registrada exitosamente!");
            
    return "resultado-propiedad";
}

    //Listar todas las propiedades
    @GetMapping("/propiedades")
    public String listarPropiedades(Model model) {
        model.addAttribute("propiedades", inmuebleDAO.findAll());
        return "lista-propiedades";
    }

    //Ver detalles de una propiedad
    @GetMapping("/propiedades/{id}")
    public String verPropiedad(@PathVariable Long id, Model model) {
        Inmueble inmueble = inmuebleDAO.findById(id).orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));
        model.addAttribute("inmueble", inmueble);
        return "detalle-propiedad";
    }
}
