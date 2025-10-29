# API REST - CABA PRO

La aplicación ahora cuenta con una API REST completa que permite el consumo desde aplicaciones Node.js y otros clientes.

## Base URL
```
http://localhost:8080/api
```

## Configuración CORS
La API está configurada para permitir peticiones desde:
- `http://localhost:3000` (Node.js/React/Angular)
- `http://localhost:3001`
- `http://localhost:4200`

## Endpoints Disponibles

### Árbitros (`/api/arbitros`)

#### GET /api/arbitros
Obtener todos los árbitros
```bash
curl -X GET http://localhost:8080/api/arbitros
```

#### GET /api/arbitros/{id}
Obtener árbitro por ID
```bash
curl -X GET http://localhost:8080/api/arbitros/1
```

#### POST /api/arbitros
Crear nuevo árbitro
```bash
curl -X POST http://localhost:8080/api/arbitros \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "cedula": "12345678",
    "phone": "555-1234",
    "username": "juanperez",
    "contraseña": "{noop}password123",
    "speciality": "Fútbol profesional"
  }'
```

#### PUT /api/arbitros/{id}
Actualizar árbitro existente
```bash
curl -X PUT http://localhost:8080/api/arbitros/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez Actualizado",
    "email": "juan.perez@email.com",
    "cedula": "12345678",
    "phone": "555-1234",
    "username": "juanperez",
    "contraseña": "password123",
    "speciality": "Fútbol profesional"
  }'
```

#### DELETE /api/arbitros/{id}
Eliminar árbitro
```bash
curl -X DELETE http://localhost:8080/api/arbitros/1
```

#### GET /api/arbitros/search?username={username}
Buscar árbitro por username
```bash
curl -X GET "http://localhost:8080/api/arbitros/search?username=juanperez"
```

#### GET /api/arbitros/cedula/{cedula}
Buscar árbitro por cédula
```bash
curl -X GET "http://localhost:8080/api/arbitros/cedula/12345678"
```

#### GET /api/arbitros/{id}/photo
Obtener foto del árbitro
```bash
curl -X GET http://localhost:8080/api/arbitros/1/photo
```

### Entrenadores (`/api/entrenadores`)

#### GET /api/entrenadores
Obtener todos los entrenadores

#### POST /api/entrenadores
Crear nuevo entrenador
```bash
curl -X POST http://localhost:8080/api/entrenadores \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCompleto": "Carlos García",
    "email": "carlos.garcia@email.com",
    "cedula": "87654321",
    "telefono": "555-5678",
    "equipo": "Atlético Nacional",
    "categoria": "PROFESIONAL",
    "experiencia": 5
  }'
```

### Equipos (`/api/equipos`)

#### GET /api/equipos
Obtener todos los equipos

#### POST /api/equipos
Crear nuevo equipo
```bash
curl -X POST http://localhost:8080/api/equipos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Real Madrid",
    "ciudad": "Madrid",
    "fundacion": "1902-03-06"
  }'
```

### Partidos (`/api/partidos`)

#### GET /api/partidos
Obtener todos los partidos

#### POST /api/partidos
Crear nuevo partido
```bash
curl -X POST http://localhost:8080/api/partidos \
  -H "Content-Type: application/json" \
  -d '{
    "fecha": "2024-12-20",
    "hora": "15:30",
    "equipoLocal": {"id": 1},
    "equipoVisitante": {"id": 2},
    "estado": "PROGRAMADO"
  }'
```

#### GET /api/partidos/estado/{estado}
Obtener partidos por estado
```bash
curl -X GET http://localhost:8080/api/partidos/estado/PROGRAMADO
```

#### GET /api/partidos/sin-arbitro
Obtener partidos sin árbitro asignado
```bash
curl -X GET http://localhost:8080/api/partidos/sin-arbitro
```

## Ejemplo de uso en Node.js

### Instalación de dependencias
```bash
npm install axios
```

### Código de ejemplo (ejemplo-api.js)
```javascript
const axios = require('axios');

const API_BASE_URL = 'http://localhost:8080/api';

class CABAProAPI {
  
  // Árbitros
  async obtenerArbitros() {
    try {
      const response = await axios.get(`${API_BASE_URL}/arbitros`);
      return response.data;
    } catch (error) {
      console.error('Error al obtener árbitros:', error);
      throw error;
    }
  }

  async crearArbitro(arbitroData) {
    try {
      const response = await axios.post(`${API_BASE_URL}/arbitros`, arbitroData, {
        headers: { 'Content-Type': 'application/json' }
      });
      return response.data;
    } catch (error) {
      console.error('Error al crear árbitro:', error);
      throw error;
    }
  }

  async obtenerArbitroPorId(id) {
    try {
      const response = await axios.get(`${API_BASE_URL}/arbitros/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error al obtener árbitro:', error);
      throw error;
    }
  }

  // Partidos
  async obtenerPartidos() {
    try {
      const response = await axios.get(`${API_BASE_URL}/partidos`);
      return response.data;
    } catch (error) {
      console.error('Error al obtener partidos:', error);
      throw error;
    }
  }

  async crearPartido(partidoData) {
    try {
      const response = await axios.post(`${API_BASE_URL}/partidos`, partidoData, {
        headers: { 'Content-Type': 'application/json' }
      });
      return response.data;
    } catch (error) {
      console.error('Error al crear partido:', error);
      throw error;
    }
  }

  // Equipos
  async obtenerEquipos() {
    try {
      const response = await axios.get(`${API_BASE_URL}/equipos`);
      return response.data;
    } catch (error) {
      console.error('Error al obtener equipos:', error);
      throw error;
    }
  }
}

// Ejemplo de uso
async function ejemploUso() {
  const api = new CABAProAPI();

  try {
    // Obtener todos los árbitros
    console.log('=== ÁRBITROS ===');
    const arbitros = await api.obtenerArbitros();
    console.log('Árbitros disponibles:', arbitros.length);
    
    // Obtener todos los equipos
    console.log('\n=== EQUIPOS ===');
    const equipos = await api.obtenerEquipos();
    console.log('Equipos disponibles:', equipos.length);
    
    // Obtener todos los partidos
    console.log('\n=== PARTIDOS ===');
    const partidos = await api.obtenerPartidos();
    console.log('Partidos programados:', partidos.length);
    
    // Crear un nuevo árbitro
    console.log('\n=== CREAR ÁRBITRO ===');
    const nuevoArbitro = {
      nombre: "API Test Árbitro",
      cedula: "999999999",
      phone: "555-9999",
      username: "apitest",
      contraseña: "{noop}password123",
      speciality: "API Testing"
    };
    
    const arbitroCreado = await api.crearArbitro(nuevoArbitro);
    console.log('Árbitro creado:', arbitroCreado);
    
  } catch (error) {
    console.error('Error en ejemplo:', error.response?.data || error.message);
  }
}

// Ejecutar ejemplo
if (require.main === module) {
  ejemploUso();
}

module.exports = CABAProAPI;
```

### Ejecutar el ejemplo
```bash
node ejemplo-api.js
```

## Manejo de errores

La API retorna códigos HTTP estándar:
- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Datos de entrada inválidos
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error interno del servidor

## Formato de respuesta de error
```json
{
  "error": "Descripción del error",
  "status": 400,
  "timestamp": "2024-12-15T10:30:00"
}
```

## Consideraciones importantes

1. **CORS**: Ya está configurado para desarrollo local
2. **Autenticación**: Los endpoints API están configurados sin autenticación para facilitar el consumo externo
3. **Validación**: Los endpoints validan los datos de entrada y retornan errores apropiados
4. **Serialización**: Las entidades JPA están configuradas con Jackson para evitar referencias circulares

## Pruebas con Postman o Thunder Client

Puedes importar esta colección básica:
```json
{
  "name": "CABA PRO API",
  "requests": [
    {
      "name": "Get Arbitros",
      "method": "GET",
      "url": "http://localhost:8080/api/arbitros"
    },
    {
      "name": "Get Partidos",
      "method": "GET", 
      "url": "http://localhost:8080/api/partidos"
    },
    {
      "name": "Get Equipos",
      "method": "GET",
      "url": "http://localhost:8080/api/equipos"
    }
  ]
}
```
