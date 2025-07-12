package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Model.Chamado;

public interface ChamadoRepository extends JpaRepository<Chamado, Long>{
    
}
