package com.project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "printers")
public class Printer {
    
    @Id
    private String ip;

    private String tipo;   // 'SP3710', 'C3003', 'C3004'…

    private String name;

    public Printer() {}
    public Printer(String ip, String tipo) {
        this.ip = ip;
        this.tipo = tipo;
    }
    public String getIp()   { return ip; }
    public String getTipo() { return tipo; }
    public String getName() {return name;}
    public void setIp(String ip)     { this.ip = ip; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setName(String name) {this.name = name;}

}
