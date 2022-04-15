package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.UserConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class UserConfigService extends CrudService<UserConfig, Integer> {

    private UserConfigRepository repository;

    public UserConfigService(@Autowired UserConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    protected UserConfigRepository getRepository() {
        return repository;
    }

}
