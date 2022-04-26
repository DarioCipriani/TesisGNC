package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.Presupuesto;
import com.gnc.task.application.data.repository.BudgetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetService extends CrudService<Presupuesto, Integer> {

    private BudgetsRepository repository;

    public BudgetService(@Autowired BudgetsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected BudgetsRepository getRepository() {
        return repository;
    }

    public List<Presupuesto> getAllBudgets() {
        return repository.findAll();
    }

    public boolean deleteBudgetByBudgetID(int bId) {
        Optional<Presupuesto> presupuesto = repository.findById(bId);
        if (presupuesto.isPresent()) {
            repository.delete(presupuesto.get());
            return true;
        }
        return false;
    }

    public Presupuesto getBudgetByNroPresupuesto(Integer nroPresupuesto) {
        return repository.findBynroPresupuesto(nroPresupuesto);
    }

    public Presupuesto updateProduct(Presupuesto presupuesto) {
        return repository.save(presupuesto);
    }

}
