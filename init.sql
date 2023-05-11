CREATE DATABASE healthsystem;

CREATE USER postgres WITH PASSWORD 'postgres';

GRANT ALL PRIVILEGES ON DATABASE healthsystem TO postgres;

USE healthsystem;

CREATE TABLE IF NOT EXISTS doctor (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    zipcode VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS patient (
                                       id BIGINT PRIMARY KEY,
                                       first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS appointment (
                                           id BIGSERIAL PRIMARY KEY,
                                           appointment_time TIMESTAMP NOT NULL,
                                           doctor_id BIGINT NOT NULL,
                                           patient_id BIGINT NOT NULL,
                                           FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (patient_id) REFERENCES patient(id)
    );

INSERT INTO doctor (first_name, last_name, specialty, zipcode, phone, email)
VALUES ('John', 'Doe', 'Cardiologist', '12345', '555-123-4567', 'john.doe@example.com');

INSERT INTO doctor (first_name, last_name, specialty, zipcode, phone, email)
VALUES ('Jane', 'Smith', 'Dermatologist', '67890', '555-987-6543', 'jane.smith@example.com');

INSERT INTO doctor (first_name, last_name, specialty, zipcode, phone, email)
VALUES ('Michael', 'Brown', 'Neurologist', '54321', '555-111-2222', 'michael.brown@example.com');

INSERT INTO patient (first_name, last_name, age, gender, phone, email)
VALUES ('Alice', 'Johnson', 28, 'Female', '555-333-4444', 'alice.johnson@example.com');

INSERT INTO patient (first_name, last_name, age, gender, phone, email)
VALUES ('Bob', 'Wilson', 34, 'Male', '555-666-7777', 'bob.wilson@example.com');

INSERT INTO patient (first_name, last_name, age, gender, phone, email)
VALUES ('Charlie', 'Lee', 23, 'Male', '555-888-9999', 'charlie.lee@example.com');

INSERT INTO appointment (appointment_time, doctor_id, patient_id)
VALUES ('2023-06-01 10:00:00', 1, 1);

INSERT INTO appointment (appointment_time, doctor_id, patient_id)
VALUES ('2023-06-02 14:00:00', 2, 2);

INSERT INTO appointment (appointment_time, doctor_id, patient_id)
VALUES ('2023-06-03 16:00:00', 3, 3);
