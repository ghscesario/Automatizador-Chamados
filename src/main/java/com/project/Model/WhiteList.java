package com.project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_whitelist")
public class WhiteList {
    @Id
    private String numero;
    private String observacao;
}
