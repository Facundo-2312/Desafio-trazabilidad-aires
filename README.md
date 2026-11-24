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
- Código QR descargable en PNG de 1024 × 1024 para abrir directamente cada ficha.
- Impresión del QR desde el navegador, con opción de guardarlo como PDF.
- Acceso por cuentas individuales, roles de administrador y técnico, y gestión de usuarios.
- Respaldo JSON descargable e importación en un inventario vacío desde la sección **Respaldos**.

Los QR contienen el dominio actual de la aplicación; si cambia el dominio, hay que descargar e imprimir los códigos nuevos. No usan un proveedor externo de QR dinámicos.
- Diseño responsive para teléfono y escritorio.

La aplicación inicia escuchando en el puerto 8080. Requiere iniciar sesión para consultar o modificar datos. En el primer inicio, configura `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`; no existen credenciales por defecto. La aplicación no inicia hasta que exista un administrador y la contraseña tenga al menos 12 caracteres. El administrador puede crear cuentas individuales para los técnicos, que comparten el inventario.

## QR desde celular

Al escanear un QR generado mientras se abre `localhost`, la aplicación reemplaza automáticamente `localhost` por la dirección IPv4 de la red local. Accede a `http://IP-DE-TU-COMPUTADORA:8080/equipos` desde el navegador y descarga o escanea el QR nuevamente. El técnico inicia sesión y, al hacerlo, vuelve a la ficha solicitada.

Para una publicación en Internet o una red fija, configura la URL pública en `src/main/resources/application.properties`:

```properties
app.public-base-url=https://tu-dominio.com
```

## Despliegue en Render

El archivo `render.yaml` configura la aplicación y una base PostgreSQL. Render puede crear ambos servicios al conectar este repositorio mediante **New > Blueprint**.

El plan gratuito tiene limitaciones: la aplicación se suspende tras 15 minutos sin tráfico y tarda alrededor de un minuto en reactivarse. La base PostgreSQL gratuita caduca a los 30 días; después hay un plazo de 14 días para pasarla a un plan pago antes de que Render la elimine. No es adecuado para datos que deban conservarse a largo plazo. Descarga periódicamente un respaldo JSON desde **Respaldos** y guárdalo fuera de Render; no hay copias automáticas en este plan.

El servicio no crea usuarios ni contraseñas predeterminados. En Render, define `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` como variables privadas antes del primer arranque. La cookie de sesión se marca como segura en HTTPS; las contraseñas se guardan con BCrypt y los formularios usan protección CSRF.

Los registros locales de H2 no se copian automáticamente a PostgreSQL. El perfil de Render desactiva los datos de ejemplo para permitir importar la base local antes de empezar a usarla. El administrador puede descargar respaldos JSON (hasta 10 MB) y restaurarlos solo cuando el inventario está vacío. Conserva una copia fuera de Render: la base gratuita no ofrece copias automáticas.
