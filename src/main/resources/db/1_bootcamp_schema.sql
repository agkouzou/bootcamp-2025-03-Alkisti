TRUNCATE TABLE
    users, messages, threads,
    user_profile_settings, user_profile_settings_traits
    RESTART IDENTITY CASCADE;

CREATE TABLE IF NOT EXISTS threads (
                                       id BIGSERIAL PRIMARY KEY,
                                       title VARCHAR(255),
                                       completion_model VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS messages (
                                        id BIGSERIAL PRIMARY KEY,
                                        content TEXT,
                                        thread_id BIGINT NOT NULL,
                                        is_completion BOOLEAN,
                                        completion_model VARCHAR(255),
                                        CONSTRAINT fk_thread FOREIGN KEY(thread_id) REFERENCES threads(id)
);

-- ALTER TABLE messages ALTER COLUMN content TYPE VARCHAR(2000);
ALTER TABLE messages ALTER COLUMN content TYPE TEXT;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    name TEXT,
    verified BOOLEAN DEFAULT FALSE,
    verification_token TEXT
);

CREATE TABLE IF NOT EXISTS user_profile_settings (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     nickname VARCHAR(255),
                                                     introduction VARCHAR(1000),
                                                     job VARCHAR(255),
                                                     notes VARCHAR(2000),
                                                     user_id BIGINT NOT NULL UNIQUE,
                                                     CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_profile_settings_traits (
                                                            user_profile_settings_id BIGINT NOT NULL,
                                                            traits VARCHAR(255),
                                                            CONSTRAINT fk_profile_settings FOREIGN KEY (user_profile_settings_id) REFERENCES user_profile_settings(id)
);

-- Create test users
INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('chris@ctrlspace.dev', '123456', 'Chris Sekas', true, null);

INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('alkisti@ctrlspace.dev', '123456', 'Alkisti', true, null);

INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('nick@ctrlspace.dev', '123456789', 'Nick', true, null);

INSERT INTO users (email, password, name, verified, verification_token)
VALUES ('george@ctrlspace.dev', '43f43gt45', 'George', true, null);
