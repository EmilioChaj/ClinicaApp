# Gestión de Citas

App Android nativa para gestionar citas médicas. Permite a los pacientes registrarse, iniciar sesión, explorar médicos disponibles por especialidad, consultar horarios de atención y programar/cancelar citas.

## Características

- **Registro e inicio de sesión** de pacientes con contraseñas hasheadas (SHA-256 + sal).
- **Listado de médicos** disponibles con nombre y especialidad.
- **Disponibilidad por médico:** genera slots de 60 minutos a partir del horario de atención, marca los ocupados y bloquea horarios pasados.
- **Programar cita:** selecciona fecha, hora e motivo, con confirmación y revalidación de disponibilidad.
- **Mis citas:** vista de citas próximas (cancelables) e historial de citas pasadas o canceladas.
- **Sesión persistente** mediante `SharedPreferences`.
- **Base de datos local SQLite** con tablas de pacientes, médicos, horarios de atención y citas.

## Tecnologías

- **Lenguaje:** Java 17
- **SDK:** `minSdk 24`, `targetSdk 35`, `compileSdk 35`
- **Base de datos:** SQLite (`SQLiteOpenHelper`)
- **Sesión:** `SharedPreferences`
- **UI:** Material Components, AppCompat, ConstraintLayout, ListView/RecyclerView adapters personalizados
- **Seguridad:** SHA-256 con sal aleatoria por usuario (`PasswordUtil`)

## Estructura del proyecto

```
app/src/main/java/com/gestioncitas/app/
├── ui/                 # Actividades (pantallas)
│   ├── LoginActivity.java
│   ├── RegistroActivity.java
│   ├── MedicosActivity.java
│   ├── DisponibilidadActivity.java
│   └── MisCitasActivity.java
├── model/              # Entidades de datos
│   ├── Paciente.java
│   ├── Medico.java
│   ├── Cita.java
│   ├── Slot.java
│   └── HorarioAtencion.java
├── dao/                # Capa de acceso a datos
│   ├── PacienteDao.java
│   ├── MedicoDao.java
│   ├── CitaDao.java
│   └── HorarioDao.java
├── db/                 # Base de datos SQLite
│   └── DatabaseHelper.java
├── adapter/            # Adaptadores de listas
│   ├── MedicoAdapter.java
│   ├── CitaAdapter.java
│   └── SlotAdapter.java
├── session/            # Gestión de sesión
│   └── SessionManager.java
└── util/               # Utilidades
    └── PasswordUtil.java
```

## Instalación y primeros pasos

### Requisitos

- Android Studio (recomendado)
- JDK 17
- Un emulador Android o dispositivo físico con API 24+

### Pasos

1. Clona el repositorio:

```bash
git clone https://github.com/tu-usuario/GestionCitas.git
cd GestionCitas
```

2. Abre el proyecto en **Android Studio** (`File > Open` → selecciona la carpeta `Gestion_citas`).

3. Espera a que Gradle se sincronice automáticamente.

4. Ejecuta la app en un emulador o dispositivo físico:

```bash
./gradlew assembleDebug
```

o presiona el botón **Run** en Android Studio.

## Cuentas de prueba

Al instalar la app se insertan datos de ejemplo. Puedes iniciar sesión con:

| Usuario | Contraseña |
|---------|------------|
| `maria` | `1234` |
| `juan`  | `1234` |

También puedes crear tu propia cuenta desde la pantalla de registro.

## Arquitectura y decisiones de diseño

- **Patrón DAO:** cada entidad tiene su clase de acceso a datos (`PacienteDao`, `MedicoDao`, `CitaDao`, `HorarioDao`) que abstrae las consultas SQL.
- **Transacciones:** `CitaDao.programarCita()` revalida dentro de una transacción SQLite que el horario siga libre antes de insertar, evitando doble reserva.
- **Migración de contraseñas:** la base de datos pasó de texto plano (v1) a `sal:hash` con SHA-256 (v2) sin borrar datos existentes (`onUpgrade`).
- **Slots de 60 minutos:** `DisponibilidadActivity` genera horarios a partir de los bloques de atención de cada médico y día de la semana.

## Licencia

Este proyecto es de carácter académico / personal. Puedes usarlo y modificarlo libremente.

---

Desarrollado con Android Studio + Java.
