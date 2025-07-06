package com.project.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Service.ChamadoService;
import com.project.Service.PrinterMonitorService;



@RestController
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private PrinterMonitorService printerMonitorService;

    @GetMapping("/chamar")
    public void chamarAutomatizador(){
        chamadoService.criarChamado();
    }

    @GetMapping("/verificarToner")
    public void verificarToner(){
        printerMonitorService.checkAllPrinters();
    }
    
}
