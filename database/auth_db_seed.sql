
USE auth_db;

INSERT INTO users_auth (username, role, password_hash, status)
VALUES ('admin1', 'ADMIN', '$2a$10$6btCbMY4uvqPvebK32ejUuMpQuzB26uoCgn38o/kFmPe..co9jLGW', 'ACTIVE');

INSERT INTO users_auth (username, role, password_hash, status)
VALUES ('inst1', 'INSTRUCTOR', '$2a$10$6btCbMY4uvqPvebK32ejUuMpQuzB26uoCgn38o/kFmPe..co9jLGW', 'ACTIVE');

INSERT INTO users_auth (username, role, password_hash, status)
VALUES ('inst2', 'INSTRUCTOR', '$2a$10$6btCbMY4uvqPvebK32ejUuMpQuzB26uoCgn38o/kFmPe..co9jLGW', 'ACTIVE');

INSERT INTO users_auth (username, role, password_hash, status)
VALUES ('stu1', 'STUDENT', '$2a$10$6btCbMY4uvqPvebK32ejUuMpQuzB26uoCgn38o/kFmPe..co9jLGW', 'ACTIVE');

INSERT INTO users_auth (username, role, password_hash, status)
VALUES ('stu2', 'STUDENT', '$2a$10$6btCbMY4uvqPvebK32ejUuMpQuzB26uoCgn38o/kFmPe..co9jLGW', 'ACTIVE');

INSERT INTO users_auth (username, role, password_hash, status)
VALUES ('stu3', 'STUDENT', '$2a$10$6btCbMY4uvqPvebK32ejUuMpQuzB26uoCgn38o/kFmPe..co9jLGW', 'ACTIVE');
