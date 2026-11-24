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
