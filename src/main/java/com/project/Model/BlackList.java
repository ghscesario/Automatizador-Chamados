package com.project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bot_blacklist")
@Getter
@Setter
public class BlackList {
    @Id
    private String numero;
}
