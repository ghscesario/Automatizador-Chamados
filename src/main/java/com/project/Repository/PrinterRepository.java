package com.project.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.Model.Printer;

@Repository
public interface PrinterRepository extends JpaRepository<Printer, String>{
    
    List<Printer> findByTipo(String tipo); 

    List<Printer> findAllByTipo(String tipo);

}
