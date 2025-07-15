package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Model.PrinterCall;

public interface PrinterCallRepository extends JpaRepository<PrinterCall, String>{
    
}
