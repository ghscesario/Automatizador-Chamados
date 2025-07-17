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

    @GetMapping("/chamarTeste")
    public void chamarAutomatizador(){
        chamadoService.criarChamadoTeste();
    }

    @GetMapping("/verificarToner")
    public void verificarToner(){
        printerMonitorService.checkAllPrinters();
    }

    // @GetMapping("/checarKyocera")
    // public void verificarKyocera() throws Exception{
    //     printerMonitorService.checkKyocera("10.239.20.21");
    // }
    
}
