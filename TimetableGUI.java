import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

public class TimetableGUI extends JFrame {
    private TimetableGenerator generator;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private JButton loginButton;
    private JButton generateButton;
    private JButton exportButton;
    private JButton addDataButton;
    private JButton clearDataButton;
    private JLabel statusLabel;
    private static final String[] VALID_DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final Logger LOGGER = Logger.getLogger(TimetableGUI.class.getName());

    public TimetableGUI() {
        try {
            generator = new TimetableGenerator();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Configuration file error", e);
            JOptionPane.showMessageDialog(this, "Configuration error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Automated Timetable Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Admin Login"));
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);
        loginButton = new JButton("Login");
        loginPanel.add(loginButton);
        statusLabel = new JLabel("Please login to proceed.");
        loginPanel.add(statusLabel);
        add(loginPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        timetableTable = new JTable(tableModel);
        timetableTable.setRowHeight(30);
        timetableTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        timetableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        addDataButton = new JButton("Add Data");
        addDataButton.setEnabled(false);
        buttonPanel.add(addDataButton);
        generateButton = new JButton("Generate Timetable");
        generateButton.setEnabled(false);
        buttonPanel.add(generateButton);
        exportButton = new JButton("Export to PDF");
        exportButton.setEnabled(false);
        buttonPanel.add(exportButton);
        clearDataButton = new JButton("Clear Data");
        clearDataButton.setEnabled(false);
        buttonPanel.add(clearDataButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> handleLogin());
        addDataButton.addActionListener(e -> showDataInputDialog());
        generateButton.addActionListener(e -> handleGenerateTimetable());
        exportButton.addActionListener(e -> handleExportPDF());
        clearDataButton.addActionListener(e -> handleClearData());

        pack();
        setLocationRelativeTo(null);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username or password cannot be empty.");
            statusLabel.setForeground(Color.RED);
            return;
        }
        try {
            if (generator.loginAdmin(username, password)) {
                statusLabel.setText("Login successful!");
                statusLabel.setForeground(Color.GREEN);
                loginButton.setEnabled(false);
                usernameField.setEnabled(false);
                passwordField.setEnabled(false);
                generateButton.setEnabled(true);
                addDataButton.setEnabled(true);
                clearDataButton.setEnabled(true);
            } else {
                statusLabel.setText("Login failed. Invalid credentials.");
                statusLabel.setForeground(Color.RED);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during login", ex);
            statusLabel.setText("Database error during login: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void handleGenerateTimetable() {
        generateButton.setEnabled(false);
        statusLabel.setText("Generating timetable...");
        statusLabel.setForeground(Color.BLACK);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                generator.refreshData();
                return generator.generateTimetable();
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        updateTimetableTable();
                        statusLabel.setText("Timetable generated and saved!");
                        statusLabel.setForeground(Color.GREEN);
                        exportButton.setEnabled(true);
                    } else {
                        statusLabel.setText("Failed to generate timetable. Check constraints or add more data.");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Timetable generation error", ex);
                    statusLabel.setText("Error generating timetable: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                } finally {
                    generateButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void handleExportPDF() {
        try {
            generator.exportTimetableToPDF();
            statusLabel.setText("PDF LaTeX code saved to timetable.tex.");
            statusLabel.setForeground(Color.GREEN);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "PDF export error", ex);
            statusLabel.setText("Failed to export PDF: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void handleClearData() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all data? This action cannot be undone.",
                "Confirm Clear Data",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                generator.getDbManager().clearData();
                generator.refreshData();
                statusLabel.setText("All data cleared successfully!");
                statusLabel.setForeground(Color.GREEN);
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);
                exportButton.setEnabled(false);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Clear data error", ex);
                statusLabel.setText("Failed to clear data: " + ex.getMessage());
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void updateTimetableTable() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        List<TimeSlot> timeSlots = generator.getTimeSlots();
        List<ScheduledClass> schedule = generator.getSchedule();
        if (timeSlots == null || schedule == null) {
            statusLabel.setText("No timetable data available.");
            statusLabel.setForeground(Color.RED);
            return;
        }

        Set<String> days = new TreeSet<>();
        Set<Integer> startHours = new TreeSet<>();
        for (TimeSlot ts : timeSlots) {
            days.add(ts.day);
            for (int hour = ts.startHour; hour < ts.startHour + ts.duration; hour++) {
                startHours.add(hour);
            }
        }

        tableModel.addColumn("Day");
        for (Integer hour : startHours) {
            tableModel.addColumn(String.format("%d:00", hour));
        }

        Object[][] data = new Object[days.size()][startHours.size() + 1];
        int row = 0;
        for (String day : days) {
            data[row][0] = day;
            row++;
        }

        for (ScheduledClass sc : schedule) {
            int rowIndex = new ArrayList<>(days).indexOf(sc.subTimeSlot.day);
            int colIndex = new ArrayList<>(startHours).indexOf(sc.subTimeSlot.startHour) + 1;
            String cellContent = String.format("%s\n%s\n%s",
                    sc.subject.name, sc.teacher.name, sc.room.name);
            data[rowIndex][colIndex] = cellContent;
        }

        for (Object[] rowData : data) {
            tableModel.addRow(rowData);
        }

        for (int i = 0; i < timetableTable.getColumnCount(); i++) {
            timetableTable.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 100 : 150);
        }
    }

    private void refreshSubjects(DefaultListModel<String> subjectListModel, List<Integer> subjectIds, JDialog dataDialog) {
        SwingWorker<List<Subject>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Subject> doInBackground() throws SQLException {
                return generator.getDbManager().getSubjects();
            }

            @Override
            protected void done() {
                try {
                    List<Subject> subjects = get();
                    subjectListModel.clear();
                    subjectIds.clear();
                    for (Subject subject : subjects) {
                        subjectListModel.addElement(subject.name);
                        subjectIds.add(subject.id);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error fetching subjects", ex);
                    JOptionPane.showMessageDialog(dataDialog, "Error fetching subjects: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showDataInputDialog() {
        JDialog dataDialog = new JDialog(this, "Add Timetable Data", true);
        dataDialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Subject Panel
        JPanel subjectPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        subjectPanel.add(new JLabel("Subject Name:"));
        JTextField subjectNameField = new JTextField();
        subjectPanel.add(subjectNameField);
        subjectPanel.add(new JLabel("Is Lab:"));
        JCheckBox isLabCheck = new JCheckBox();
        subjectPanel.add(isLabCheck);
        subjectPanel.add(new JLabel("Duration (hours):"));
        JTextField durationField = new JTextField();
        subjectPanel.add(durationField);
        subjectPanel.add(new JLabel("Room Type:"));
        JTextField roomTypeField = new JTextField();
        subjectPanel.add(roomTypeField);
        subjectPanel.add(new JLabel("Lectures per Week:"));
        JTextField lecturesPerWeekField = new JTextField();
        subjectPanel.add(lecturesPerWeekField);
        JButton saveSubjectButton = new JButton("Save Subject");
        subjectPanel.add(saveSubjectButton);
        tabbedPane.addTab("Subject", subjectPanel);

        // Teacher Panel
        JPanel teacherPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        teacherPanel.add(new JLabel("Teacher Name:"));
        JTextField teacherNameField = new JTextField();
        teacherPanel.add(teacherNameField);
        teacherPanel.add(new JLabel("Unavailable Days (comma-separated):"));
        JTextField unavailableDaysField = new JTextField();
        teacherPanel.add(unavailableDaysField);
        teacherPanel.add(new JLabel("Subjects:"));
        DefaultListModel<String> subjectListModel = new DefaultListModel<>();
        List<Integer> subjectIds = new ArrayList<>();
        JList<String> subjectList = new JList<>(subjectListModel);
        subjectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane subjectScrollPane = new JScrollPane(subjectList);
        teacherPanel.add(subjectScrollPane);
        JButton saveTeacherButton = new JButton("Save Teacher");
        teacherPanel.add(saveTeacherButton);
        tabbedPane.addTab("Teacher", teacherPanel);

        // Room Panel
        JPanel roomPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        roomPanel.add(new JLabel("Room Name:"));
        JTextField roomNameField = new JTextField();
        roomPanel.add(roomNameField);
        roomPanel.add(new JLabel("Room Type:"));
        JTextField roomTypeField2 = new JTextField();
        roomPanel.add(roomTypeField2);
        JButton saveRoomButton = new JButton("Save Room");
        roomPanel.add(saveRoomButton);
        tabbedPane.addTab("Room", roomPanel);

        // Time Slot Panel
        JPanel timeSlotPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        timeSlotPanel.add(new JLabel("Day:"));
        JComboBox<String> dayField = new JComboBox<>(VALID_DAYS);
        timeSlotPanel.add(dayField);
        timeSlotPanel.add(new JLabel("Start Hour (0-23):"));
        JTextField startHourField = new JTextField();
        timeSlotPanel.add(startHourField);
        timeSlotPanel.add(new JLabel("Total Available Hours:"));
        JTextField durationField2 = new JTextField();
        timeSlotPanel.add(durationField2);
        JButton saveTimeSlotButton = new JButton("Save Time Slot");
        timeSlotPanel.add(saveTimeSlotButton);
        tabbedPane.addTab("Time Slot", timeSlotPanel);

        // Load subjects initially
        refreshSubjects(subjectListModel, subjectIds, dataDialog);

        // Subject Save Action
        saveSubjectButton.addActionListener(e -> {
            try {
                String name = subjectNameField.getText().trim();
                boolean isLab = isLabCheck.isSelected();
                String durationText = durationField.getText().trim();
                String roomType = roomTypeField.getText().trim();
                String lecturesPerWeekText = lecturesPerWeekField.getText().trim();

                if (name.isEmpty() || durationText.isEmpty() || roomType.isEmpty() || lecturesPerWeekText.isEmpty()) {
                    JOptionPane.showMessageDialog(dataDialog, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int duration;
                int lecturesPerWeek;
                try {
                    duration = Integer.parseInt(durationText);
                    lecturesPerWeek = Integer.parseInt(lecturesPerWeekText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dataDialog, "Duration and lectures per week must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (duration <= 0 || lecturesPerWeek <= 0) {
                    JOptionPane.showMessageDialog(dataDialog, "Duration and lectures per week must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                generator.getDbManager().addSubject(name, isLab, duration, roomType, lecturesPerWeek);
                JOptionPane.showMessageDialog(dataDialog, "Subject added successfully!");
                subjectNameField.setText("");
                isLabCheck.setSelected(false);
                durationField.setText("");
                roomTypeField.setText("");
                lecturesPerWeekField.setText("");
                refreshSubjects(subjectListModel, subjectIds, dataDialog);
                generator.refreshData();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error adding subject", ex);
                String message = ex.getMessage().contains("Duplicate entry") ?
                        "Subject with this name already exists." :
                        "Error adding subject: " + ex.getMessage();
                JOptionPane.showMessageDialog(dataDialog, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Teacher Save Action
        saveTeacherButton.addActionListener(e -> {
            try {
                String name = teacherNameField.getText().trim();
                String unavailableDays = unavailableDaysField.getText().trim();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dataDialog, "Teacher name must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (subjectList.getSelectedIndices().length == 0) {
                    JOptionPane.showMessageDialog(dataDialog, "At least one subject must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!unavailableDays.isEmpty()) {
                    String[] days = unavailableDays.split(",");
                    for (String day : days) {
                        String trimmedDay = day.trim();
                        if (!trimmedDay.isEmpty() && !Arrays.asList(VALID_DAYS).contains(trimmedDay)) {
                            JOptionPane.showMessageDialog(dataDialog, "Invalid day: " + trimmedDay, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                int teacherId = generator.getDbManager().addTeacher(name, unavailableDays);
                int[] selectedIndices = subjectList.getSelectedIndices();
                List<String> selectedSubjects = new ArrayList<>();
                for (int index : selectedIndices) {
                    if (index < subjectIds.size()) {
                        generator.getDbManager().addTeacherSubject(teacherId, subjectIds.get(index));
                        selectedSubjects.add(subjectListModel.getElementAt(index));
                    }
                }
                JOptionPane.showMessageDialog(dataDialog,
                        "Teacher '" + name + "' added successfully with subjects: " + String.join(", ", selectedSubjects),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                teacherNameField.setText("");
                unavailableDaysField.setText("");
                subjectList.clearSelection();
                generator.refreshData();
                refreshSubjects(subjectListModel, subjectIds, dataDialog);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error adding teacher", ex);
                String message = ex.getMessage().contains("Duplicate entry") ?
                        "Teacher with this name already exists." :
                        "Error adding teacher: " + ex.getMessage();
                JOptionPane.showMessageDialog(dataDialog, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Room Save Action
        saveRoomButton.addActionListener(e -> {
            try {
                String name = roomNameField.getText().trim();
                String roomType = roomTypeField2.getText().trim();

                if (name.isEmpty() || roomType.isEmpty()) {
                    JOptionPane.showMessageDialog(dataDialog, "Room name and type must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                generator.getDbManager().addRoom(name, roomType);
                JOptionPane.showMessageDialog(dataDialog, "Room '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                roomNameField.setText("");
                roomTypeField2.setText("");
                generator.refreshData();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error adding room", ex);
                String message = ex.getMessage().contains("Duplicate entry") ?
                        "Room with this name already exists." :
                        "Error adding room: " + ex.getMessage();
                JOptionPane.showMessageDialog(dataDialog, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Time Slot Save Action
        saveTimeSlotButton.addActionListener(e -> {
            try {
                String day = (String) dayField.getSelectedItem();
                String startHourText = startHourField.getText().trim();
                String durationText = durationField2.getText().trim();

                if (startHourText.isEmpty() || durationText.isEmpty()) {
                    JOptionPane.showMessageDialog(dataDialog, "Start hour and duration must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int startHour;
                int duration;
                try {
                    startHour = Integer.parseInt(startHourText);
                    duration = Integer.parseInt(durationText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dataDialog, "Start hour and duration must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (startHour < 0 || startHour > 23) {
                    JOptionPane.showMessageDialog(dataDialog, "Start hour must be between 0 and 23.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (duration <= 0) {
                    JOptionPane.showMessageDialog(dataDialog, "Duration must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                generator.getDbManager().addTimeSlot(day, startHour, duration);
                JOptionPane.showMessageDialog(dataDialog, "Time slot added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                startHourField.setText("");
                durationField2.setText("");
                dayField.setSelectedIndex(0);
                generator.refreshData();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error adding time slot", ex);
                String message = ex.getMessage().contains("Duplicate entry") ?
                        "Time slot for this day and start hour already exists." :
                        "Error adding time slot: " + ex.getMessage();
                JOptionPane.showMessageDialog(dataDialog, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dataDialog.add(tabbedPane, BorderLayout.CENTER);
        dataDialog.pack();
        dataDialog.setLocationRelativeTo(this);
        dataDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TimetableGUI().setVisible(true));
    }
}