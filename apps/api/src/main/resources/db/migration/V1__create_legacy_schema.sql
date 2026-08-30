CREATE TABLE application_user (
    app_user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40),
    email VARCHAR(80) NOT NULL,
    password VARCHAR(225) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    registered_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_application_user_email UNIQUE (email),
    CONSTRAINT ck_application_user_type CHECK (type IN ('Student', 'Client', 'IRM User', 'Admin')),
    CONSTRAINT ck_application_user_status CHECK (status IN ('Pending', 'Active', 'Blocked', 'Removed'))
);

CREATE TABLE user_verification (
    verification_id INT AUTO_INCREMENT PRIMARY KEY,
    app_user_id INT NOT NULL,
    server_ref VARCHAR(48) NOT NULL,
    code VARCHAR(6),
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    expiration_date DATETIME(6) NOT NULL,
    updated_date DATETIME(6),
    CONSTRAINT uk_user_verification_app_user UNIQUE (app_user_id),
    CONSTRAINT uk_user_verification_server_ref UNIQUE (server_ref),
    CONSTRAINT fk_user_verification_user FOREIGN KEY (app_user_id) REFERENCES application_user (app_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_user_verification_type CHECK (type IN ('Email Verification', 'Forgot Password')),
    CONSTRAINT ck_user_verification_status CHECK (status IN ('Active', 'Inactive'))
);

CREATE INDEX idx_user_verification_expiration ON user_verification (expiration_date);

CREATE TABLE authentication_token (
    auth_token_id INT AUTO_INCREMENT PRIMARY KEY,
    app_user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiration_date DATETIME(6) NOT NULL,
    updated_date DATETIME(6),
    CONSTRAINT uk_authentication_token_app_user UNIQUE (app_user_id),
    CONSTRAINT uk_authentication_token_token UNIQUE (token),
    CONSTRAINT fk_authentication_token_user FOREIGN KEY (app_user_id) REFERENCES application_user (app_user_id) ON DELETE CASCADE
);

CREATE INDEX idx_authentication_token_expiration ON authentication_token (expiration_date);

CREATE TABLE student (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    app_user_id INT NOT NULL,
    name VARCHAR(255),
    wintec_id VARCHAR(20),
    gender VARCHAR(16),
    student_type VARCHAR(32),
    personal_email VARCHAR(255),
    student_email VARCHAR(255),
    phone_number VARCHAR(255),
    personal_statement VARCHAR(512),
    cv_link VARCHAR(255),
    linkedin_link VARCHAR(255),
    portfolio_link VARCHAR(255),
    github_link VARCHAR(255),
    average_grade VARCHAR(10),
    programme_of_study VARCHAR(255),
    area_of_study VARCHAR(255),
    internship_options VARCHAR(255),
    preferred_companies VARCHAR(255),
    first_preference VARCHAR(255),
    second_preference VARCHAR(255),
    skills VARCHAR(255),
    favourite_courses VARCHAR(255),
    reference VARCHAR(50),
    speciality VARCHAR(255),
    qualification_type VARCHAR(255),
    submission_date DATETIME(6),
    review_status VARCHAR(50),
    interview_readiness VARCHAR(5),
    app_status INT,
    intern_status INT,
    CONSTRAINT uk_student_app_user UNIQUE (app_user_id),
    CONSTRAINT fk_student_user FOREIGN KEY (app_user_id) REFERENCES application_user (app_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_student_gender CHECK (gender IS NULL OR gender IN ('Male', 'Female')),
    CONSTRAINT ck_student_type CHECK (student_type IS NULL OR student_type IN ('Domestic Student', 'International Student')),
    CONSTRAINT ck_student_interview_readiness CHECK (interview_readiness IS NULL OR interview_readiness IN ('TRUE', 'FALSE'))
);

CREATE INDEX idx_student_wintec_id ON student (wintec_id);
CREATE INDEX idx_student_email ON student (student_email);
