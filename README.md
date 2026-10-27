# Induzteck App — Plataforma de Protocolos Eléctricos y Control en Terreno

Aplicación móvil desarrollada en **Android Studio** con **Jetpack Compose** y **Kotlin**, orientada a la gestión operativa de inspecciones eléctricas, registro de protocolos en terreno (transformadores, celdas MT, interruptores, relés de protección, cables de poder), consulta de clientes/instalaciones y control de perfiles con avatares multimedia.

---

##  Integrantes
* **Benjamin Andurandegui**
* **Constanza Arriaza**
* **Asignatura:** Desarrollo de Aplicaciones Móviles **Seccion:** 011D

---

## Funcionalidades Implementadas

1. **Interfaz Visual con Material 3:**
   * Diseño moderno, intuitivo y adaptativo con soporte para temas claro y oscuro.
   * Componentes reutilizables y jerarquía visual optimizada para operadores en terreno y clientes.

2. **Formularios Validados:**
   * **Registro de usuarios y Login:** Validación de RUT chileno con dígito verificador (módulo 11), formato de correo electrónico, contraseñas seguras con restricciones estrictas (mayúscula, minúscula, número, símbolo y mínimo 8 caracteres) y campos comerciales para empresas.
   * **Creación de Protocolos:** Validación en tiempo real de condiciones ambientales en terreno (temperaturas entre -10°C y 60°C, humedad entre 0% y 100%) y selección de pruebas compatibles por tipo de equipo.

3. **Navegación Funcional:**
   * Implementada con **Jetpack Navigation Compose**.
   * Flujos protegidos y navegación fluida entre autenticación, panel de inicio, creación de protocolos, historial, borradores, consulta de clientes/instalaciones, detalles de respaldo digital y perfil de usuario.

4. **Gestión de Estado (MVVM):**
   * Arquitectura orientada a eventos y estado unidireccional utilizando **`StateFlow`** y `collectAsStateWithLifecycle()`.
   * ViewModels independientes que encapsulan la lógica de negocio y validación desacoplada de la interfaz gráfica.

5. **Almacenamiento Local (SQLite y SharedPreferences):**
   * Base de datos relacional nativa en SQLite con **16 tablas normalizadas** (`InduztekDbHelper`), datos semilla precargados, restricciones de claves foráneas e integridad transaccional.
   * Persistencia del avatar de usuario en formato **BLOB** dentro de SQLite.
   * Control de sesiones activas mediante `SharedPreferences`.

6. **Uso de Recursos Nativos del Dispositivo:**
   * **Cámara y Galería:** Selección de imágenes de galería o captura directa con la cámara nativa con compresión automática a JPEG (`ConvertidorImagen`) para avatares de perfil.
   * **Geolocalización GPS:** Detección de ubicación en terreno mediante `LocationManager` y `Geocoder` para autocompletar direcciones en instalaciones.
   * **Permisos en tiempo de ejecución:** Gestión segura de permisos para cámara y ubicación con retroalimentación mediante `Snackbar`.

7. **Animaciones:**
   * Transiciones fluidas y alertas visuales animadas con **`AnimatedVisibility`**, `fadeIn()`, `fadeOut()`, `expandVertically()` y `shrinkVertically()`.

---

## Tecnologías y Librerías Utilizadas
* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose & Material 3
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Base de Datos:** SQLite Nativa (`SQLiteOpenHelper`)
* **Navegación:** Jetpack Navigation Compose
* **Control de Versiones:** Git / GitHub

---

## Pasos para Ejecutar el Proyecto

1. **Clonar o descargar el repositorio:**
   Abre tu terminal y clona el repositorio del proyecto en tu equipo.

2. **Abrir en Android Studio:**
   * Inicia **Android Studio**.
   * Selecciona *Open an existing project* y navega hasta la carpeta raíz del proyecto (`induzteck-app`).
   * Espera a que Gradle sincronice las dependencias del proyecto (`Sync Project with Gradle Files`).

3. **Configurar el Dispositivo / Emulador:**
   * Asegúrate de tener un Emulador Android configurado (API 26 o superior recomendado) o conecta un dispositivo físico mediante USB con la *Depuración USB* habilitada.

4. **Ejecutar la Aplicación:**
   * Haz clic en el botón **Run** o presiona `Shift + F10` en Android Studio.
   * La aplicación se compilará y se instalará en el emulador o dispositivo.

5. **Credenciales de Demostración:**
   * **Técnico / Ingeniero:** `tecnico@induztek.cl` / Contraseña: `123456` (o registrar una nueva cuenta).
   * **Cliente:** `cliente@ejemplo.cl` / Contraseña: `123456`.
