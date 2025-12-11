package com.mantecasl.accommodationapp.business.controller;

import com.mantecasl.accommodationapp.business.entity.*;
import com.mantecasl.accommodationapp.business.persistance.*;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GestorInmuebles {

    private static final String ATTR_INMUEBLE = "inmueble";
    private static final String ATTR_PROPIETARIO = "propietario";
    private static final String ATTR_USUARIO = "usuario";
    private static final String ATTR_MENSAJE = "mensaje";
    private static final String ATTR_ERROR = "error";
    private static final String ATTR_PROPIEDADES = "propiedades";

    private static final String VIEW_REGISTRO_PROPIEDAD = "registro-propiedad";
    private static final String VIEW_RESULTADO_PROPIEDAD = "resultado-propiedad";
    private static final String VIEW_LISTA_PROPIEDADES = "lista-propiedades";
    private static final String VIEW_DETALLE_INMUEBLE = "detalle-inmueble";

    private static final String MSG_USER_NOT_FOUND = "No se encontró un usuario con ese email. Debe registrarse primero.";
    private static final String MSG_PROPERTY_REGISTERED = "¡Propiedad registrada exitosamente!";
    private static final String MSG_ERROR_REGISTERING = "Error al registrar la propiedad: ";
    private static final String MSG_PROPERTY_NOT_FOUND = "Propiedad no encontrada";

    private final InmuebleDAO inmuebleDAO;
    private final UsuarioDAO usuarioDAO;
    private final PropietarioDAO propietarioDAO;

    public GestorInmuebles(InmuebleDAO inmuebleDAO,
                           UsuarioDAO usuarioDAO,
                           PropietarioDAO propietarioDAO) {
        this.inmuebleDAO = inmuebleDAO;
        this.usuarioDAO = usuarioDAO;
        this.propietarioDAO = propietarioDAO;
    }

    //Mostrar formulario de registro de propiedad
    @GetMapping("/propiedades/registro")
    public String mostrarFormulario(Model model){
        model.addAttribute(ATTR_INMUEBLE, new Inmueble());
        return VIEW_REGISTRO_PROPIEDAD;
    }
    
    //Completar los campos del registro de propiedad
    @PostMapping("/propiedades/registrar")
    @Transactional
    public String registrarPropiedad(
        @RequestParam String calle,
        @RequestParam String numero,
        @RequestParam String ciudad,
        @RequestParam String codigoPostal,
        @RequestParam double precioNoche,
        @RequestParam String descripcion,
        @RequestParam boolean reservaDirecta,
        @RequestParam Integer capacidad,
        @RequestParam String emailPropietario,
        @RequestParam String telefonoContacto, 
        @RequestParam String cuentaBancaria,
        Model model) {    

        try {
            // Buscar el usuario por email
            Usuario usuario = usuarioDAO.findByEmail(emailPropietario);
            if (usuario == null) {
                model.addAttribute(ATTR_ERROR, MSG_USER_NOT_FOUND);
                return VIEW_REGISTRO_PROPIEDAD;
            }

            // Buscar si ya existe un Propietario para este usuario
            Propietario propietario = propietarioDAO.findByUsuarioId(usuario.getId());
            if (propietario == null) {
                propietario = new Propietario();
                propietario.setUsuario(usuario);
                propietario.setTelefonoContacto(telefonoContacto);
                propietario.setCuentaBancaria(cuentaBancaria);
                propietario = propietarioDAO.save(propietario);
            } else {
                propietario.setTelefonoContacto(telefonoContacto);
                propietario.setCuentaBancaria(cuentaBancaria);
            }

            // Crear y guardar el inmueble usando los campos separados
            Inmueble inmueble = new Inmueble();
            inmueble.setCalle(calle);
            inmueble.setNumero(numero);
            inmueble.setCiudad(ciudad);
            inmueble.setCodigoPostal(codigoPostal);
            inmueble.setPrecioNoche(precioNoche);
            inmueble.setDescripcion(descripcion);
            inmueble.setCapacidad(capacidad);
            inmueble.setReservaDirecta(reservaDirecta);
            inmueble.setPropietario(propietario);

            Inmueble nuevoInmueble = inmuebleDAO.save(inmueble);

            // Agregar el inmueble a la lista del propietario
            propietario.agregarInmueble(nuevoInmueble);

            // Mandar datos a la interfaz
            model.addAttribute(ATTR_INMUEBLE, nuevoInmueble);
            model.addAttribute(ATTR_PROPIETARIO, propietario);
            model.addAttribute(ATTR_USUARIO, usuario);
            model.addAttribute(ATTR_MENSAJE, MSG_PROPERTY_REGISTERED);

            return VIEW_RESULTADO_PROPIEDAD;

        } catch (Exception e) {
            model.addAttribute(ATTR_ERROR, MSG_ERROR_REGISTERING + e.getMessage());
            return VIEW_REGISTRO_PROPIEDAD;
        }
    }


    //Listar todas las propiedades
    @GetMapping("/propiedades")
    public String listarPropiedades(Model model) {
        List<Inmueble> propiedades = inmuebleDAO.findAll();
        model.addAttribute(ATTR_PROPIEDADES, propiedades);
        return VIEW_LISTA_PROPIEDADES;
    }

    //Ver detalles de una propiedad
    @GetMapping("/propiedades/{id}")
    public String verPropiedad(@PathVariable Long id, Model model) {
        Inmueble inmueble = inmuebleDAO.findById(id).orElseThrow(() -> new RuntimeException(MSG_PROPERTY_NOT_FOUND));
        model.addAttribute(ATTR_INMUEBLE, inmueble);
        return VIEW_DETALLE_INMUEBLE;
    }
}
