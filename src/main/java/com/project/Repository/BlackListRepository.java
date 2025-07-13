package com.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.Model.BlackList;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList, String>{
    
}
