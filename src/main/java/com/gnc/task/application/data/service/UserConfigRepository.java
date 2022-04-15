package com.gnc.task.application.data.service;

import com.gnc.task.application.data.entity.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConfigRepository extends JpaRepository<UserConfig, Integer> {
    UserConfig findByUser_Id(Integer id);
    UserConfig findByUser_Username(String username);
}
