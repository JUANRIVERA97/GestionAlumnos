# Gestión de Alumnos — App Android

Evidencia **GA8-220501096-AA2-EV02** · APK (desarrollar módulos móviles según requerimientos del proyecto)
SENA · Tecnólogo en Análisis y Desarrollo de Software · Ficha 3186670

Aplicación Android para un instituto que permite **iniciar sesión**, **registrar usuarios** y **registrar alumnos**, usando **Firebase**.

## Pantallas (módulos)

| Pantalla | Archivo Java | Diseño (XML) | Función |
|---|---|---|---|
| Inicio de sesión | `MainActivity.java` | `activity_main.xml` | Valida correo y contraseña con Firebase Authentication. Muestra *Ingreso correcto* o *Ingreso fallido*. |
| Registro de usuario | `RegisterActivity.java` | `activity_register.xml` | Crea una cuenta nueva (correo, contraseña y confirmación). Muestra *Registro exitoso*. |
| Inicio – Alumnos | `HomeActivity.java` | `activity_home.xml`, `item_alumno.xml` | Formulario para registrar alumnos (nombre, documento, programa, semestre), listado en tiempo real y opción de eliminar. Guarda en Cloud Firestore. |

## Tecnologías

- Android Studio · Java · diseño en XML con Material Design 3
- Firebase Authentication (correo y contraseña)
- Cloud Firestore (colección `alumnos`)

## Cómo ejecutarlo

1. Crear un proyecto en [Firebase](https://console.firebase.google.com) y agregar una app Android con el paquete `com.sena.gestionalumnos`.
2. Descargar `google-services.json` y copiarlo en la carpeta `app/`.
3. En Firebase habilitar **Authentication → Correo electrónico/contraseña** y crear la base de datos **Cloud Firestore**.
4. Abrir el proyecto en Android Studio y ejecutar ▶ en un emulador o celular.

## Autor(a)

**[Tu nombre completo]** — Ficha 3186670
