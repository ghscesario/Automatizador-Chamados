package com.project.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Service.ChamadoService;



@RestController
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @GetMapping("/chamar")
    public void chamarAutomatizador(){
        chamadoService.criarChamado();
    }
    
}
