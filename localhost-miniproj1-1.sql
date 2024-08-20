SET SERVEROUTPUT ON;


CREATE TABLE Members (
    id VARCHAR2(50) PRIMARY KEY,
    password VARCHAR2(50),
    name VARCHAR2(50),
    phone VARCHAR2(20),
    address VARCHAR2(255),
    gender CHAR(1),
    last_login TIMESTAMP,
    last_logout TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0
);





CREATE TABLE LoginHistory (
    id NUMBER PRIMARY KEY,
    member_id VARCHAR2(50),
    login_time TIMESTAMP,
    logout_time TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES Members(id)
);

CREATE SEQUENCE login_history_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_login_history_id
BEFORE INSERT ON LoginHistory
FOR EACH ROW
BEGIN
    :NEW.id := login_history_seq.NEXTVAL;
END;

CREATE TABLE Posts (
    post_id NUMBER PRIMARY KEY,
    author_id VARCHAR2(50),
    title VARCHAR2(255),
    content VARCHAR2(255),
    password VARCHAR2(50),
    views NUMBER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES Members(id)
);


CREATE SEQUENCE posts_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER trg_posts_id
BEFORE INSERT ON Posts
FOR EACH ROW
BEGIN
    :NEW.post_id := posts_seq.NEXTVAL;
END;

CREATE TABLE Members (
    id VARCHAR2(50) PRIMARY KEY,
    password VARCHAR2(50),
    name VARCHAR2(50),
    phone VARCHAR2(20),
    address VARCHAR2(255),
    gender VARCHAR2(10),
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_logout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted NUMBER(1) DEFAULT 0
);

