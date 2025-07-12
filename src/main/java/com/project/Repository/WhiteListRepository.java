package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.Model.WhiteList;

public interface WhiteListRepository extends JpaRepository<WhiteList, String>{
    
}
