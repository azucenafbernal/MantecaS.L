package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.transaction.Transactional;
import java.util.List;

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

    @Autowired
    private PropietarioDAO propietarioDAO;

    //Mostrar formulario de registro de propiedad
    @GetMapping("/propiedades/registro")
    public String mostrarFormulario(Model model){
        model.addAttribute("inmueble", new Inmueble());
        return "registro-propiedad";
    }
    
    //Completar los campos del registro de propiedad
    @PostMapping("/propiedades/registrar")
    @Transactional
    public String registrarPropiedad(
        @RequestParam String direccion,
        @RequestParam double precioNoche,
        @RequestParam String descripcion,
        @RequestParam Integer capacidad,
        @RequestParam String emailPropietario,
        @RequestParam String telefonoContacto, 
        @RequestParam String cuentaBancaria,
        Model model) {    

        try {
            //Buscar el usuario por email
            Usuario usuario = usuarioDAO.findByEmail(emailPropietario);
            
            if (usuario == null) {
                model.addAttribute("error", "No se encontró un usuario con ese email. Debe registrarse primero.");
                return "registro-propiedad";
            }
            
            //Buscar si ya existe un Propietario para este usuario
            Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
            
            if (propietario == null) {
                //Crear nuevo Propietario
                propietario = new Propietario();
                propietario.setUsuario(usuario);
                propietario.setTelefonoContacto(telefonoContacto);
                propietario.setCuentaBancaria(cuentaBancaria);
                propietario = propietarioDAO.save(propietario);
            } else {
                //Opcional: Actualizar datos si el propietario ya existe
                propietario.setTelefonoContacto(telefonoContacto);
                propietario.setCuentaBancaria(cuentaBancaria);
            }

            //Crear y guardar el inmueble
            Inmueble inmueble = new Inmueble();
            inmueble.setDireccion(direccion);
            inmueble.setPrecioNoche(precioNoche);
            inmueble.setDescripcion(descripcion);
            inmueble.setCapacidad(capacidad);
            inmueble.setPropietario(propietario);
            
            Inmueble nuevoInmueble = inmuebleDAO.save(inmueble);

            //Agregar el inmueble a la lista del propietario
            propietario.agregarInmueble(nuevoInmueble);

            //Mandar datos a la interfaz
            model.addAttribute("inmueble", nuevoInmueble);
            model.addAttribute("propietario", propietario);
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensaje", "¡Propiedad registrada exitosamente!");
                    
            return "resultado-propiedad";

        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar la propiedad: " + e.getMessage());
            return "registro-propiedad";
        }
    }

    //Listar todas las propiedades
    @GetMapping("/propiedades")
    public String listarPropiedades(Model model) {
        List<Inmueble> propiedades = inmuebleDAO.findAll();
        model.addAttribute("propiedades", propiedades);
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
