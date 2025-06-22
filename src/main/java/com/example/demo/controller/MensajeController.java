package com.example.demo.controller;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.example.demo.entity.ChatEntity;
import com.example.demo.entity.CuentaEntity;
import com.example.demo.entity.MensajeEntity;
import com.example.demo.entity.PrioridadEntity;

import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.MensajeRepository;
import com.example.demo.repository.PrioridadRepository;

@Controller
public class MensajeController {

	@Autowired
	private PrioridadRepository prioridadRepository;

	@Autowired
	private ChatRepository chatRepository;

	@Autowired
	private MensajeRepository mensajeRepository;

	@GetMapping("/chat")
	public String showChat(Model model, HttpSession session,
	        @RequestParam(value = "codChat", required = false, defaultValue = "0") Integer codChat) {

	    CuentaEntity usuario = (CuentaEntity) session.getAttribute("usuario");

	    if (usuario == null) {
	        return "redirect:/login";
	    }

	    // Datos de sesión
	    model.addAttribute("sessionUsuario", usuario);
	    model.addAttribute("sessionCodUsu", usuario.getCodUsuario());

	    // Datos comunes
	    model.addAttribute("chatEntity", new ChatEntity());
	    model.addAttribute("mensaje", new MensajeEntity());
	    List<PrioridadEntity> lstPrioridad = prioridadRepository.findAll();
	    model.addAttribute("lstPrioridad", lstPrioridad);

	    // Cargar chats según el tipo de usuario
	    List<ChatEntity> lstChat;
	    Integer codPrioridad = (Integer) session.getAttribute("codPrioridad");

	    String tipoUsuario = usuario.getTipo().getNomTipo().toLowerCase();
	    System.out.println("Tipo de usuario logueado: " + tipoUsuario);
	    if ("asesor(a)".equals(tipoUsuario)) {
	        lstChat = (codPrioridad != null)
	                ? chatRepository.findAllByPrioridad_CodPrioridad(codPrioridad)
	                : chatRepository.findAll();
	    } else if ("estudiante".equals(tipoUsuario)) {
	        lstChat = chatRepository.findAllByCuenta_CodUsuario(usuario.getCodUsuario());
	    } else {
	        model.addAttribute("loginInvalido", "Tipo de usuario no reconocido");
	        return "views/login";
	    }

	    model.addAttribute("lstChat", lstChat);

	    // Si se seleccionó un chat específico, mostrar sus mensajes
	    if (codChat != 0) {
	        List<MensajeEntity> mensajes = mensajeRepository.findAllByChat_CodChat(codChat);
	        model.addAttribute("lstMensajes", mensajes);
	        model.addAttribute("codChat", codChat);

	        if (!mensajes.isEmpty()) {
	            MensajeEntity ultimoMensaje = mensajes.get(mensajes.size() - 1);
	            model.addAttribute("mensaje", ultimoMensaje);
	        } else {
	            Optional<ChatEntity> chatOptional = chatRepository.findById(codChat);
	            MensajeEntity nuevoMensaje = new MensajeEntity();
	            nuevoMensaje.setChat(chatOptional.orElse(null));
	            nuevoMensaje.setCuenta(usuario);
	            model.addAttribute("mensaje", nuevoMensaje);
	        }
	    }

	    return "views/chat";
	}


	@PostMapping("/filtrar_chat")
	public String filtrarChat(@ModelAttribute("chatEntity") ChatEntity chatEntity, HttpSession session) {
		int prioridadSeleccionada = chatEntity.getPrioridad().getCodPrioridad();
		session.setAttribute("codPrioridad", prioridadSeleccionada);

		return "redirect:/chat";
	}

	
	@PostMapping("/registrar_mensaje")
    public String registrarMensaje (Model model, HttpSession session, @ModelAttribute("mensaje") MensajeEntity mensaje,
    		@RequestParam("contenido")String contenido){
		
		int codChat = mensaje.getChat().getCodChat();
		mensaje.setFecMensaje(LocalDateTime.now());
		if(mensaje.getCodMensaje() == null) {
			mensaje.setCodMensaje(1);
		} else {		
			mensaje.setCodMensaje(mensaje.getCodMensaje()+1);
		}
		mensaje.setContenido(contenido);
		
        CuentaEntity cuentaEncontrada = (CuentaEntity) session.getAttribute("usuario");
        
        System.out.println(mensaje.toString());
        if(cuentaEncontrada !=null) {       
        }
                  
        mensaje.setCuenta(cuentaEncontrada);
                                               
        mensajeRepository.save(mensaje);
        
		return "redirect:/chat?codChat=" + codChat;
    }
}