# Viaje Cusco · Android privado

App Android con web integrada, Firebase y logo propio. No necesita un servidor en tu computadora. Internet permite compartir los datos; cada instalación usa acceso anónimo y debe autorizarse en Firestore.

## Subir a GitHub

Sube **el contenido de esta carpeta** a la raíz de `06domenica06-star/viaje-cusco-app`. No subas el ZIP como un único archivo.

La carpeta `.github` debe estar incluida. Si el navegador la omite, crea el archivo `.github/workflows/compilar-apk.yml` desde **Add file > Create new file** y pega su contenido. En la raíz deben verse `app`, `web`, `scripts`, `build.gradle` y `settings.gradle`.

## Configurar la firma (una vez)

La firma se entrega en un ZIP privado separado. **Nunca subas ese ZIP, el archivo .p12 ni los TXT de secretos al repositorio**.

En el repositorio abre **Settings > Secrets and variables > Actions > New repository secret**:

| Nombre del secreto | Contenido que debes pegar |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Todo el texto del archivo del mismo nombre `.txt` de tu paquete de firma |
| `ANDROID_KEYSTORE_PASSWORD` | Todo el texto de su archivo `.txt` |

El alias se mantiene como `viaje-cusco`. Conserva el archivo de firma y su contraseña como respaldo privado. La misma firma es necesaria para todas las actualizaciones.

## Compilar en GitHub

1. Entra en **Actions > Compilar APK > Run workflow**.
2. Primera versión: nombre **1.0**, código **1**.
3. Espera el resultado. Al terminar, entra en la ejecución y descarga el artefacto `Viaje-Cusco-APK-1.0-1`.
4. Extrae el ZIP del artefacto. Dentro está `Viaje-Cusco.apk`.
5. Instala el mismo APK en ambos Android (Android 8 o posterior y Android System WebView actualizado).
6. En cada app abre **Viaje compartido > Autorizar este dispositivo**, copia el código y crea su documento `allowedDevices/CODIGO` con `enabled` boolean `true` en Firebase.
7. Pulsa **Reintentar conexión**. Comprueba agregar y editar un gasto entre ambos celulares. El código del navegador de la computadora es distinto: puedes retirar su autorización tras las pruebas para dejar solo los dos celulares.

No hace falta `google-services.json`: esta app usa la configuración Firebase de la web integrada. Las reglas privadas ya publicadas siguen vigentes. Las operaciones quedan prohibidas a dispositivos no autorizados.

## Actualizar a 1.1

Modifica los archivos de `web/` y súbelos a GitHub. Ejecuta otra compilación con nombre **1.1**, código **2** (después **1.2**, código **3**, etc.). Instala encima de la versión anterior, sin desinstalar. Conserva aplicación `pe.domenica.viajecusco` y los dos secretos de firma. Los datos de Firebase no se borran al compilar ni al actualizar. Los cambios de contenido web necesitan una nueva APK con este diseño; no hay descarga automática de código.

## Datos y respaldo

Si tienes datos locales en una web anterior, expórtalos antes de cambiar de versión. Al conectar un segundo dispositivo, adopta los datos compartidos y conserva respaldo local previo. JSON e ICS se exportan con el selector de archivos Android; importar JSON permite elegir un archivo del teléfono. Los enlaces de reservas y mapas se abren en el navegador.

Las ediciones de distintos registros se combinan. Si ambos editan el mismo campo, gana la última escritura. Las notificaciones de recordatorios requieren la app abierta; no se ha añadido programación nativa en segundo plano.

## Validación

Se verificaron la sintaxis JavaScript, los archivos, las versiones y el XML Android. La compilación local no pudo descargar dependencias por restricciones de red del entorno; no se entrega una APK ya compilada. El workflow comprueba compilación, lint y firma en GitHub. Hasta que termine correctamente no se confirma la APK. Hace falta probar la sincronización en ambos celulares autorizados.
