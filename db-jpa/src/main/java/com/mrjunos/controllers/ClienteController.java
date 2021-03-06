package com.mrjunos.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mrjunos.models.entity.Cliente;
import com.mrjunos.models.service.IClienteService;
import com.mrjunos.models.service.IUploadFileService;
import com.mrjunos.util.paginator.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	/* DECLARACION DE VARIABLES */

	@Autowired
	private IClienteService clienteService;

	@Autowired
	private IUploadFileService uploadFileService;

	/* VALIDACIONES VISTA */

	@RequestMapping(value = "/cliente")
	public String nuevo(Model model) {
		Cliente c = new Cliente();
		model.addAttribute("cliente", c);
		model.addAttribute("titulo", "Clientes");
		return "cliente";
	}

	@GetMapping(value = "/uploads/{file:.+}")
	public ResponseEntity<Resource> verImagen(@PathVariable String file) {

		Resource recurso = null;
		try {
			recurso = uploadFileService.load(file);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; file=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	/* ACCESO A DATOS */

	@RequestMapping(value = "/cliente", method = RequestMethod.POST)
	public String guardar(@Valid Cliente c, BindingResult result, Model model,
			@RequestParam("file") MultipartFile imagen, RedirectAttributes flash, SessionStatus status) {

		String messageFlash = "";
		String severity = "info";

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Clientes");
			return "cliente";
		}

		if (!imagen.isEmpty()) {

			if (c.getId() != null && c.getId() > 0 && c.getImagen() != null && c.getImagen().length() > 0) {
				uploadFileService.delete(c.getImagen());
			}

			String fileName = null;
			try {
				fileName = uploadFileService.copy(imagen, c.getId().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			messageFlash = "Has subido correctamente: " + fileName;
			c.setImagen(fileName);
		}

		messageFlash = c.getId() != null ? messageFlash + "<br> Cliente editado con éxito"
				: messageFlash + "<br> Cliente creado con éxito";
		clienteService.save(c);
		status.isComplete();
		flash.addFlashAttribute("message", messageFlash);
		flash.addFlashAttribute("severity", severity);
		return "redirect:clientes";
	}

	@RequestMapping(value = "/cliente/{id}")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		Cliente c = null;
		if (id > 0) {
			c = clienteService.find(id);
			if (c == null) {
				flash.addFlashAttribute("message", "El ID del cliente no Existe en la base de datos");
				flash.addFlashAttribute("severity", "danger");
				return "redirect:/clientes";
			}
		} else {
			flash.addFlashAttribute("message", "El ID del cliente no puede ser 0");
			flash.addFlashAttribute("severity", "danger");
			return "redirect:/clientes";
		}
		model.addAttribute("cliente", c);
		model.addAttribute("titulo", "Editar Cliente");
		return "cliente";
	}

	@RequestMapping(value = "/cliente/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		String messageFlash = "";
		String severity = "info";

		if (id > 0) {
			Cliente c = clienteService.find(id);
			clienteService.remove(id);
			flash.addFlashAttribute("message", "Eliminado con éxito");
			flash.addFlashAttribute("severity", "success");

			messageFlash = uploadFileService.delete(c.getImagen()) ? "Imagen eliminada con éxito!"
					: "No se pudo eliminar la imagen";
		}
		flash.addFlashAttribute("message", messageFlash);
		flash.addFlashAttribute("severity", severity);
		return "redirect:/clientes";
	}

	@RequestMapping(value = "/clientes", method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {

		Pageable pageRequest = PageRequest.of(page, 5);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);

		PageRender<Cliente> pageRender = new PageRender<>("/clientes", clientes);
		model.addAttribute("titulo", "Listado de Clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "clientes";
	}

	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {

		Cliente c = clienteService.find(id);
		if (c == null) {
			flash.addFlashAttribute("message", "El cliente no existe en la base de datos");
			flash.addFlashAttribute("severity", "danger");
			return "redirect:/listar";
		}

		model.addAttribute("cliente", c);
		model.addAttribute("titulo", c.getNombre() + " " + c.getApellido());

		return "ver";
	}
}
