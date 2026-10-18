# Entrega adaptada a Latino-cloud

El destino del proyecto es https://github.com/raiderking2022-wq/Latino-cloud. Se actualizaron el manifiesto `repo.json`, el repositorio por defecto de Gradle y las instrucciones; se conservan los avisos de autoría y licencia del código original.

## Estado de verificación

- **Compilación nueva completada:** la [ejecución del 18 de octubre de 2026](https://github.com/raiderking2022-wq/Latino-cloud/actions/runs/39141064911) superó las pruebas, compiló ambos proveedores, comprobó la compatibilidad JAR y publicó la distribución en `builds`.
- **Corrección incluida y compilada:** el alias `uqload.vc` usa `override var mainUrl`, acorde con la API distribuida. La integración ya pasó la compilación en este repositorio.
- **Peelink2:** HTML de catálogo y ficha inspeccionado; reproducción en Android sin verificar.
- **RelampagoMovies:** experimental, con selectores y reproducción sin verificar por bloqueo Cloudflare.
- **Instalación por enlace publicada:** el manifiesto es https://raw.githubusercontent.com/raiderking2022-wq/Latino-cloud/main/repo.json y la lista de complementos es https://raw.githubusercontent.com/raiderking2022-wq/Latino-cloud/builds/plugins.json.
- **Advertencias no bloqueantes:** D8 informó de metadatos Kotlin más recientes que su versión de lectura, aunque la compilación terminó correctamente. No se presenta esto como una prueba de carga o reproducción real en Android.

## Publicación directa

El repositorio contiene el código descomprimido y el workflow en `.github/workflows/build.yml`. No es necesario subir manualmente los paquetes preparados para la cuenta anterior.

No se necesita introducir tokens, contraseñas ni cookies en el código; el workflow usa el token temporal de GitHub Actions. La distribución queda en `builds` después de superar las pruebas y la compilación.
