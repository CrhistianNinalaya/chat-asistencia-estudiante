SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS priorities;
DROP TABLE IF EXISTS categories;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE accounts (
    id BINARY(16) NOT NULL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    account_type VARCHAR(31) NOT NULL
);

CREATE TABLE tickets (
    id BINARY(16) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP NULL,
    generated_by BINARY(16) NOT NULL,
    assigned_to BINARY(16) NULL,
    active BIT(1) NOT NULL DEFAULT 1,
    category VARCHAR(20) NOT NULL,
    CONSTRAINT fk_ticket_generated_by FOREIGN KEY (generated_by) REFERENCES accounts(id),
    CONSTRAINT fk_ticket_assigned_to FOREIGN KEY (assigned_to) REFERENCES accounts(id)
);

CREATE TABLE messages (
    id BINARY(16) NOT NULL PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    sent_at DATETIME NOT NULL,
    chat_id BINARY(16) NOT NULL,
    account_id BINARY(16) NOT NULL,
    CONSTRAINT fk_msg_chat FOREIGN KEY (chat_id) REFERENCES tickets(id),
    CONSTRAINT fk_msg_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- 3. Cuentas (Password es BCrypt de 123456)
INSERT INTO accounts (id, first_name, last_name, email, password, account_type) VALUES
(UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0), 'Carlos', 'Mendoza', 'asesor@demo.com', '$2a$10$JIHOWoGAatfAQG.8kid6UOl80F2FdBv8z5xzHHu7I7dpoVenr4n46', 'ADVISOR'),
(UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0), 'Ana', 'García', 'estudiante1@demo.com', '$2a$10$JIHOWoGAatfAQG.8kid6UOl80F2FdBv8z5xzHHu7I7dpoVenr4n46', 'STUDENT'),
(UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc', 0), 'Luis', 'Torres', 'estudiante2@demo.com', '$2a$10$JIHOWoGAatfAQG.8kid6UOl80F2FdBv8z5xzHHu7I7dpoVenr4n46', 'STUDENT');

-- 4. Tickets (started_at escalonado para probar SLA: >4d = HIGH, 2-4d = MEDIUM, <2d = LOW)
INSERT INTO tickets (id, title, description, started_at, closed_at, generated_by, assigned_to, active, category) VALUES
(
    UUID_TO_BIN('c1111111-1111-1111-1111-111111111111', 0),
    'Problemas con el pago de la pensión',
    'Realicé mi pago de pensión pero no aparece reflejado en el sistema.',
    NOW() - INTERVAL 5 DAY,
    NULL,
    UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0),
    UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0),
    1,
    'BILLING'
),
(
    UUID_TO_BIN('c2222222-2222-2222-2222-222222222222', 0),
    'Consulta sobre convalidación de cursos',
    'Deseo saber qué requisitos necesito para convalidar Matemática II.',
    NOW() - INTERVAL 3 DAY,
    NULL,
    UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc', 0),
    NULL,
    1,
    'GENERAL'
),
(
    UUID_TO_BIN('c3333333-3333-3333-3333-333333333333', 0),
    'Horarios de laboratorio de cómputo',
    '¿En qué horario están disponibles los laboratorios de programación?',
    NOW() - INTERVAL 2 HOUR,
    NULL,
    UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0),
    NULL,
    1,
    'GENERAL'
);

-- 5. Mensajes
INSERT INTO messages (id, content, sent_at, chat_id, account_id) VALUES
(
    UUID_TO_BIN('d1111111-1111-1111-1111-111111111111', 0),
    'Buenas tardes, realicé mi pago pero aún figura como pendiente.',
    NOW() - INTERVAL 10 MINUTE,
    UUID_TO_BIN('c1111111-1111-1111-1111-111111111111', 0),
    UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 0)
),
(
    UUID_TO_BIN('d2222222-2222-2222-2222-222222222222', 0),
    'Hola Ana, indícanos el número de operación del voucher por favor.',
    NOW() - INTERVAL 5 MINUTE,
    UUID_TO_BIN('c1111111-1111-1111-1111-111111111111', 0),
    UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0)
),
(
    UUID_TO_BIN('d3333333-3333-3333-3333-333333333333', 0),
    'Hola, deseo saber qué requisitos necesito para convalidar Matemática II.',
    NOW() - INTERVAL 20 MINUTE,
    UUID_TO_BIN('c2222222-2222-2222-2222-222222222222', 0),
    UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc', 0)
);
