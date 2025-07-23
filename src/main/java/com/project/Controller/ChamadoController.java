package com.project.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.Service.BuscadorResolvidosService;
import com.project.Service.ChamadoService;
import com.project.Service.FecharChamadoService;
import com.project.Service.NotificacaoChamadosResolvidosService;
import com.project.Service.PrinterMonitorService;



@RestController
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private PrinterMonitorService printerMonitorService;

    @Autowired
    private FecharChamadoService fecharChamadoService;

    @Autowired
    private BuscadorResolvidosService buscadorResolvidosService;

    @Autowired
    private NotificacaoChamadosResolvidosService notificacaoService;

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

    @GetMapping("/fechar/{id}")
    public void testarFechar(@PathVariable String id, @RequestBody String resolucao){
        fecharChamadoService.fecharChamado(id, resolucao);
    }

    @GetMapping("/testarCaptura")
    public void testarCaptura(){
        List<String> lista = buscadorResolvidosService.buscarNumerosDeChamadosResolvidos();
        ResponseEntity.ok().body(lista);
    }

    @GetMapping("/chamados-resolvidos")
    public String notificarChamadosResolvidos() {
        try {
            notificacaoService.notificarChamadosResolvidos();
            return "Notificações de chamados resolvidos enviadas com sucesso.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao enviar notificações: " + e.getMessage();
        }
    }

    // @GetMapping("/salvarsessao")
    // public void salvarsessao(){
    //     fecharChamadoService.salvarSessao();
    // }
    
}
