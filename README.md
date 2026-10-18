# Cloudstream Latino

Repositorio de Cloudstream dedicado a [Peelink2](https://www.peelink2.com/inicio). El catálogo se consulta por páginas y no se limita a las películas con audio latino; el reproductor conserva la prioridad por las opciones latinas.

## Instalar o actualizar

En Ajustes > Extensiones > Añadir repositorio, utiliza:

```text
https://raw.githubusercontent.com/raiderking2022-wq/Latino-cloud/main/repo.json
```

Después instala o actualiza **Peelink2**. La versión 3 corrige la búsqueda, añade navegación por catálogo y categorías, y mantiene el mismo identificador para que la aplicación pueda actualizar la instalación existente.

La extensión está marcada como beta. Usa una versión de Cloudstream compatible con extensiones beta si tu versión estable no la muestra o no permite instalarla.

## Catálogo y búsqueda

- **Todo el catálogo:** navega por [las páginas del sitio](https://www.peelink2.com/inicio), incluidas las siguientes páginas; no se descarga todo de una vez en el teléfono.
- **Destacadas:** separa las recomendaciones de la primera página para no repetirlas dentro del listado general.
- **Películas en latino:** mantiene la sección específica y su paginación.
- **Géneros:** añade Acción, Ciencia ficción, Drama, Comedia, Infantil y Terror. Las páginas de género usan el botón de carga adicional publicado por el sitio.
- **Búsqueda:** lee las portadas de los resultados y deja de tomar el encabezado genérico de la página como película. Si no hay resultados, devuelve una lista vacía.
- **Variantes de nombres:** `spiderman` y `Spider-Man` se consultan también como `spider man`, que es la forma que devuelve resultados en la web.

El fallo se reprodujo con [la búsqueda original](https://www.peelink2.com/?s=spiderman), que no devolvía películas, y se contrastó con [spider man](https://www.peelink2.com/?s=spider+man) y [batman](https://www.peelink2.com/?s=batman). La estructura de catálogo y las páginas siguientes se revisaron en [inicio](https://www.peelink2.com/inicio), [página 2](https://www.peelink2.com/inicio/page/2), [latino, página 2](https://www.peelink2.com/latino/page/2) y [Acción](https://www.peelink2.com/2012/12/pagina-accion.html).

La disponibilidad de una película en el catálogo no garantiza que tenga audio latino o un servidor compatible. No se modifica silenciosamente el idioma elegido ni se garantiza reproducción de todo el sitio.

## Compilación automática

Cada cambio de código en `main` ejecuta pruebas unitarias, genera `.cs3` y `.jar`, comprueba compatibilidad JAR y publica una lista con una sola extensión en `builds`. Los archivos de distribuciones retiradas se eliminan de esa rama al publicar; el historial Git se conserva.

La [lista de distribución](https://raw.githubusercontent.com/raiderking2022-wq/Latino-cloud/builds/plugins.json) solo cambia después de una compilación correcta. Consulta [GitHub Actions](https://github.com/raiderking2022-wq/Latino-cloud/actions) para comprobar el resultado.

```bash
chmod +x gradlew
./gradlew testDebugUnitTest make makePluginsJson ensureJarCompatibility
```

El proyecto usa Java 17, Android SDK 35, el [plugin Gradle de Cloudstream](https://github.com/recloudstream/gradle) y configuración adaptada de [recloudstream/extensions](https://github.com/recloudstream/extensions). Las dependencias SNAPSHOT pueden cambiar con el tiempo.

Las pruebas cubren los resultados vacíos, el encabezado genérico, los enlaces anidados de las búsquedas, portadas sin título, duplicados, selección del catálogo, paginación, configuración de categorías y selección de reproductores latinos. No reemplazan una prueba de reproducción real en Android.

## Límites y uso

Este repositorio no aloja películas. Úsalo únicamente con contenido al que tengas derecho de acceso; no se evaden CAPTCHA, suscripciones, DRM, autenticación ni acortadores publicitarios.

Para reportar un problema, indica la versión de la extensión, el texto buscado o la URL de la ficha y el mensaje de error. No publiques cookies, contraseñas, tokens ni capturas con datos personales.
