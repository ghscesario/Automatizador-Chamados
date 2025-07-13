package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.Model.WhiteList;

@Repository
public interface WhiteListRepository extends JpaRepository<WhiteList, String>{
    
}
