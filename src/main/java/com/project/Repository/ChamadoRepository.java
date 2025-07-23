package com.project.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Model.Chamado;

public interface ChamadoRepository extends JpaRepository<Chamado, Long>{
    List<Chamado> findByNumeroChamadoIn(List<String> numerosChamados);
    List<Chamado> findByNumeroChamadoInAndMensagemEnviadaFalse(List<String> numerosChamado);
}
