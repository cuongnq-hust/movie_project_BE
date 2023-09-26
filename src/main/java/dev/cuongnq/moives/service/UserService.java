package dev.cuongnq.moives.service;


import dev.cuongnq.moives.model.Role;
import dev.cuongnq.moives.model.User;

public interface UserService {
    User saveUser(User user);
    Role saveRole(Role role);
    void addToUser(String username, String rolename);
}
