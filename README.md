# CRM Inmobiliario - Aplicación Dockerizada

Este proyecto es un CRM para el sector inmobiliario desarrollado con Spring Boot y MySQL.

## Requisitos

- Docker
- Docker Compose

## Estructura de Dockerización

- **Dockerfile**: Configura la construcción de la aplicación Spring Boot
- **docker-compose.yml**: Configura los servicios de la aplicación y la base de datos MySQL
- **wait-for-it.sh**: Script para asegurar que la aplicación espere a que la base de datos esté disponible

## Instrucciones de Uso

### Iniciar la aplicación

```bash
# Navegar al directorio del proyecto
cd c:\Users\Nicolas\OneDrive\Escritorio\crm3\crmInmobiliario

# Construir e iniciar los contenedores
docker-compose up -d
```

### Detener la aplicación

```bash
docker-compose down
```

### Ver los logs de la aplicación

```bash
docker-compose logs -f app
```

## Configuración

La aplicación utiliza las siguientes variables de entorno:

- **SPRING_DATASOURCE_URL**: URL de conexión a la base de datos MySQL
- **SPRING_DATASOURCE_USERNAME**: Usuario de MySQL
- **SPRING_DATASOURCE_PASSWORD**: Contraseña de MySQL

Estas variables están configuradas en el archivo docker-compose.yml.

## Acceso a la aplicación

Una vez iniciada, la aplicación estará disponible en:

- **URL**: http://localhost:8080

## Acceso a la base de datos

MySQL estará disponible en:

- **Host**: localhost
- **Puerto**: 3306
- **Nombre de la BD**: crm_inmobiliario
- **Usuario**: root
- **Contraseña**: password
