INSERT INTO usuario(username, password, role)
VALUES ('user1','{noop}1234','ROLE_ADMIN');

INSERT INTO usuario(username, password, role)
VALUES ('user2','{noop}1234','ROLE_ARBITRO');

INSERT INTO usuario(username, password, role)
VALUES ('user3','{noop}1234','ROLE_ENTRENADOR');

-- Usuario específico para Sebastian Medina
INSERT INTO usuario(username, password, role)
VALUES ('sebastian.medina','{noop}arbitro123','ROLE_ARBITRO');

INSERT INTO arbitros (nombre, contraseña, username, cedula, phone, speciality, scale, photo_data, photo_content_type, photo_filename, usuario_id) 
VALUES ('Sebastian Medina', 'arbitro123', 'sebastian.medina', '1058198772', '3222469936', 'Campo', 'Internacional', NULL, NULL, NULL, 4);

-- Partidos para sebastian medina (usando ID 1 que será el primer árbitro insertado)
INSERT INTO partidos (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-09-01', '15:00', 'Lakers Bogotá', 'Equipo A', 'FINALIZADO', 1);

INSERT INTO partidos (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-09-05', '18:00', 'Bulls Medellín', 'Equipo C', 'FINALIZADO', 1);

INSERT INTO partidos (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-01', '15:00', 'Equipo E', 'Equipo F', 'PROGRAMADO', 1);

INSERT INTO partidos (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-05', '18:00', 'Equipo G', 'Equipo H', 'PROGRAMADO', 1);

-- Entrenadores para las reseñas
INSERT INTO entrenadores (nombre, apellidos, documento, email, telefono, equipo, anos_experiencia, categoria, activo) 
VALUES ('Carlos', 'Rodriguez', '12345678', 'carlos.rodriguez@email.com', '3111234567', 'Lakers Bogotá', 5, 'PROFESIONAL', true);

INSERT INTO entrenadores (nombre, apellidos, documento, email, telefono, equipo, anos_experiencia, categoria, activo) 
VALUES ('Maria', 'Gonzalez', '87654321', 'maria.gonzalez@email.com', '3119876543', 'Bulls Medellín', 8, 'PROFESIONAL', true);

-- Reseñas de ejemplo para Sebastian Medina (arbitro_id = 1) relacionadas con partidos específicos
INSERT INTO reseñas (arbitro_id, entrenador_id, partido_id, puntuacion, descripcion, fecha_creacion) 
VALUES (1, 1, 1, 5, 'Excelente arbitraje! Sebastian demostró gran conocimiento de las reglas y mantuvo el control del partido en todo momento. Sus decisiones fueron justas y consistentes durante toda la final regional. Definitivamente uno de los mejores árbitros con los que hemos trabajado.', '2025-09-01');

INSERT INTO reseñas (arbitro_id, entrenador_id, partido_id, puntuacion, descripcion, fecha_creacion) 
VALUES (1, 2, 2, 4, 'Muy buen desempeño en general. Sebastian mostró profesionalismo y buena comunicación con los jugadores. Hubo un par de decisiones dudosas en el tercer cuarto, pero en general manejó bien la presión del juego semifinal. Lo recomendaría para futuros partidos importantes.', '2025-09-05');

