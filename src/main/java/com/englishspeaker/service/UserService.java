package com.englishspeaker.service;

import com.englishspeaker.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class UserService {
    private static final String FILE_PATH = "users.txt";
    private final Gson gson = new Gson();
    private String currentUser = null;
    private List<User> users;

    public UserService() {
        users = loadUsers();
    }

    public boolean register(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (findUser(username) != null) {
            return false;
        }
        users.add(new User(username, password));
        saveUsers();
        return true;
    }

    public boolean login(String username, String password) {
        User user = findUser(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = username;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    private User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private List<User> loadUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<User> list = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 2);
                if (parts.length == 2) {
                    list.add(new User(parts[0], parts[1]));
                }
            }
            return list;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                writer.write(user.getUsername() + "\t" + user.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}
