package com.project.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Model.WhiteList;
import com.project.Service.CadastroWhitelistService;


@RestController
@RequestMapping("/whitelist")
public class CadastroWhitelistController {

    @Autowired
    private CadastroWhitelistService cadastroWhitelist;
    
    @PostMapping("/cadastrar")
    public ResponseEntity<String> insert(@RequestBody WhiteList whitelist){
        cadastroWhitelist.insert(whitelist.getNumero(), whitelist.getNome(), whitelist.getCargo());
        return ResponseEntity.ok("Usuário cadastrado com sucesso na Whitelist!");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable String numero){
        cadastroWhitelist.delete(numero);
        return ResponseEntity.ok().body("Usuário deletado!");
    }

}
