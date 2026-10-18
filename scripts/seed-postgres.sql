-- Clean existing data
TRUNCATE TABLE messages, ticket_assignment_history, ticket_activities, tickets, admins, advisors, students, accounts CASCADE;

-- 1. Accounts (Password is BCrypt for 'password123': $2a$10$PrQKXhy5QCBBqjHSbnRICuCAuQ4fm4aZM2E/K5LBhJFIwj6e0DY9K)
INSERT INTO accounts (id, first_name, last_name, email, password, account_type) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Carlos', 'Mendoza', 'asesor@demo.com', '$2a$10$PrQKXhy5QCBBqjHSbnRICuCAuQ4fm4aZM2E/K5LBhJFIwj6e0DY9K', 'ADVISOR'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Ana', 'García', 'estudiante1@demo.com', '$2a$10$PrQKXhy5QCBBqjHSbnRICuCAuQ4fm4aZM2E/K5LBhJFIwj6e0DY9K', 'STUDENT'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Luis', 'Torres', 'estudiante2@demo.com', '$2a$10$PrQKXhy5QCBBqjHSbnRICuCAuQ4fm4aZM2E/K5LBhJFIwj6e0DY9K', 'STUDENT'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Administrador', 'Sistema', 'admin@demo.com', '$2a$10$PrQKXhy5QCBBqjHSbnRICuCAuQ4fm4aZM2E/K5LBhJFIwj6e0DY9K', 'ADMIN');

-- 2. Students (Joined table)
INSERT INTO students (id, student_code, career, current_semester, phone_number) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'U20211001', 'Ingeniería de Sistemas', 6, '+51999111222'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'U20211002', 'Ingeniería de Software', 4, '+51999333444');

-- 3. Advisors (Joined table)
INSERT INTO advisors (id, employee_code, max_concurrent_tickets, is_available) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'EMP-001', 3, TRUE);

-- 4. Admins (Joined table)
INSERT INTO admins (id) VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd');

-- 5. Tickets (Open, In Progress, Resolved)
INSERT INTO tickets (id, title, description, started_at, closed_at, generated_by, assigned_to, status, resolution_summary, resolution_category, category) VALUES
(
    'c1111111-1111-1111-1111-111111111111',
    'Problemas con el pago de la pensión',
    'Realicé mi pago de pensión pero no aparece reflejado en el sistema.',
    NOW() - INTERVAL '5 days',
    NULL,
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'IN_PROGRESS',
    NULL,
    NULL,
    'BILLING'
),
(
    'c2222222-2222-2222-2222-222222222222',
    'Consulta sobre convalidación de cursos',
    'Deseo saber qué requisitos necesito para convalidar Matemática II.',
    NOW() - INTERVAL '3 days',
    NULL,
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    NULL,
    'OPEN',
    NULL,
    NULL,
    'GENERAL'
),
(
    'c3333333-3333-3333-3333-333333333333',
    'Horarios de laboratorio de cómputo',
    '¿En qué horario están disponibles los laboratorios de programación?',
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '1 day',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'RESOLVED',
    'Se compartió la guía de horarios de laboratorios y canales de reserva.',
    'GUIDANCE_PROVIDED',
    'GENERAL'
);

-- 6. Ticket Assignment History
INSERT INTO ticket_assignment_history (id, ticket_id, advisor_id, assigned_at, unassigned_at, reason) VALUES
(
    'e1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    NOW() - INTERVAL '4 days',
    NULL,
    'Initial assignment'
),
(
    'e2222222-2222-2222-2222-222222222222',
    'c3333333-3333-3333-3333-333333333333',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '1 day',
    'Resolved inquiry'
);

-- 7. Ticket Activities
INSERT INTO ticket_activities (id, ticket_id, actor_id, activity_type, details, created_at) VALUES
(
    'f1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'CREATED',
    'Ticket created by student',
    NOW() - INTERVAL '5 days'
),
(
    'f2222222-2222-2222-2222-222222222222',
    'c1111111-1111-1111-1111-111111111111',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'ASSIGNED',
    'Ticket assigned to advisor Carlos Mendoza',
    NOW() - INTERVAL '4 days'
),
(
    'f3333333-3333-3333-3333-333333333333',
    'c3333333-3333-3333-3333-333333333333',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'STATUS_CHANGED',
    'Ticket resolved by advisor',
    NOW() - INTERVAL '1 day'
);

-- 8. Messages
INSERT INTO messages (id, content, sent_at, ticket_id, account_id) VALUES
(
    'd1111111-1111-1111-1111-111111111111',
    'Buenas tardes, realicé mi pago pero aún figura como pendiente.',
    NOW() - INTERVAL '10 minutes',
    'c1111111-1111-1111-1111-111111111111',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    'd2222222-2222-2222-2222-222222222222',
    'Hola Ana, indícanos el número de operación del voucher por favor.',
    NOW() - INTERVAL '5 minutes',
    'c1111111-1111-1111-1111-111111111111',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
),
(
    'd3333333-3333-3333-3333-333333333333',
    'Hola, deseo saber qué requisitos necesito para convalidar Matemática II.',
    NOW() - INTERVAL '20 minutes',
    'c2222222-2222-2222-2222-222222222222',
    'cccccccc-cccc-cccc-cccc-cccccccccccc'
);
