package com.project.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chamados")
@Getter
@Setter
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String telefone;
    private String horario;
    private String bloco;
    private String andar;
    private String area;
    private String categoria;
    private String subcategoria;
    private String urgencia;
    private String sintoma;
    private String numeroChamado;
    
    @Column(length = 400)
    private String resumo;

    @Column(length = 2000)
    private String descricao;

    @SuppressWarnings("FieldMayBeFinal")
    private LocalDateTime criadoEm = LocalDateTime.now();
}
