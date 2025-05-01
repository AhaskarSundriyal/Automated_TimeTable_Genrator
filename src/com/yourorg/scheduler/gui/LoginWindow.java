package com.yourorg.scheduler.gui;

import javax.swing.*;
import java.awt.event.*;

public class LoginWindow extends JFrame {
    public LoginWindow() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel label = new JLabel("Login as:");
        label.setBounds(30, 20, 80, 25);
        add(label);

        JButton adminBtn = new JButton("Admin");
        adminBtn.setBounds(30, 60, 100, 30);
        add(adminBtn);

        JButton teacherBtn = new JButton("Teacher");
        teacherBtn.setBounds(150, 60, 100, 30);
        add(teacherBtn);

        adminBtn.addActionListener(e -> {
            new AdminDashboard(); dispose();
        });

        teacherBtn.addActionListener(e -> {
            new TeacherDashboard(); dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginWindow();
    }
}
