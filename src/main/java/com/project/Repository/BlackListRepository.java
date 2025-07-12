package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Model.BlackList;

public interface BlackListRepository extends JpaRepository<BlackList, String>{
    
}
