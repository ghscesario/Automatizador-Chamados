package com.project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bot_whitelist")
@Getter
@Setter
public class WhiteList {
    @Id
    private String numero;
    private String cargo;
    private String nome;
}
