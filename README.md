# Azeris — Proyecto Final de Curso

## Descripción

Este proyecto es un videojuego **de explorador de mazmorras rougelite** llamado **Azeris**. Esta desarrollado en **Java** utilizando el framework **LibGDX** dentro del entorno de desarrollo **IntelliJ IDEA**.

Azeris combina mecánicas clásicas de exploración de mazmorras, combate y progresión del jugador. Además del videojuego, el proyecto incluye integración con servicios web modernos para la recopilación y visualización de datos.

Los datos generados durante las partidas se almacenan en **Firebase**, permitiendo posteriormente mostrarlos en una página web desplegada en **Vercel**, desde donde también es posible descargar e instalar el juego.

---

## Características principales

- Exploración de mazmorras en vista top-down
- Sistema de combate en tiempo real
- Recolección y almacenamiento de estadísticas
- Integración con Firebase
- Página web conectada al backend del juego
- Descarga e instalación del juego desde la web
- Arquitectura modular utilizando LibGDX

---

## Tecnologías utilizadas

### Desarrollo del videojuego
- **Java**
- **LibGDX**
- **IntelliJ IDEA**
- **Gradle**

### Backend y almacenamiento
- **Firebase**
    - Firestore / Realtime Database

### Desarrollo web
- **Vue y Ionic**
- **Vercel** para despliegue web

---

## Integración con Firebase

El proyecto utiliza Firebase para almacenar información relacionada con las partidas y estadísticas de los jugadores. Esto permite:

- Registrar datos de juego
- Mostrar estadísticas en tiempo real a traves de la pagina web oficial
- Centralizar información en la nube
- Conectar el videojuego con la plataforma web

---

## Plataforma web

La página web asociada al proyecto está desplegada en Vercel y permite:

- Visualizar datos recopilados desde Firebase
- Mostrar estadísticas del videojuego
- Descargar e instalar el juego
- Servir como plataforma de presentación del proyecto

---

## Público objetivo

Este proyecto está dirigido principalmente a:

- El tribunal del proyecto
- Estudiantes interesados en desarrollo de videojuegos
- Personas interesadas en Java y LibGDX
- Usuarios que disfrutan de juegos dungeon crawler clásicos

---

## Cómo ejecutar el proyecto

### Instalación

- Windows

Desde la página oficial, descarga el archivo AzerisWindowsVer subido a Itch.io. Descomprímelo y ejecuta el archivo Azeris.exe.

- Ubuntu

Desde la página oficial, descarga AzerisUbuntuVer subido a Itch.io. Descomprímelo y abre la terminal. Dirígete a la carpeta /bin de AzerisUbuntuVer y ejecuta el siguiente comando:

````
sudo chmod +x AzerisUbuntuVer
````

Por defecto, Ubuntu no deja ejecutar el archivo a menos que se le otorguen los permisos explícitos de ejecución Una vez realizado el comando, ejecuta el archivo AzerisUbuntuVer con doble clic.
