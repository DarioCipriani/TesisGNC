package com.gnc.task.application.mvp.presenter;

import com.gnc.task.application.mvp.model.entity.Producto;
import com.gnc.task.application.mvp.model.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService extends CrudService<Producto, Integer> {

    private ProductsRepository repository;

    public ProductService(@Autowired ProductsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ProductsRepository getRepository() {
        return repository;
    }

    public List<Producto> getAllProducts() {
        return repository.findAll();
    }

    public boolean deleteProductByProductID(int pId) {
        Optional<Producto> producto = repository.findById(pId);
        if (producto.isPresent()) {
            repository.delete(producto.get());
            return true;
        }
        return false;
    }

    public Producto getProductByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public Producto updateProduct(Producto product) {
        return repository.save(product);
    }


}
