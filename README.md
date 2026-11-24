# ClimaTrack — Trazabilidad de Aires

Aplicación web en Java para inventariar aires acondicionados, consultar su ficha desde un código QR y registrar el historial de mantenimiento.

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior

## Ejecutar

Desde esta carpeta:

```powershell
mvn spring-boot:run
```

Abre [http://localhost:8080](http://localhost:8080). La primera ejecución carga dos equipos y algunos mantenimientos de ejemplo. Los datos se guardan en `data/trazabilidad.mv.db` y se conservan al reiniciar.

También se puede crear un JAR ejecutable con `mvn package` y ejecutarlo mediante `java -jar target/trazabilidad-aires-1.0.0.jar`.

## Funcionalidades

- Panel con inventario, cantidad de mantenimientos y actividad reciente.
- Búsqueda por ubicación, marca o número de serie.
- Alta y edición de equipos con datos técnicos, instalación y garantía.
- Ficha de equipo con historial de mantenimiento y formulario para registrar visitas.
- Código QR descargable en PNG para abrir directamente cada ficha.
- Diseño responsive para teléfono y escritorio.

La aplicación inicia escuchando en el puerto 8080 y no incluye autenticación. Para acceder desde celulares, despliega la aplicación en una red privada y configura el acceso seguro antes de exponerla a Internet; los QR apuntan al origen desde el que se solicitan.

## QR desde celular

Al escanear un QR generado mientras se abre `localhost`, la aplicación reemplaza automáticamente `localhost` por la dirección IPv4 de la red local. Accede a `http://IP-DE-TU-COMPUTADORA:8080/equipos` desde el navegador y descarga o escanea el QR nuevamente.

Para una publicación en Internet o una red fija, configura la URL pública en `src/main/resources/application.properties`:

```properties
app.public-base-url=https://tu-dominio.com
```

## Despliegue en Render

El archivo `render.yaml` configura la aplicación y una base PostgreSQL. Render puede crear ambos servicios al conectar este repositorio mediante **New > Blueprint**.

El plan gratuito tiene limitaciones: la aplicación se suspende tras 15 minutos sin tráfico y tarda alrededor de un minuto en reactivarse. La base PostgreSQL gratuita caduca a los 30 días; después hay un plazo de 14 días para pasarla a un plan pago antes de que Render la elimine. No es adecuado para datos que deban conservarse a largo plazo.

La aplicación no incluye autenticación. Con esta configuración, cualquier persona que encuentre la URL pública puede consultar y modificar equipos y mantenimientos. Publica solamente datos que estés dispuesto a hacer públicos.

Los registros locales de H2 no se copian automáticamente a PostgreSQL. El perfil de Render desactiva los datos de ejemplo para permitir importar la base local antes de empezar a usarla. Conserva una copia local independiente: la base gratuita de Render no ofrece copias de seguridad.
