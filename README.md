# Sistema de Gestión Deportiva CABA

Sistema integral de gestión deportiva para la administración de torneos, equipos, árbitros y entrenadores con funcionalidades avanzadas de seguimiento y reportería.

## Descripción del Proyecto

Aplicación web robusta desarrollada con **Spring Boot** y **Thymeleaf** que permite la gestión completa de actividades deportivas. El sistema ofrece herramientas para la asignación inteligente de árbitros, organización de torneos, registro de equipos, seguimiento de entrenadores, y monitoreo de condiciones climáticas para partidos.

## Características Principales

### Gestión Administrativa
- **Panel de Administración Centralizado**: Control total del sistema desde una interfaz unificada
- **Sistema de Autenticación y Autorización**: Gestión segura de usuarios con Spring Security
- **Dashboard de Estadísticas**: Visualización de métricas clave del sistema

### Funcionalidades Deportivas
- **Gestión de Árbitros**: Registro completo, asignación automatizada y seguimiento de árbitros
- **Administración de Equipos**: Control detallado de equipos participantes y sus integrantes
- **Organización de Torneos**: Creación, configuración y gestión de competencias deportivas
- **Control de Entrenadores**: Registro, perfil y seguimiento de entrenadores
- **Gestión de Partidos**: Programación, asignación y seguimiento de encuentros deportivos

### Características Avanzadas
- **Integración Climática**: Información meteorológica en tiempo real para partidos mediante la API de [Open-Meteo](https://open-meteo.com/)
  - Temperatura y condiciones climáticas para partidos futuros
  - Historial meteorológico de partidos pasados
  - Códigos WMO para condiciones climáticas detalladas
- **Sistema de Reportes**: Generación de estadísticas y reportes personalizados
- **API RESTful**: Servicio de API complementario para integraciones externas

## Tecnologías Utilizadas

### Backend
- **Framework**: Spring Boot 3.x
- **Template Engine**: Thymeleaf
- **ORM**: Spring Data JPA
- **Base de Datos**: H2 (desarrollo)
- **Seguridad**: Spring Security
- **Build Tool**: Maven

### Frontend
- **Framework CSS**: Bootstrap 5
- **Lenguajes**: HTML5, CSS3, JavaScript
- **Diseño Responsivo**: Mobile-first approach

### Servicios Externos
- **API Climática**: Open-Meteo API
- **Infraestructura**: AWS EC2
- **Contenedores**: Docker

##  Instalación y Configuración

### Prerrequisitos

```
- Java 17 o superior
- Maven 3.6+
- Git
- Docker (para despliegue)
```

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/caba-pro.git
cd caba-pro
```

2. **Compilar el proyecto**
```bash
mvn clean install
```

3. **Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

4. **Acceder a la aplicación**
   - URL: `http://localhost:8080`
   - Consola H2: `http://localhost:8080/h2-console`

## Despliegue

### Entornos de Producción

#### Aplicación Principal (Spring Boot)
- **URL**: http://44.210.108.213:8080
- **Docker Hub**: `niosto602/spring-arbitros`
- **Plataforma**: AWS EC2

#### API REST (Express.js)
- **URL**: http://44.210.108.213:3000
- **Docker Hub**: `niosto602/arbitros-api`
- **Repositorio**: [Arbitros-API-REST](https://github.com/niosto/Arbitros-API-REST)

### Despliegue con Docker

```bash
# Descargar imagen
docker pull niosto602/spring-arbitros

# Ejecutar contenedor
docker run -p 8080:8080 niosto602/spring-arbitros
```

## Equipo de Desarrollo
- Nicolás Ospina | [@niosto](https://github.com/niosto)
- Sebastián Medina | [@Sebaxdenc](https://github.com/Sebaxdenc) 
- Sara López | [@slopma](https://github.com/slopma) 
- Paula Llanos | [@kirbchy](https://github.com/kirbchy) 
