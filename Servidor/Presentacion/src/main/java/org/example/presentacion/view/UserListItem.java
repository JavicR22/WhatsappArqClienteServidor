package org.example.presentacion.view;


import javax.swing.*;
import java.awt.*;

// Item para la lista
public class UserListItem {
    private final String username;
    private final ImageIcon avatar;

    public UserListItem(String username, ImageIcon avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() { return username; }
    public ImageIcon getAvatar() { return avatar; }
}



