package com.gnc.task.application.mvp.model.repository;

import com.gnc.task.application.mvp.model.entity.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConfigRepository extends JpaRepository<UserConfig, Integer> {
    UserConfig findByUser_Id(Integer id);

    UserConfig findByUser_Username(String username);
}
