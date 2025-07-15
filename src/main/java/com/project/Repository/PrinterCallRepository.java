package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.Model.PrinterCall;

@Repository
public interface PrinterCallRepository extends JpaRepository<PrinterCall, String>{
    
}
