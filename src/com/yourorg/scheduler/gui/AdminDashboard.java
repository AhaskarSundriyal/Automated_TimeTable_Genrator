package com.yourorg.scheduler.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.yourorg.scheduler.db.DBConnection;

public class AdminDashboard extends JFrame {
    private JTextField teacherNameField, subjectNameField;
    private JTextArea logArea;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel title = new JLabel("Admin Panel - Add Entities");
        title.setBounds(150, 10, 300, 30);
        add(title);

        // ---- TEACHER INPUT ----
        JLabel tLabel = new JLabel("Teacher Name:");
        tLabel.setBounds(30, 60, 100, 25);
        add(tLabel);

        teacherNameField = new JTextField();
        teacherNameField.setBounds(140, 60, 150, 25);
        add(teacherNameField);

        JButton addTeacherBtn = new JButton("Add Teacher");
        addTeacherBtn.setBounds(310, 60, 130, 25);
        add(addTeacherBtn);

        // ---- SUBJECT INPUT ----
        JLabel sLabel = new JLabel("Subject Name:");
        sLabel.setBounds(30, 100, 100, 25);
        add(sLabel);

        subjectNameField = new JTextField();
        subjectNameField.setBounds(140, 100, 150, 25);
        add(subjectNameField);

        JButton addSubjectBtn = new JButton("Add Subject");
        addSubjectBtn.setBounds(310, 100, 130, 25);
        add(addSubjectBtn);

        // ---- LOG OUTPUT ----
        logArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBounds(30, 150, 410, 180);
        add(scroll);

        // ---- Actions ----
        addTeacherBtn.addActionListener(e -> addTeacher());

        addSubjectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSubject();
            }
        });
    }

    private void addTeacher() {
        String name = teacherNameField.getText().trim();
        if (name.isEmpty()) {
            log("Teacher name cannot be empty.");
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO instructors (name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
            log("Teacher added: " + name);
        } catch (Exception ex) {
            log("Error adding teacher: " + ex.getMessage());
        }
    }

    private void addSubject() {
        String subject = subjectNameField.getText().trim();
        if (subject.isEmpty()) {
            log("Subject name cannot be empty.");
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO courses (name) VALUES (?)")) {
            ps.setString(1, subject);
            ps.executeUpdate();
            log("Subject added: " + subject);  // Corrected semicolon
        } catch (Exception ex) {
            log("Error adding subject: " + ex.getMessage());
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminDashboard().setVisible(true);
        });
    }
}
