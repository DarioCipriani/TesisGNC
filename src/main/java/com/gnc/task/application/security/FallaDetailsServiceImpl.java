package com.gnc.task.application.security;

import com.gnc.task.application.data.entity.Fallas;
import com.gnc.task.application.data.service.FallaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FallaDetailsServiceImpl {

    @Autowired
    private FallaRepository fallaRepository;

    public boolean deleteByDescription(String description) {
        Fallas falla = fallaRepository.findByDescription(description);
        fallaRepository.delete(falla);
        return true;
    }

    public void updateFallas(Fallas fallas) {
        fallaRepository.save(fallas);
    }

    public Optional<Fallas> getFallaById(Integer id) {
        return fallaRepository.findById(id);
    }

    public List<Fallas> findAllFalla() {
        return fallaRepository.findAll();
    }

}
