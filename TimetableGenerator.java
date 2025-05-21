import java.sql.*;
import java.util.*;
import java.io.*;

class Subject {
    int id;
    String name;
    boolean isLab;
    int duration;
    String requiredRoomType;
    int lecturesPerWeek;

    Subject(int id, String name, boolean isLab, int duration, String requiredRoomType, int lecturesPerWeek) {
        this.id = id;
        this.name = name;
        this.isLab = isLab;
        this.duration = duration;
        this.requiredRoomType = requiredRoomType;
        this.lecturesPerWeek = lecturesPerWeek;
    }
}

class Teacher {
    int id;
    String name;
    List<String> unavailableDays;
    List<Integer> subjectIds;

    Teacher(int id, String name, List<String> unavailableDays, List<Integer> subjectIds) {
        this.id = id;
        this.name = name;
        this.unavailableDays = unavailableDays;
        this.subjectIds = subjectIds != null ? subjectIds : new ArrayList<>();
    }
}

class Room {
    int id;
    String name;
    String type;

    Room(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}

class TimeSlot {
    int id;
    String day;
    int startHour;
    int duration;

    TimeSlot(int id, String day, int startHour, int duration) {
        this.id = id;
        this.day = day;
        this.startHour = startHour;
        this.duration = duration;
    }

    // Generate sub-slots of 1 or 2 hours within this time slot's duration
    List<SubTimeSlot> generateSubSlots(int requiredDuration) {
        List<SubTimeSlot> subSlots = new ArrayList<>();
        if (requiredDuration < 1 || requiredDuration > 2) return subSlots; // Only support 1 or 2-hour classes
        for (int hour = startHour; hour + requiredDuration <= startHour + duration; hour++) {
            subSlots.add(new SubTimeSlot(id, day, hour, requiredDuration));
        }
        return subSlots;
    }

    public boolean overlaps(TimeSlot other) {
        int thisStart = this.startHour;
        int thisEnd = this.startHour + this.duration;
        int otherStart = other.startHour;
        int otherEnd = other.startHour + other.duration;
        return thisStart < otherEnd && thisEnd > otherStart;
    }
}

class SubTimeSlot {
    int parentId; // Links to the parent TimeSlot's ID
    String day;
    int startHour;
    int duration;

    SubTimeSlot(int parentId, String day, int startHour, int duration) {
        this.parentId = parentId;
        this.day = day;
        this.startHour = startHour;
        this.duration = duration;
    }

    public boolean overlaps(SubTimeSlot other) {
        if (!this.day.equals(other.day)) return false;
        int thisStart = this.startHour;
        int thisEnd = this.startHour + this.duration;
        int otherStart = other.startHour;
        int otherEnd = other.startHour + other.duration;
        return thisStart < otherEnd && thisEnd > otherStart;
    }
}

class ScheduledClass {
    Subject subject;
    Teacher teacher;
    Room room;
    SubTimeSlot subTimeSlot;
    int timeSlotId; // Store the parent time slot ID for database saving

    ScheduledClass(Subject subject, Teacher teacher, Room room, SubTimeSlot subTimeSlot, int timeSlotId) {
        this.subject = subject;
        this.teacher = teacher;
        this.room = room;
        this.subTimeSlot = subTimeSlot;
        this.timeSlotId = timeSlotId;
    }
}

class DatabaseManager {
    private Connection conn;
    private static final String[] VALID_DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    DatabaseManager() throws SQLException, IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/config.properties")) {
            props.load(fis);
        }
        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/timetable_ds");
        String user = props.getProperty("db.user", "root");
        String password = props.getProperty("db.password", "anAnt@77");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
            initializeSchema();
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }

    private void initializeSchema() throws SQLException {
        String[] schema = {
                "CREATE TABLE IF NOT EXISTS admins (username VARCHAR(50) PRIMARY KEY, password VARCHAR(100) NOT NULL)",
                "CREATE TABLE IF NOT EXISTS subjects (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE, is_lab BOOLEAN, duration INT, required_room_type VARCHAR(50), lectures_per_week INT)",
                "CREATE TABLE IF NOT EXISTS teachers (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE, unavailable_days TEXT)",
                "CREATE TABLE IF NOT EXISTS rooms (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE, type VARCHAR(50))",
                "CREATE TABLE IF NOT EXISTS time_slots (id INT AUTO_INCREMENT PRIMARY KEY, day VARCHAR(20), start_hour INT, duration INT)",
                "CREATE TABLE IF NOT EXISTS teacher_subjects (teacher_id INT, subject_id INT, PRIMARY KEY (teacher_id, subject_id), FOREIGN KEY (teacher_id) REFERENCES teachers(id), FOREIGN KEY (subject_id) REFERENCES subjects(id))",
                "CREATE TABLE IF NOT EXISTS timetable (id INT AUTO_INCREMENT PRIMARY KEY, subject_id INT, teacher_id INT, room_id INT, time_slot_id INT, sub_slot_start_hour INT, sub_slot_duration INT, FOREIGN KEY (subject_id) REFERENCES subjects(id), FOREIGN KEY (teacher_id) REFERENCES teachers(id), FOREIGN KEY (room_id) REFERENCES rooms(id), FOREIGN KEY (time_slot_id) REFERENCES time_slots(id))"
        };
        try (Statement stmt = conn.createStatement()) {
            for (String sql : schema) {
                stmt.execute(sql);
            }
        }
    }

    void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    List<Subject> getSubjects() throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                subjects.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBoolean("is_lab"),
                        rs.getInt("duration"),
                        rs.getString("required_room_type"),
                        rs.getInt("lectures_per_week")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subjects: " + e.getMessage());
            throw e;
        }
        return subjects;
    }

    List<Teacher> getTeachers() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teachers";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int teacherId = rs.getInt("id");
                String name = rs.getString("name");
                List<String> unavailableDays = new ArrayList<>();
                String days = rs.getString("unavailable_days");
                if (days != null && !days.isEmpty()) {
                    unavailableDays = Arrays.asList(days.split(","));
                }
                List<Integer> subjectIds = new ArrayList<>();
                String subjectQuery = "SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(subjectQuery)) {
                    pstmt.setInt(1, teacherId);
                    try (ResultSet subjectRs = pstmt.executeQuery()) {
                        while (subjectRs.next()) {
                            subjectIds.add(subjectRs.getInt("subject_id"));
                        }
                    }
                }
                teachers.add(new Teacher(teacherId, name, unavailableDays, subjectIds));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching teachers: " + e.getMessage());
            throw e;
        }
        return teachers;
    }

    List<Room> getRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                rooms.add(new Room(rs.getInt("id"), rs.getString("name"), rs.getString("type")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms: " + e.getMessage());
            throw e;
        }
        return rooms;
    }

    List<TimeSlot> getTimeSlots() throws SQLException {
        List<TimeSlot> timeSlots = new ArrayList<>();
        String query = "SELECT * FROM time_slots";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                timeSlots.add(new TimeSlot(
                        rs.getInt("id"),
                        rs.getString("day"),
                        rs.getInt("start_hour"),
                        rs.getInt("duration")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching time slots: " + e.getMessage());
            throw e;
        }
        return timeSlots;
    }

    void saveSchedule(List<ScheduledClass> schedule) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM timetable");
        }
        String query = "INSERT INTO timetable (subject_id, teacher_id, room_id, time_slot_id, sub_slot_start_hour, sub_slot_duration) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (ScheduledClass sc : schedule) {
                pstmt.setInt(1, sc.subject.id);
                pstmt.setInt(2, sc.teacher.id);
                pstmt.setInt(3, sc.room.id);
                pstmt.setInt(4, sc.timeSlotId);
                pstmt.setInt(5, sc.subTimeSlot.startHour);
                pstmt.setInt(6, sc.subTimeSlot.duration);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error saving schedule: " + e.getMessage());
            throw e;
        }
    }

    boolean verifyAdmin(String username, String password) throws SQLException {
        String query = "SELECT password FROM admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return password.equals(storedPassword); // Plain-text comparison (insecure)
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error verifying admin: " + e.getMessage());
            throw e;
        }
    }

    void addSubject(String name, boolean isLab, int duration, String requiredRoomType, int lecturesPerWeek) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM subjects WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new SQLException("Subject with name '" + name + "' already exists.");
                }
            }
        }
        String query = "INSERT INTO subjects (name, is_lab, duration, required_room_type, lectures_per_week) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setBoolean(2, isLab);
            pstmt.setInt(3, duration);
            pstmt.setString(4, requiredRoomType);
            pstmt.setInt(5, lecturesPerWeek);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding subject: " + e.getMessage());
            throw e;
        }
    }

    int addTeacher(String name, String unavailableDays) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM teachers WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new SQLException("Teacher with name '" + name + "' already exists.");
                }
            }
        }
        String[] days = unavailableDays.split(",");
        for (String day : days) {
            if (!day.trim().isEmpty() && !Arrays.asList(VALID_DAYS).contains(day.trim())) {
                throw new SQLException("Invalid day: " + day);
            }
        }
        String query = "INSERT INTO teachers (name, unavailable_days) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, unavailableDays.isEmpty() ? null : unavailableDays);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Failed to retrieve teacher ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding teacher: " + e.getMessage());
            throw e;
        }
    }

    void addTeacherSubject(int teacherId, int subjectId) throws SQLException {
        String query = "INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, subjectId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding teacher-subject mapping: " + e.getMessage());
            throw e;
        }
    }

    void addRoom(String name, String type) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM rooms WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new SQLException("Room with name '" + name + "' already exists.");
                }
            }
        }
        String query = "INSERT INTO rooms (name, type) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding room: " + e.getMessage());
            throw e;
        }
    }

    void addTimeSlot(String day, int startHour, int duration) throws SQLException {
        if (!Arrays.asList(VALID_DAYS).contains(day)) {
            throw new SQLException("Invalid day: " + day);
        }
        String query = "INSERT INTO time_slots (day, start_hour, duration) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, day);
            pstmt.setInt(2, startHour);
            pstmt.setInt(3, duration);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding time slot: " + e.getMessage());
            throw e;
        }
    }

    public void clearData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            try {
                stmt.executeUpdate("DELETE FROM timetable");
                stmt.executeUpdate("DELETE FROM subjects");
                stmt.executeUpdate("DELETE FROM teachers");
                stmt.executeUpdate("DELETE FROM rooms");
                stmt.executeUpdate("DELETE FROM time_slots");
                stmt.executeUpdate("DELETE FROM teacher_subjects");
            } finally {
                stmt.execute("SET FOREIGN_KEY_CHECKS=1");
            }
        } catch (SQLException e) {
            System.err.println("Error clearing data: " + e.getMessage());
            throw e;
        }
    }
}

public class TimetableGenerator {
    private List<Subject> subjects;
    private List<Teacher> teachers;
    private List<Room> rooms;
    private List<TimeSlot> timeSlots;
    private List<ScheduledClass> schedule;
    private DatabaseManager dbManager;
    private boolean isAdminLoggedIn;

    public TimetableGenerator() throws SQLException, IOException {
        dbManager = new DatabaseManager();
        refreshData();
        schedule = new ArrayList<>();
        isAdminLoggedIn = false;
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void refreshData() throws SQLException {
        subjects = dbManager.getSubjects();
        teachers = dbManager.getTeachers();
        rooms = dbManager.getRooms();
        timeSlots = dbManager.getTimeSlots();
    }

    public boolean loginAdmin(String username, String password) throws SQLException {
        isAdminLoggedIn = dbManager.verifyAdmin(username, password);
        return isAdminLoggedIn;
    }

    public List<ScheduledClass> getSchedule() {
        return schedule;
    }

    private boolean checkTeacherConsecutiveHours(Teacher teacher, SubTimeSlot newSubTimeSlot) {
        List<SubTimeSlot> teacherSubTimeSlots = new ArrayList<>();
        for (ScheduledClass sc : schedule) {
            if (sc.teacher.id == teacher.id && sc.subTimeSlot.day.equals(newSubTimeSlot.day)) {
                teacherSubTimeSlots.add(sc.subTimeSlot);
            }
        }
        teacherSubTimeSlots.add(newSubTimeSlot);
        teacherSubTimeSlots.sort(Comparator.comparingInt(ts -> ts.startHour));

        if (teacherSubTimeSlots.isEmpty()) return true;

        int currentConsecutive = 0;
        int previousEnd = -1;
        for (SubTimeSlot ts : teacherSubTimeSlots) {
            if (ts.startHour == previousEnd) {
                currentConsecutive += ts.duration;
                if (currentConsecutive > 2) {
                    return false;
                }
            } else {
                currentConsecutive = ts.duration;
            }
            previousEnd = ts.startHour + ts.duration;
        }
        return true;
    }

    private boolean checkSubjectLectureLimit(Subject subject) {
        int scheduledCount = 0;
        for (ScheduledClass sc : schedule) {
            if (sc.subject.id == subject.id) {
                scheduledCount++;
            }
        }
        return scheduledCount < subject.lecturesPerWeek;
    }

    private boolean checkSubjectMultipleTimesPerDay(Subject subject, SubTimeSlot subTimeSlot) {
        for (ScheduledClass sc : schedule) {
            if (sc.subject.id == subject.id && sc.subTimeSlot.day.equals(subTimeSlot.day)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkBreakAfterTwoHours(SubTimeSlot subTimeSlot) {
        int totalHours = 0;
        for (ScheduledClass sc : schedule) {
            if (sc.subTimeSlot.day.equals(subTimeSlot.day) && sc.subTimeSlot.startHour < subTimeSlot.startHour) {
                totalHours += sc.subTimeSlot.duration;
            }
        }
        if (totalHours >= 2) {
            for (ScheduledClass sc : schedule) {
                if (sc.subTimeSlot.day.equals(subTimeSlot.day) &&
                        sc.subTimeSlot.startHour + sc.subTimeSlot.duration == subTimeSlot.startHour) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean generateTimetable() throws SQLException {
        if (!isAdminLoggedIn) {
            System.err.println("Only admin can generate the timetable. Please login first.");
            return false;
        }
        schedule.clear();

        List<Subject> subjectsToSchedule = new ArrayList<>();
        for (Subject subject : subjects) {
            for (int i = 0; i < subject.lecturesPerWeek; i++) {
                subjectsToSchedule.add(subject);
            }
        }

        boolean success = scheduleSubjects(subjectsToSchedule, 0);
        if (success) {
            dbManager.saveSchedule(schedule);
        }
        return success;
    }

    private boolean scheduleSubjects(List<Subject> subjectsToSchedule, int subjectIndex) {
        if (subjectIndex >= subjectsToSchedule.size()) {
            return true;
        }

        Subject subject = subjectsToSchedule.get(subjectIndex);
        int duration = subject.isLab && subject.duration < 2 ? 2 : subject.duration;

        for (Teacher teacher : teachers) {
            if (!teacher.subjectIds.contains(subject.id)) continue;

            for (TimeSlot timeSlot : timeSlots) {
                if (teacher.unavailableDays.contains(timeSlot.day)) continue;

                List<SubTimeSlot> subSlots = timeSlot.generateSubSlots(duration);
                for (SubTimeSlot subTimeSlot : subSlots) {
                    if (!checkTeacherConsecutiveHours(teacher, subTimeSlot)) continue;
                    if (!checkSubjectMultipleTimesPerDay(subject, subTimeSlot)) continue;
                    if (!checkBreakAfterTwoHours(subTimeSlot)) continue;

                    for (Room room : rooms) {
                        if (!room.type.equals(subject.requiredRoomType)) continue;

                        boolean isRoomFree = true;
                        boolean isTeacherFree = true;
                        for (ScheduledClass sc : schedule) {
                            if (sc.room.id == room.id && sc.subTimeSlot.overlaps(subTimeSlot)) {
                                isRoomFree = false;
                            }
                            if (sc.teacher.id == teacher.id && sc.subTimeSlot.overlaps(subTimeSlot)) {
                                isTeacherFree = false;
                            }
                        }

                        if (isRoomFree && isTeacherFree) {
                            ScheduledClass sc = new ScheduledClass(subject, teacher, room, subTimeSlot, timeSlot.id);
                            schedule.add(sc);

                            if (scheduleSubjects(subjectsToSchedule, subjectIndex + 1)) {
                                return true;
                            }

                            schedule.remove(schedule.size() - 1);
                        }
                    }
                }
            }
        }
        return false;
    }

    public String generateLatexForPDF() {
        StringBuilder latex = new StringBuilder();
        latex.append("\\documentclass{article}\n");
        latex.append("\\usepackage{geometry}\n");
        latex.append("\\geometry{a4paper, margin=1in}\n");
        latex.append("\\usepackage{booktabs}\n");
        latex.append("\\usepackage{longtable}\n");
        latex.append("\\title{Timetable}\n");
        latex.append("\\author{Automated Timetable Generator}\n");
        latex.append("\\date{May 20, 2025}\n");
        latex.append("\\begin{document}\n");
        latex.append("\\maketitle\n");
        latex.append("\\section*{Generated Timetable}\n");
        latex.append("\\begin{longtable}{p{2.5cm}p{2.5cm}p{2.5cm}p{2.5cm}p{2.5cm}p{2cm}}\n");
        latex.append("\\toprule\n");
        latex.append("Subject & Teacher & Room & Day & Start Time & Duration \\\\ \\midrule\n");

        for (ScheduledClass sc : schedule) {
            String row = String.format("%s & %s & %s & %s & %d:00 & %d hours \\\\ \n",
                    sc.subject.name.replace("_", "\\_"),
                    sc.teacher.name.replace("_", "\\_"),
                    sc.room.name.replace("_", "\\_"),
                    sc.subTimeSlot.day, sc.subTimeSlot.startHour, sc.subTimeSlot.duration);
            latex.append(row);
        }

        latex.append("\\bottomrule\n");
        latex.append("\\end{longtable}\n");
        latex.append("\\end{document}\n");
        return latex.toString();
    }

    public void exportTimetableToPDF() throws IOException {
        if (schedule.isEmpty()) {
            throw new IOException("No timetable to export. Please generate a timetable first.");
        }
        String latexContent = generateLatexForPDF();
        try (FileWriter writer = new FileWriter("timetable.tex")) {
            writer.write(latexContent);
        }
        System.out.println("LaTeX code saved to timetable.tex. Compile with a LaTeX compiler to create the PDF.");
    }
}