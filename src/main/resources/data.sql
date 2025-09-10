INSERT INTO usuarios(username, password, role, email, activo) 
VALUES ('user1', '{noop}1234', 'ROLE_ADMIN', 'admin@test.com', true);

INSERT INTO usuarios(username, password, role, email, activo) 
VALUES ('user2', '{noop}1234', 'ROLE_ARBITRO', 'arbitro@test.com', true);

INSERT INTO usuarios(username, password, role, email, activo) 
VALUES ('user3', '{noop}1234', 'ROLE_ENTRENADOR', 'entrenador@test.com', true);

INSERT INTO usuarios(username, password, role, email, activo) 
VALUES ('sebastian.medina', '{noop}arbitro123', 'ROLE_ARBITRO', 'sebas@test.com', true);

INSERT INTO usuarios(username, password, role, email, activo) 
VALUES ('paula.lp', '{noop}arbitro1234', 'ROLE_ARBITRO', 'pau@test.com', true);

-- Escalafones disponibles
INSERT INTO escalafon (nombre, honorario_base) 
VALUES ('INTERNACIONAL', 250000);

INSERT INTO escalafon (nombre, honorario_base) 
VALUES ('NACIONAL', 180000);

INSERT INTO escalafon (nombre, honorario_base) 
VALUES ('LOCAL', 120000);

--Arbitros
INSERT INTO arbitro (nombre, contraseña, username, cedula, phone, speciality, photo_data, photo_content_type, photo_filename, usuario_id, escalafon_id) 
VALUES ('Paula Lop', '{noop}arbitro1234', 'paula.lp', '34567', '3222469936', 'Campo', NULL, NULL, NULL, 5, 1);

INSERT INTO arbitro (nombre, contraseña, username, cedula, phone, speciality, photo_data, photo_content_type, photo_filename, usuario_id, escalafon_id) 
VALUES ('Sebastian Medina', '{noop}arbitro123', 'sebastian.medina', '1058198772', '3222469936', 'Campo', NULL, NULL, NULL, 4, 1);

-- Equipos
INSERT INTO equipo(nombre, estado, ciudad, fundacion, logo)
VALUES ('Lakers', true, 'Los Angeles', 1947, 'https://images.seeklogo.com/logo-png/21/2/los-angeles-lakers-logo-png_seeklogo-216335.png');

INSERT INTO equipo(nombre, estado, ciudad, fundacion, logo)
VALUES ('Chicago Bulls', true, 'Illinois', 1996, 'https://www.citypng.com/public/uploads/preview/hd-chicago-bulls-vector-logo-png-701751694711025arhvkh69nh.png');

INSERT INTO equipo(nombre, estado, ciudad, fundacion, logo)
VALUES ('Paisitas', false, 'Medellin', 2025, 'https://static.wikia.nocookie.net/logopedia/images/b/b4/Atl%C3%A9tico_Nacional_1.png');

-- Partidos para Sebastian Medina (arbitro_id = 2, porque es el segundo árbitro insertado)
INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-05', '15:00', 1, 2, 'PROGRAMADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-11', '18:00', 1, 3, 'PROGRAMADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-08-15', '16:00', 2, 3, 'FINALIZADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-08-20', '19:00', 3, 1, 'FINALIZADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-09-02', '17:30', 1, 3, 'FINALIZADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-20', '14:00', 2, 1, 'PROGRAMADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-11-05', '18:30', 3, 2, 'PROGRAMADO', 2);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-09-08', '20:00', 2, 3, 'FINALIZADO', 2);

-- Entrenadores (ajusta el nombre de tabla según sea necesario: entrenador vs entrenadores)
INSERT INTO entrenadores (nombre_completo, cedula, email, telefono, equipo, experiencia, categoria, activo, fecha_creacion, usuario_id) 
VALUES ('Carlos Rodriguez', '12345678', 'carlos.rodriguez@email.com', '3101234567', 'Lakers', 5, 'PROFESIONAL', true, CURRENT_TIMESTAMP, NULL);

INSERT INTO entrenadores (nombre_completo, cedula, email, telefono, equipo, experiencia, categoria, activo, fecha_creacion, usuario_id) 
VALUES ('Sara López', '87654321', 'sara.lopez@email.com', '3101234568', 'Chicago Bulls', 5, 'PROFESIONAL', true, CURRENT_TIMESTAMP, 6);

INSERT INTO entrenadores (nombre_completo, cedula, email, telefono, equipo, experiencia, categoria, activo, fecha_creacion, usuario_id) 
VALUES ('Nicolás Ospina', '11223344', 'nicolas.ospina@email.com', '3101234569', 'Paisitas', 5, 'PROFESIONAL', true, CURRENT_TIMESTAMP, NULL);

-- Reseñas para Sebastian Medina
INSERT INTO reseña (puntuacion, descripcion, fecha_creacion, arbitro_id, entrenador_id, partido_id) 
VALUES (5, 'Excelente arbitraje, muy profesional y justo en todas sus decisiones. Mantuvo el control del partido en todo momento.', '2025-08-15 18:00:00', 2, 1, 3);

INSERT INTO reseña (puntuacion, descripcion, fecha_creacion, arbitro_id, entrenador_id, partido_id) 
VALUES (4, 'Buen trabajo en general, aunque algunas decisiones fueron cuestionables. En general, un arbitraje sólido.', '2025-08-20 21:00:00', 2, 2, 4);

INSERT INTO reseña (puntuacion, descripcion, fecha_creacion, arbitro_id, entrenador_id, partido_id) 
VALUES (5, 'Arbitraje impecable. Sebastian demostró gran conocimiento del reglamento y excelente manejo de situaciones difíciles.', '2025-09-02 19:30:00', 2, 3, 5);

INSERT INTO reseña (puntuacion, descripcion, fecha_creacion, arbitro_id, entrenador_id, partido_id) 
VALUES (4, 'Muy profesional, puntual y con buen criterio. Recomendamos ampliamente sus servicios de arbitraje.', '2025-08-16 10:00:00', 2, 1, NULL);

INSERT INTO reseña (puntuacion, descripcion, fecha_creacion, arbitro_id, entrenador_id, partido_id) 
VALUES (3, 'Arbitraje correcto pero podría mejorar en la comunicación con los jugadores. Decisiones técnicas acertadas.', '2025-08-22 14:30:00', 2, 2, NULL);