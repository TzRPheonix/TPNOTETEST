package com.example.TPNOTETEST.service.impl;

import com.example.TPNOTETEST.exception.DataIntegrityViolationException;
import com.example.TPNOTETEST.exception.ObjectNotFoundException;
import com.example.TPNOTETEST.model.User;
import com.example.TPNOTETEST.repository.UserRepository;
import com.example.TPNOTETEST.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Utilisateur non trouvé avec Id: " + id));
    }

    @Override
    public User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Email déjà existant: " + user.getEmail());
        }
    }

    @Override
    public User updateUser(Long id, User user) {
        System.out.println("id: " + id + " user: " + user);
        if (userRepository.findById(id).isEmpty()) {
            throw new ObjectNotFoundException("Utilisateur non trouvé avec Id: " + id);
        }
        user.setId(id);
        try {
            return userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Email déjà existant: " + user.getEmail());
        }
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException("Utilisateur non trouvé avec Id: " + id);
        }
        userRepository.deleteById(id);
    }
}
