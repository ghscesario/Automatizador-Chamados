package com.project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "printer_calls")
public class PrinterCall {
    
    @Id
    private String ip;
    
    private String numeroChamado;

    public PrinterCall() {}      

    public PrinterCall(String ip) { this.ip = ip; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getNumeroChamado() { return numeroChamado; }
    public void setNumeroChamado(String numeroChamado) { this.numeroChamado = numeroChamado; }

}
