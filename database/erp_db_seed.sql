
USE erp_db;

INSERT INTO students (user_id, roll_no, program, year, email, phone) VALUES
(4, '2021CS101', 'Computer Science', 3, 'stu1@university.edu', '555-0101'),
(5, '2021CS102', 'Computer Science', 3, 'stu2@university.edu', '555-0102'),
(6, '2022EE201', 'Electrical Engineering', 2, 'stu3@university.edu', '555-0103');

INSERT INTO instructors (user_id, employee_id, department, email, phone) VALUES
(2, 'EMP001', 'Computer Science', 'inst1@university.edu', '555-0201'),
(3, 'EMP002', 'Mathematics', 'inst2@university.edu', '555-0202');

INSERT INTO courses (code, title, credits, description) VALUES
('CS101', 'Introduction to Programming', 4, 'Basic programming concepts using Python'),
('CS201', 'Data Structures', 4, 'Fundamental data structures and algorithms'),
('CS301', 'Database Systems', 3, 'Relational databases and SQL'),
('MATH101', 'Calculus I', 3, 'Differential calculus and applications'),
('MATH201', 'Linear Algebra', 3, 'Vector spaces and matrix theory'),
('EE101', 'Circuit Theory', 4, 'Basic electrical circuit analysis');

INSERT INTO sections (course_id, instructor_id, section_code, day_time, room, capacity, semester, year) VALUES
(1, 1, 'A', 'Mon/Wed 10:00-11:30', 'Room 101', 30, 'Fall', 2024),
(1, 1, 'B', 'Tue/Thu 14:00-15:30', 'Room 102', 30, 'Fall', 2024),
(2, 1, 'A', 'Mon/Wed 14:00-15:30', 'Room 201', 25, 'Fall', 2024),
(3, 1, 'A', 'Tue/Thu 10:00-11:30', 'Room 301', 25, 'Fall', 2024),
(4, 2, 'A', 'Mon/Wed/Fri 09:00-10:00', 'Room 105', 40, 'Fall', 2024),
(5, 2, 'A', 'Tue/Thu 11:00-12:30', 'Room 205', 30, 'Fall', 2024);

INSERT INTO enrollments (student_id, section_id, status, drop_deadline) VALUES
(1, 1, 'ENROLLED', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(1, 3, 'ENROLLED', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(1, 5, 'ENROLLED', DATE_ADD(NOW(), INTERVAL 30 DAY));

INSERT INTO enrollments (student_id, section_id, status, drop_deadline) VALUES
(2, 2, 'ENROLLED', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, 4, 'ENROLLED', DATE_ADD(NOW(), INTERVAL 30 DAY));

UPDATE sections SET enrolled_count = 1 WHERE section_id IN (1, 3, 5);
UPDATE sections SET enrolled_count = 1 WHERE section_id IN (2, 4);

INSERT INTO grades (enrollment_id, component, score, max_score, weightage) VALUES
(1, 'Quiz', 18.0, 20.0, 20.0),
(1, 'Midterm', 27.0, 30.0, 30.0),
(1, 'End-Sem', 42.0, 50.0, 50.0);

INSERT INTO grades (enrollment_id, component, score, max_score, weightage) VALUES
(2, 'Quiz', 16.0, 20.0, 20.0),
(2, 'Midterm', 25.0, 30.0, 30.0),
(2, 'End-Sem', 40.0, 50.0, 50.0);

INSERT INTO grades (enrollment_id, component, score, max_score, weightage) VALUES
(4, 'Quiz', 19.0, 20.0, 20.0),
(4, 'Midterm', 28.0, 30.0, 30.0);
