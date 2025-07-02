package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PrioridadEntity;
import com.example.demo.repository.PrioridadRepository;

@RestController
@RequestMapping("/api/prioridades")
public class PrioridadController {
	@Autowired
    private PrioridadRepository repo;
    
    @GetMapping
    public List<PrioridadEntity> listAll() {
        return repo.findAll();
    }
}
