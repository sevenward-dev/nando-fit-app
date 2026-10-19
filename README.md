<div align="center">

  <img src="app/src/main/res/drawable/logo_nandofitplus.png" alt="NandoFit+ Logo" width="130" />

  # NandoFit+ ⚡
  **Aplicación de Entrenamiento de Alta Intensidad y Sobrecarga Progresiva**

  *Diseñada para optimizar cada repetición, controlar los tiempos de descanso y monitorear el progreso real en el gimnasio.*

  <p align="center">
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
    <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose" /></a>
    <a href="https://firebase.google.com"><img src="https://img.shields.io/badge/Firebase-Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase Firestore" /></a>
    <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Min%20SDK-24%20%7C%20Target%2034-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android SDK" /></a>
    <img src="https://img.shields.io/badge/Theme-Dark%20%26%20Neon%20Green-42D60F?style=for-the-badge" alt="Neon Theme" />
  </p>

</div>

---

## 📌 Tabla de Contenidos
- [Descripción General](#-descripción-general)
- [Características Principales](#-características-principales)
- [Arquitectura y Stack Tecnológico](#-arquitectura-y-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Rutina de Entrenamiento Preconfigurada](#-rutina-de-entrenamiento-preconfigurada)
- [Requisitos y Configuración](#-requisitos-y-configuración)
- [Compilación y Ejecución](#-compilación-y-ejecución)
- [Diseño e Identidad Visual](#-diseño-e-identidad-visual)
- [Autor](#-autor)

---

## 📖 Descripción General

**NandoFit+** es una aplicación nativa para Android construida con **Kotlin** y **Jetpack Compose**, orientada a deportistas y atletas que buscan aplicar con precisión el principio de **sobrecarga progresiva**.

A diferencia de los anotadores tradicionales, NandoFit+ proporciona una experiencia en vivo durante el entrenamiento: guía las series efectivas, calibra el nivel de esfuerzo (RPE / Fallo muscular), ejecuta un temporizador inteligente en primer plano (*Foreground Service*) y registra el tonelaje total acumulado (volumen en kg) sincronizado en la nube con soporte offline.

---

## ✨ Características Principales

- 🏋️ **Gestión de Sesión Activa**:
  - Visualización del ejercicio actual con notas técnicas, pines de ajuste de máquinas y rango objetivo de repeticiones.
  - Registro dinámico serie por serie: peso (`kg`), repeticiones (`reps`) y nivel de esfuerzo (`Fácil`, `Óptimo 80-85%`, `Fallo`).
  - Comparativa inmediata con la última carga registrada para garantizar la sobrecarga progresiva.

- ⏱️ **Temporizador de Descanso en Segundo Plano (`Foreground Service`)**:
  - Cuenta regresiva precisa entre series y transiciones entre bloques de ejercicios.
  - Notificación persistente con tiempo restante interactivo para salir de la app sin perder la cuenta del descanso.

- 📊 **Métricas y Resumen de Sesión**:
  - Cálculo instantáneo del **Volumen Total Levantado** (`kg = peso × reps`).
  - Tiempo total bajo tensión y duración de la sesión.
  - Pantalla de progreso histórico con registro de récords personales.

- 📶 **Offline-First con Firebase Firestore**:
  - Almacenamiento en caché persistente local: registra tus series sin conexión dentro del gimnasio y sincroniza automáticamente al recuperar señal.
  - Poblado inicial automatizado (*seed*) de la rutina si la base de datos está vacía.

- 🎨 **Interfaz de Alto Impacto (Dark & Neon)**:
  - Diseñada bajo **Material 3** con una paleta oscura OLED (`#0D0D0D`) combinada con acentos verde neón de alta visibilidad (`#42D60F`) y tipografías ergonómicas para uso con una sola mano.

---

## 🏛️ Arquitectura y Stack Tecnológico

El proyecto implementa las directrices modernas de desarrollo en Android propuestas por Google:

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Jetpack Compose)           │
│    HomeScreen · ActiveWorkoutScreen · SummaryScreen     │
└───────────────────────────▲────────────────────────────┘
                            │ State / Events
┌───────────────────────────┴────────────────────────────┐
│              ViewModel Layer (StateFlow / Coroutines)  │
│                   WorkoutViewModel                     │
└───────────────────────────▲────────────────────────────┘
                            │ Data Operations
┌───────────────────────────┴────────────────────────────┐
│                    Repository Layer                    │
│                  FirestoreRepository                   │
└───────────────────────────▲────────────────────────────┘
                            │ Remote & Local Sync
┌───────────────────────────┴────────────────────────────┐
│       Data Source: Firebase Firestore (Offline Cache)  │
│             Background: WorkoutTimerService            │
└────────────────────────────────────────────────────────┘
```

### Tecnologías Utilizadas:
| Componente | Tecnología | Propósito |
| :--- | :--- | :--- |
| **Lenguaje** | [Kotlin 2.0](https://kotlinlang.org/) | Código conciso, seguro ante nulos y moderno |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | UI declarativa y reactiva de última generación |
| **Design System** | [Material Design 3](https://m3.material.io/) | Componentes modernos con paleta personalizada |
| **Navegación** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) | Navegación desacoplada y tipada por rutas |
| **Concurrencia** | [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) | Manejo asíncrono y reactivo de datos en segundo plano |
| **Servicios** | Android `ForegroundService` | Control del temporizador de descanso en segundo plano |
| **Backend & Sync** | [Firebase Cloud Firestore](https://firebase.google.com/docs/firestore) | Base de datos NoSQL con persistencia offline |
| **Build System** | Gradle (Kotlin DSL `.kts`) + Version Catalog (`libs.versions.toml`) | Gestión modular y centralizada de dependencias |

---

## 📂 Estructura del Proyecto

```text
nandofit-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/sevenward/nandofit/
│   │   │   │   ├── MainActivity.kt                # Punto de entrada de la actividad
│   │   │   │   ├── NandoFitApp.kt                 # Application class inicializadora
│   │   │   │   ├── WorkoutTimerService.kt         # Servicio foreground del temporizador
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/Models.kt            # Modelos de dominio (Exercise, WorkoutSession, SetLog)
│   │   │   │   │   └── repository/FirestoreRepository.kt # Integración con Firestore y caché
│   │   │   │   └── ui/
│   │   │   │       ├── Navigation.kt              # Grafo de navegación y barra inferior
│   │   │   │       ├── WorkoutViewModel.kt        # Gestión del estado global del entrenamiento
│   │   │   │       ├── components/                # Componentes reusables (Logo, cards, etc.)
│   │   │   │       ├── screens/                   # Pantallas principales (Home, Active, Summary, Progress)
│   │   │   │       └── theme/                     # Paleta de colores, tipografía y tema oscuro
│   │   │   ├── res/                               # Recursos gráficos, mipmaps y vectores
│   │   │   └── AndroidManifest.xml                # Declaración de permisos, actividades y servicios
│   │   └── test/                                  # Pruebas unitarias
│   └── build.gradle.kts                           # Dependencias y configuración del módulo app
├── gradle/                                        # Version Catalogs y Gradle Wrapper
├── build.gradle.kts                               # Configuración raíz de compilación
├── settings.gradle.kts                            # Módulos y repositorios del proyecto
└── README.md                                      # Documentación oficial
```

---

## 📋 Rutina de Entrenamiento Preconfigurada

NandoFit+ incluye por defecto una rutina científica de frecuencia 3 organizada por bloques de torso, pierna y cuerpo completo:

<details>
<summary><b>Día 1: Torso (Push / Pull)</b></summary>

1. **Press Inclinado** (8 - 10 reps | 150s descanso)
2. **Jalón al Pecho** (8 - 12 reps | 120s descanso)
3. **Aperturas** (10 - 15 reps | 90s descanso)
4. **Remo en T** (8 - 10 reps | 150s descanso)
5. **Elevaciones Laterales** (12 - 15 reps | 75s descanso)
6. **Curl de Bíceps** (10 - 12 reps | 90s descanso)
7. **Extensión de Tríceps** (10 - 12 reps | 90s descanso)
</details>

<details>
<summary><b>Día 2: Pierna (Leg Day)</b></summary>

1. **Sentadilla en Máquina** (8 - 10 reps | 150s descanso)
2. **Hip Thrust** (8 - 12 reps | 150s descanso)
3. **Extensión de Cuádriceps** (10 - 15 reps | 90s descanso)
4. **Curl Femoral Sentado** (10 - 15 reps | 90s descanso)
5. **Elevaciones Laterales** (12 - 15 reps | 75s descanso)
6. **Pantorrilla** (12 - 15 reps | 75s descanso)
7. **Abdomen** (12 - 15 reps | 60s descanso)
</details>

<details>
<summary><b>Día 3: Full Body (Alta Densidad)</b></summary>

1. **Prensa de Piernas** (8 - 10 reps | 150s descanso)
2. **Press Plano** (8 - 10 reps | 150s descanso)
3. **Remo con Cable** (10 - 12 reps | 120s descanso)
4. **Press Militar** (8 - 12 reps | 120s descanso)
5. **Curl Femoral** (10 - 15 reps | 90s descanso)
6. **Elevaciones Laterales** (12 - 15 reps | 75s descanso)
7. **Biserie Brazos (Bíceps + Tríceps)** (10 - 12 reps | 90s descanso)
</details>

---

## ⚙️ Requisitos y Configuración

### Prerrequisitos:
- **Android Studio**: Iguana (2023.2.1) o superior.
- **JDK**: Versión 11 o 17 (Java 11 configurado en Gradle).
- **Dispositivo / Emulador**: Android 7.0 (API nivel 24) o superior.

### Configuración de Firebase:
El proyecto utiliza Firebase Firestore. Para conectar tu propio backend:
1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Registra una aplicación Android con el package `sevenward.nandofit`.
3. Descarga el archivo `google-services.json` y colócalo en la carpeta `app/`.
4. Habilita **Cloud Firestore** en modo producción o prueba y configura las reglas de seguridad deseadas.

---

## 🚀 Compilación y Ejecución

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/sevenward-dev/nando-fit-app.git
   cd nando-fit-app
   ```

2. **Abrir en Android Studio**:
   - Selecciona `File > Open` y elige el directorio del repositorio.
   - Espera a que Gradle sincronice las dependencias descritas en `gradle/libs.versions.toml`.

3. **Compilar desde la terminal (opcional)**:
   ```bash
   # Compilar APK de depuración
   ./gradlew assembleDebug

   # Ejecutar comprobaciones y pruebas unitarias
   ./gradlew test
   ```

4. **Ejecutar en tu dispositivo**:
   - Conecta tu teléfono mediante depuración USB o inicia un emulador.
   - Presiona `Run ▶` en Android Studio o instala el APK generado:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🎨 Diseño e Identidad Visual

La interfaz está construida sobre una identidad visual deportiva de alto contraste:

| Elemento | Código Hex | Vista Previa |
| :--- | :--- | :--- |
| **Nando Neon Green** | `#42D60F` | ![#42D60F](https://via.placeholder.com/15/42D60F/000000?text=+) |
| **Nando Black (OLED)** | `#0D0D0D` | ![#0D0D0D](https://via.placeholder.com/15/0D0D0D/000000?text=+) |
| **Nando Dark Card** | `#181A18` | ![#181A18](https://via.placeholder.com/15/181A18/000000?text=+) |
| **Nando Surface Light** | `#F5F7F5` | ![#F5F7F5](https://via.placeholder.com/15/F5F7F5/000000?text=+) |

---

## 👤 Autor

Desarrollado con dedicación por **Sevenward**:
- **GitHub**: [@sevenward-dev](https://github.com/sevenward-dev)
- **Repositorio**: [sevenward-dev/nando-fit-app](https://github.com/sevenward-dev/nando-fit-app)

---
<div align="center">
  <sub>Construido con ❤️, Kotlin y Jetpack Compose para atletas imparables.</sub>
</div>
