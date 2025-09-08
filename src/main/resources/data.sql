INSERT INTO usuario(username, password, role)
VALUES ('user1','{noop}1234','ROLE_ADMIN');

INSERT INTO usuario(username, password, role)
VALUES ('user2','{noop}1234','ROLE_ARBITRO');

INSERT INTO usuario(username, password, role)
VALUES ('user3','{noop}1234','ROLE_ENTRENADOR');

-- Usuario específico para Sebastian Medina
INSERT INTO usuario(username, password, role)
VALUES ('sebastian.medina','{noop}arbitro123','ROLE_ARBITRO');

INSERT INTO arbitros (nombre, contraseña, username, cedula, phone, speciality, scale, photo_data, photo_content_type, photo_filename, unavailability_dates, usuario_id) 
VALUES ('Sebastian Medina', 'arbitro123', 'sebastian.medina', '1058198772', '3222469936', 'Campo', 'Internacional', NULL, NULL, NULL, '', 4);

-- Partidos para sebastian medina (usando ID 1 que será el primer árbitro insertado)
INSERT INTO partidos (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-01', '15:00', 'Equipo A', 'Equipo B', 'PROGRAMADO', 1);

INSERT INTO partidos (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-05', '18:00', 'Equipo C', 'Equipo D', 'PROGRAMADO', 1);
