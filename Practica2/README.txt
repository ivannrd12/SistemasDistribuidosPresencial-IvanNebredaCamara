# README - Práctica Obligatoria 2

## Descripción

Aplicación web que integra:
- Frontend y backend usando Spring Boot, JPA, Hibernate y Thymeleaf.
- API de terceros desarrollada en Flask (Python) para simular excepciones.
- Tratamiento amigable de errores.

## Tecnologías utilizadas

- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Thymeleaf
- MySQL (Docker)
- Python Flask

## Requisitos

- Docker + Docker Compose instalado.
- Java 17 o superior.
- Python 3 instalado.
- Pip instalado para gestionar dependencias de Python.

## Instrucciones de uso

### 1. Levantar la base de datos MySQL

Desde el directorio donde esté el archivo docker-compose.yml:
```bash
docker-compose up -d
```

- Usuario MySQL: root
- Contraseña: 123456
- Base de datos: practica2

### 2. Iniciar el servidor Flask

Desde la carpeta /api-flask/:
```bash
pip install flask requests
python app.py
```

- Servidor Flask en localhost:5000

### 3. Iniciar la aplicación Spring Boot

Abrir el proyecto en IntelliJ IDEA:
- Ejecutar `Run 'SpringfrontendApplication'`
- Acceso en `http://localhost:8080/`

### 4. Credenciales para acceder

- Usuario: admin
- Contraseña: 1234

## Navegación de la aplicación

| Ruta | Descripción |
|------|-------------|
| `/` | Página principal de presentación |
| `/login` | Login de usuarios |
| `/api-invocaciones` | Pantalla para simular invocaciones a APIs de Flask |
| `/invocar/archivo` | Simula error de archivo |
| `/invocar/basedatos` | Simula error de base de datos |
| `/invocar/pokemon` | Simula error de API externa |

## Tratamiento de excepciones

- Se capturan errores de acceso a archivos, base de datos y APIs externas.
- Se muestran mensajes amigables en la web:
  - Error accediendo a un archivo.
  - Error accediendo a la base de datos.
  - Error llamando a API externa.
  - Error de conexión general.

## Notas finales

- La base de datos MySQL es usada solo por Spring Boot.
- Flask corre en servidor independiente y simula excepciones.
- La aplicación muestra un comportamiento profesional en el manejo de errores.


