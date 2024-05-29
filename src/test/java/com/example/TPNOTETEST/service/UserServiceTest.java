package com.example.TPNOTETEST.service;

import com.example.TPNOTETEST.exception.DataIntegrityViolationException;
import com.example.TPNOTETEST.exception.ObjectNotFoundException;
import com.example.TPNOTETEST.model.User;
import com.example.TPNOTETEST.repository.UserRepository;
import com.example.TPNOTETEST.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Lancer une exception si l'utilisateur n'est pas trouvé par ID")
    void testGetUserByIdNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }

    @Test
    @DisplayName("Récupérer tous les utilisateurs")
    void testGetAllUsers() {
        User user = new User("Axel", "Axel@gmail.com", "123");
        List<User> users = Collections.singletonList(user);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result, "Il doit y avoir une liste d'utilisateurs");
        assertEquals(1, result.size(), "La liste doit contenir un utilisateur");
        assertEquals(user.getEmail(), result.get(0).getEmail(), "L'email de l'utilisateur doit être le même");
    }

    @Test
    @DisplayName("Récupérer un utilisateur par ID")
    void testGetUserById() {
        Long userId = 1L;
        User user = new User("Axel", "axel@gmail.com", "123");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);
        System.out.println("existingUser: " + result);


        assertNotNull(result, "L'utilisateur ne doit pas être nul");
        assertTrue(result instanceof User, "L'utilisateur doit être une instance de User");
        assertEquals(userId, result.getId(), "L'id de l'utilisateur doit correspondre à l'id attendu");
        assertEquals(user.getName(), result.getName(), "Le nom de l'utilisateur doit correspondre au nom attendu");
        assertEquals(user.getEmail(), result.getEmail(), "L'email de l'utilisateur doit correspondre à l'email attendu");
    }

    @Test
    @DisplayName("Enregistrer un utilisateur")
    void testSaveUser() {
        String userName = "Axel";
        String userEmail = "axel@gmail.com";
        String userPassword = "123";

        User user = new User(userName, userEmail, userPassword);
        User savedUser = new User(userName, userEmail, userPassword);
        savedUser.setId(1L);

        when(userRepository.save(user)).thenReturn(savedUser);

        User result = userService.saveUser(user);

        assertNotNull(result, "L'utilisateur ne doit pas être nul");
        assertTrue(result instanceof User, "L'utilisateur doit être une instance de User");
        assertEquals(userName, result.getName(), "Le nom de l'utilisateur doit correspondre au nom enregistré");
        assertEquals(userEmail, result.getEmail(), "L'email de l'utilisateur doit correspondre à l'email enregistré");
        assertEquals(userPassword, result.getPassword(), "Le mot de passe de l'utilisateur doit correspondre au mot de passe enregistré");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Lancer une exception si l'email de l'utilisateur existe déjà")
    void testSaveUserEmailAlreadyExists() {
        User user = new User("Axel", "Axel@gmail.com", "123");
        when(userRepository.save(user)).thenThrow(new org.springframework.dao.DataIntegrityViolationException(""));

        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.saveUser(user);
        });
        verify(userRepository, times(1)).save(user);
    }


    @Test
    @DisplayName("Mettre à jour un utilisateur")
    void testUpdateUser() {
        Long userId = 1L;
        String updatedName = "AxelUpdated";
        String updatedEmail = "axelUpdated@gmail.com";
        String updatedPassword = "234";

        User existingUser = new User("Axel", "axel@gmail.com", "123");
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        userService.saveUser(existingUser);

        existingUser.setName(updatedName);
        existingUser.setEmail(updatedEmail);
        existingUser.setPassword(updatedPassword);

        System.out.println("existingUser: " + existingUser);

        System.out.println("userId: " + userId);
        userService.updateUser(userId, existingUser);

        System.out.println("result: " + existingUser);

        assertNotNull(existingUser, "L'utilisateur ne doit pas être nul");
        assertEquals(updatedName, existingUser.getName(), "Le nom doit être mis à jour");
        assertEquals(updatedEmail, existingUser.getEmail(), "L'email doit être mis à jour");
        assertEquals(updatedPassword, existingUser.getPassword(), "Le mot de passe doit être mis à jour");

        verify(userRepository, times(1)).findById(userId);
    }


    @Test
    @DisplayName("Lancer une exception si l'email mis à jour existe déjà")
    void testUpdateUserWithExistingEmail() {
        Long userIdA = 1L;
        Long userIdB = 2L;

        User existingUserA = new User("Axel", "axel@gmail.com", "123");
        existingUserA.setId(userIdA);

        User existingUserB = new User("John", "john@gmail.com", "456");
        existingUserB.setId(userIdB);

        when(userRepository.findById(userIdA)).thenReturn(Optional.of(existingUserA));
        when(userRepository.findById(userIdB)).thenReturn(Optional.of(existingUserB));
        when(userRepository.save(any(User.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Email déjà existant"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            existingUserA.setEmail("john@gmail.com");
            userService.updateUser(userIdA, existingUserA);
        }, "Email déjà existant");

        verify(userRepository, times(1)).findById(userIdA);
        verify(userRepository, times(1)).save(existingUserA);
    }

    @Test
    @DisplayName("Supprimer un utilisateur avec succès")
    void testDeleteUserSuccess() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUserById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Lancer une exception si la suppression échoue car l'utilisateur n'est pas trouvé")
    void testDeleteUserFailure() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        ObjectNotFoundException thrown = assertThrows(ObjectNotFoundException.class, () -> {
            userService.deleteUserById(userId);
        });

        assertEquals("Utilisateur non trouvé avec Id: " + userId, thrown.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

}
