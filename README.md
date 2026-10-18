# Cloudstream Latino

> **Repositorio:** `raiderking2022-wq/Latino-cloud`. La distribución se publica automáticamente solo después de superar las pruebas y la compilación. La reproducción en Android no se verificó; RelampagoMovies continúa experimental por bloqueo Cloudflare.

La [compilación inicial del repositorio nuevo](https://github.com/raiderking2022-wq/Latino-cloud/actions/runs/39141064911) completó las pruebas, generó ambas extensiones y publicó la [lista de distribución](https://raw.githubusercontent.com/raiderking2022-wq/Latino-cloud/builds/plugins.json). Una compilación correcta no garantiza que los servidores de vídeo funcionen en un dispositivo real.

Repositorio de extensiones para Cloudstream con las fuentes solicitadas: [RelampagoMovies](https://relampagomovies.com/) y [Peelink2](https://www.peelink2.com/inicio). El código no aloja películas y solo utiliza enlaces que expone cada sitio; úsalo únicamente con contenido al que tengas derecho de acceso.

## Instalación en Cloudstream

En Ajustes > Extensiones > Añadir repositorio, pega esta dirección:

```text
https://raw.githubusercontent.com/raiderking2022-wq/Latino-cloud/main/repo.json
```

Después instala **Peelink2** y **RelampagoMovies** desde el repositorio. Ambos complementos están marcados como beta; usa una versión de Cloudstream compatible con extensiones beta si tu versión estable no los muestra o no permite instalarlos.

La lista y los archivos `.cs3` se publican en la rama `builds` solo después de una compilación correcta. Si la lista todavía no existe, consulta [GitHub Actions](https://github.com/raiderking2022-wq/Latino-cloud/actions) y espera a que termine o revisa el error; añadir `repo.json` por sí solo no instala los complementos.

## Estado y límites

| Fuente | Implementado | Verificación |
| --- | --- | --- |
| Peelink2 Latino | Portadas, catálogo latino, últimas películas, búsqueda WordPress, ficha y decodificación de opciones `px_repros['latino_N']`; resolución por extractores de Cloudstream. | HTML de catálogo y ficha inspeccionado. Reproducción en Android pendiente. |
| RelampagoMovies | Proveedor experimental con rutas `movie-ID`, páginas `?page=N`, descubrimiento del formulario de búsqueda y lectores genéricos de ficha/enlaces. | El sitio devuelve HTTP 403 y un desafío Cloudflare. No se pudieron verificar selectores, búsqueda ni reproducción. No se presenta como funcional. |

La estructura de Peelink2 se observó en [su catálogo](https://www.peelink2.com/inicio), [la sección latino](https://www.peelink2.com/latino) y [una ficha de muestra](https://www.peelink2.com/2024/10/ver-the-substance-la-sustancia-2024-online.html). Las rutas públicas de RelampagoMovies aparecen en [su catálogo paginado](https://relampagomovies.com/?page=191) y [una ficha indexada](https://www.relampagomovies.com/movie-6606); la estructura HTML del proveedor es provisional, no una integración verificada.

No se prometen servidores compatibles, reproducción de todo el catálogo ni resolución de Cloudflare. No se evaden CAPTCHA, suscripciones, DRM, autenticación ni acortadores publicitarios. Los enlaces a archivos comprimidos de descarga no se tratan como vídeos reproducibles. Los cambios de dominio, HTML o servidor pueden requerir actualizar el código.

Peelink2 prioriza únicamente las opciones etiquetadas `latino`. El catálogo “Últimas películas” y la búsqueda pueden mostrar títulos sin audio latino; en ese caso la extensión informa que no encontró una opción reproducible, en vez de sustituirla silenciosamente por castellano o subtítulos.

## Compilar y actualizar

El proyecto parte de la configuración actual de [recloudstream/extensions](https://github.com/recloudstream/extensions) y de su [plugin Gradle](https://github.com/recloudstream/gradle). Requiere Java 17, Android SDK 35 y conexión para resolver dependencias; las dependencias SNAPSHOT siguen al proyecto upstream y no garantizan compilaciones idénticas en fechas diferentes.

```bash
chmod +x gradlew
./gradlew testDebugUnitTest make makePluginsJson ensureJarCompatibility
```

Cada `push` a `main` con cambios de código ejecuta pruebas, genera `.cs3` y `.jar`, crea `plugins.json` y publica en `builds`. También se puede lanzar manualmente desde Actions > Compilar extensiones > Run workflow. Aumenta el entero `version` en el `build.gradle.kts` de la extensión modificada para distribuir una actualización.

El código se mantiene descomprimido en la raíz del repositorio. No es necesario subir un ZIP desde el celular; el workflow de `.github/workflows/build.yml` compila directamente las carpetas del proyecto.

Las pruebas cubren URLs HTTP válidas, títulos/años, base64 UTF-8, redirecciones de doble base64, selección exclusiva de latino, atributos HTML en mayúsculas y reproductores heredados vacíos. No constituyen una prueba de reproducción real ni acceden a servidores de vídeo.

## Reportar un problema

Abre una incidencia con el nombre de la extensión, versión de Cloudstream, título y URL de la ficha, mensaje exacto de error y si el sitio funciona en el navegador del mismo dispositivo. No publiques cookies, contraseñas, tokens ni capturas con datos personales.
