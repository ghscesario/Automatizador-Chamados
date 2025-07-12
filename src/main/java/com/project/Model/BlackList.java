package com.project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_blacklist")
public class BlackList {
    @Id
    private String numero;        // PK, só dígitos. Ex.: 556298001112
    private String motivo;        // opcional
}
