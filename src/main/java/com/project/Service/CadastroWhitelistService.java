package com.project.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Model.WhiteList;
import com.project.Repository.WhiteListRepository;

@Service
public class CadastroWhitelistService {
    
    @Autowired
    private WhiteListRepository whiteListRepository;

    public WhiteList insert(String numero, String nome, String cargo){
        WhiteList novo = new WhiteList();
        novo.setNumero(numero);
        novo.setNome(nome);
        novo.setCargo(cargo);
        whiteListRepository.save(novo);
        return novo;
    }

    public void delete(String numero){
        try {
            whiteListRepository.deleteById(numero);
        } catch (Exception e) {
            System.out.println("Usuário não encontrado");
        }
    }

}
