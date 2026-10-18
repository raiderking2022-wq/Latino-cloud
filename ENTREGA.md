# Entrega adaptada a Latino-cloud

El destino del proyecto es https://github.com/raiderking2022-wq/Latino-cloud. Se actualizaron el manifiesto `repo.json`, el repositorio por defecto de Gradle y las instrucciones; se conservan los avisos de autoría y licencia del código original.

## Estado de verificación

- **Base compilada anteriormente:** las pruebas y la compilación de ambos proveedores pasaron antes de esta adaptación. Eso no sustituye una compilación del paquete actualizado.
- **Corrección incluida:** el alias `uqload.vc` usa `override var mainUrl`, acorde con la API distribuida observada durante la compilación anterior. Debe verificarse al ejecutar el flujo nuevo.
- **Peelink2:** HTML de catálogo y ficha inspeccionado; reproducción en Android sin verificar.
- **RelampagoMovies:** experimental, con selectores y reproducción sin verificar por bloqueo Cloudflare.
- **Instalación por enlace:** no estará disponible hasta subir los archivos y completar correctamente la compilación de GitHub Actions en el repositorio nuevo.

## Publicación directa

El repositorio contiene el código descomprimido y el workflow en `.github/workflows/build.yml`. No es necesario subir manualmente los paquetes preparados para la cuenta anterior.

No se necesita introducir tokens, contraseñas ni cookies en el código; el workflow usa el token temporal de GitHub Actions. La distribución queda en `builds` después de superar las pruebas y la compilación.
