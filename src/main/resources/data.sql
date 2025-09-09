INSERT INTO usuario(username, password, role)
VALUES ('user1','{noop}1234','ROLE_ADMIN');

INSERT INTO usuario(username, password, role)
VALUES ('user2','{noop}1234','ROLE_ARBITRO');

INSERT INTO usuario(username, password, role)
VALUES ('user3','{noop}1234','ROLE_ENTRENADOR');

-- Usuario específico para Sebastian Medina
INSERT INTO usuario(username, password, role)
VALUES ('sebastian.medina','{noop}arbitro123','ROLE_ARBITRO');

INSERT INTO arbitro (nombre, contraseña, username, cedula, phone, speciality, scale, photo_data, photo_content_type, photo_filename, unavailability_dates, usuario_id) 
VALUES ('Sebastian Medina', '{noop}arbitro123', 'sebastian.medina', '1058198772', '3222469936', 'Campo', 'Internacional', NULL, NULL, NULL, '', 4);

-- Equipos
INSERT INTO equipo(nombre, estado, ciudad, fundacion,logo)
VALUES ('Lakers',true,'Los Angeles',1947,'https://images.seeklogo.com/logo-png/21/2/los-angeles-lakers-logo-png_seeklogo-216335.png');

INSERT INTO equipo(nombre, estado, ciudad, fundacion,logo)
VALUES ('Chicago Bulls',true, 'Illinois',1996,'https://www.citypng.com/public/uploads/preview/hd-chicago-bulls-vector-logo-png-701751694711025arhvkh69nh.png');

INSERT INTO equipo(nombre, estado, ciudad, fundacion,logo)
VALUES ('Paisitas',false,'Medellin',2025,'https://static.wikia.nocookie.net/logopedia/images/b/b4/Atl%C3%A9tico_Nacional_1.png');

-- Partidos para sebastian medina (usando ID 1 que será el primer árbitro insertado)
INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-05', '15:00', 1, 2, 'PROGRAMADO', 1);

INSERT INTO partido (fecha, hora, equipo_local, equipo_visitante, estado, arbitro_id) 
VALUES ('2025-10-11', '18:00', 1, 3, 'PROGRAMADO', 1);
