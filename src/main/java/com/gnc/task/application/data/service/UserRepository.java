package com.gnc.task.application.data.service;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    List<User> findAllByRolesIn(List<Role> roles);
}
