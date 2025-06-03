package com.pln.trafo.service;

import com.pln.trafo.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User createUser(User user) {
        // Check if username already exists
        if (User.findByUsername(user.username) != null) {
            throw new WebApplicationException("Username sudah digunakan", Response.Status.CONFLICT);
        }

        // Check if email already exists
        if (User.findByEmail(user.email) != null) {
            throw new WebApplicationException("Email sudah digunakan", Response.Status.CONFLICT);
        }

        // Hash password
        user.password = passwordEncoder.encode(user.password);
        
        user.persist();
        return user;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existingUser = User.findById(id);
        if (existingUser == null) {
            throw new WebApplicationException("User tidak ditemukan", Response.Status.NOT_FOUND);
        }

        // Check if username is being changed and already exists
        if (!existingUser.username.equals(updatedUser.username)) {
            User userWithSameUsername = User.findByUsername(updatedUser.username);
            if (userWithSameUsername != null && !userWithSameUsername.id.equals(id)) {
                throw new WebApplicationException("Username sudah digunakan", Response.Status.CONFLICT);
            }
        }

        // Check if email is being changed and already exists
        if (!existingUser.email.equals(updatedUser.email)) {
            User userWithSameEmail = User.findByEmail(updatedUser.email);
            if (userWithSameEmail != null && !userWithSameEmail.id.equals(id)) {
                throw new WebApplicationException("Email sudah digunakan", Response.Status.CONFLICT);
            }
        }

        // Update fields
        existingUser.username = updatedUser.username;
        existingUser.email = updatedUser.email;
        existingUser.role = updatedUser.role;

        // Update password only if provided
        if (updatedUser.password != null && !updatedUser.password.isEmpty()) {
            existingUser.password = passwordEncoder.encode(updatedUser.password);
        }

        return existingUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new WebApplicationException("User tidak ditemukan", Response.Status.NOT_FOUND);
        }
        user.delete();
    }

    public User getUserById(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new WebApplicationException("User tidak ditemukan", Response.Status.NOT_FOUND);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return User.listAll();
    }

    public List<User> getUsersByRole(User.UserRole role) {
        return User.findByRole(role);
    }

    public List<User> getEngineers() {
        return User.findEngineers();
    }

    public User authenticate(String username, String password) {
        User user = User.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.password)) {
            return user;
        }
        return null;
    }

    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = User.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User tidak ditemukan", Response.Status.NOT_FOUND);
        }

        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw new WebApplicationException("Password lama tidak valid", Response.Status.BAD_REQUEST);
        }

        user.password = passwordEncoder.encode(newPassword);
        return user;
    }
}
