# Informe Técnico - Cliente de Chat Académico
## Aplicación de Mensajería para la Comunidad Universitaria

---

## 1. RESUMEN EJECUTIVO

Se ha desarrollado una aplicación cliente de escritorio para un sistema de chat académico utilizando Java Swing. La aplicación implementa una arquitectura en capas con patrones de diseño (Observer, Object Pool, Factory) y persistencia local mediante H2 Database. El cliente permite autenticación de usuarios, intercambio de mensajes de texto, creación de canales privados/públicos, y gestión de invitaciones a canales.

**Estado actual:** Funcional con servidor mock para pruebas independientes. Preparado para integración con servidor real mediante TCP/IP.

---

## 2. OBJETIVOS DEL CLIENTE

### 2.1 Funcionalidades Principales Implementadas

✅ **Autenticación de usuarios**
- Login con email y contraseña
- Validación mediante servidor (actualmente mock)
- Gestión de sesión de usuario autenticado

✅ **Gestión de canales**
- Creación de canales privados y públicos
- Invitación de usuarios a canales privados
- Aceptación/rechazo de invitaciones
- Visualización de canales disponibles

✅ **Mensajería**
- Envío de mensajes de texto a usuarios individuales
- Envío de mensajes a canales
- Persistencia local de conversaciones

✅ **Persistencia local (H2 Database)**
- Almacenamiento de usuarios
- Almacenamiento de canales y membresías
- Almacenamiento de mensajes
- Almacenamiento de solicitudes de invitación

### 2.2 Funcionalidades Pendientes (Requieren Servidor Real)

⏳ **Mensajes de audio**
- Grabación de audio
- Reproducción mediante Object Pool de reproductores
- Conversión a texto (servidor)

⏳ **Sincronización con servidor**
- Actualización de datos locales con servidor MySQL
- Sincronización de mensajes offline

⏳ **Gestión de usuarios conectados**
- Visualización de usuarios online/offline
- Restricción de conexiones simultáneas

---

## 3. ARQUITECTURA IMPLEMENTADA

### 3.1 Estructura de Capas

La aplicación sigue una arquitectura en capas claramente definida:

\`\`\`
┌─────────────────────────────────────────┐
│         CAPA DE PRESENTACIÓN            │
│  (UI - Swing Components)                │
│  - ClienteUI, LoginUI, ChatUI           │
│  - CrearCanalPanel, InvitarUsuariosPanel│
│  - SolicitudesPanel                     │
└─────────────────────────────────────────┘
↓
┌─────────────────────────────────────────┐
│         CAPA DE CONTROLADORES           │
│  - AuthController                       │
│  - MensajeController                    │
│  - CanalController                      │
└─────────────────────────────────────────┘
↓
┌─────────────────────────────────────────┐
│         CAPA DE LÓGICA DE NEGOCIO       │
│  - AuthBusinessLogic                    │
│  - MensajeBusinessLogic                 │
│  - CanalBusinessLogic                   │
│  - ServicioNotificaciones (Observer)    │
└─────────────────────────────────────────┘
↓
┌─────────────────────────────────────────┐
│         CAPA DE COMUNICACIÓN            │
│  - GestorComunicacion                   │
│  - ServidorMock (temporal)              │
│  - PoolReproductoresAudio (Object Pool) │
└─────────────────────────────────────────┘
↓
┌─────────────────────────────────────────┐
│         CAPA DE DATOS                   │
│  - RepositorioLocal                     │
│  - PoolConexiones (Object Pool)         │
│  - H2 Database                          │
└─────────────────────────────────────────┘
\`\`\`

### 3.2 Paquetes y Organización

\`\`\`
org.example.client/
├── modelo/                    # Entidades del dominio
│   ├── Usuario.java
│   ├── Canal.java
│   ├── Mensaje.java
│   ├── MensajeTexto.java
│   ├── MensajeAudio.java
│   ├── MensajeCrearCanal.java
│   ├── MensajeInvitacion.java
│   ├── MensajeRespuestaInvitacion.java
│   ├── MensajeSolicitudUnion.java
│   ├── MensajeRespuesta.java
│   ├── Solicitud.java
│   └── UsuarioCanal.java
│
├── ui/                        # Interfaz gráfica
│   ├── ClienteUI.java         # Ventana principal (JFrame)
│   ├── LoginUI.java           # Panel de login
│   ├── ChatUI.java            # Panel principal de chat
│   ├── CrearCanalPanel.java   # Formulario crear canal
│   ├── InvitarUsuariosPanel.java
│   └── SolicitudesPanel.java  # Gestión de invitaciones
│
├── controladores/             # Controladores MVC
│   ├── AuthController.java
│   ├── MensajeController.java
│   └── CanalController.java
│
├── negocio/                   # Lógica de negocio
│   ├── AuthBusinessLogic.java
│   ├── MensajeBusinessLogic.java
│   ├── CanalBusinessLogic.java
│   └── ServicioNotificaciones.java
│
├── comunicacion/              # Comunicación con servidor
│   ├── GestorComunicacion.java
│   ├── PoolReproductoresAudio.java
│   └── ReproductorAudio.java
│
├── datos/                     # Persistencia
│   ├── RepositorioLocal.java
│   └── PoolConexiones.java
│
├── config/                    # Configuración
│   └── ConfigManager.java
│
└── mock/                      # Servidor simulado
└── ServidorMock.java
\`\`\`

---

## 4. COMPONENTES DESARROLLADOS

### 4.1 Modelos de Dominio

#### **Usuario**
\`\`\`java
- id: String (UUID)
- nombre: String
- correo: String (email único)
- contrasena: String
- foto: String (ruta)
- direccionIp: String
  \`\`\`

#### **Canal**
\`\`\`java
- id: String (UUID)
- nombre: String
- privado: boolean
- creadorEmail: String
- miembros: List<String> (emails)
- creadoEn: LocalDateTime
  \`\`\`

#### **Mensaje (Jerarquía)**
\`\`\`java
Mensaje (abstracta)
├── MensajeTexto
│   ├── contenido: String
│   └── destinatario: String
└── MensajeAudio
├── rutaArchivo: String
├── duracion: int
└── tamanoBytes: long
\`\`\`

#### **Solicitud**
\`\`\`java
- id: String (UUID)
- idCanal: String
- correoUsuario: String
- estado: String (PENDIENTE/ACEPTADA/RECHAZADA)
- fechaSolicitud: LocalDateTime
  \`\`\`

### 4.2 Capa de Presentación (UI)

#### **ClienteUI** - Ventana Principal
- JFrame con CardLayout para cambiar entre pantallas
- Gestiona la transición Login → Chat
- Inicializa todos los componentes del sistema
- Configura la conexión con el servidor (mock o real)

#### **LoginUI** - Pantalla de Autenticación
- Formulario con email y contraseña
- Validación de campos
- Comunicación con AuthController
- Manejo de errores de autenticación

#### **ChatUI** - Interfaz Principal de Chat
- **Panel izquierdo:** Lista de usuarios y canales disponibles
- **Panel central:** Área de mensajes y formularios
- **Panel derecho:** Información de canal/usuario seleccionado
- Navegación entre vistas:
    - Vista de bienvenida
    - Vista de chat con usuario
    - Vista de chat en canal
    - Formulario crear canal
    - Formulario invitar usuarios
    - Panel de solicitudes pendientes

#### **CrearCanalPanel** - Creación de Canales
- Formulario con nombre del canal
- Checkbox para canal privado/público
- Validación de datos
- Botón volver a pantalla principal

#### **InvitarUsuariosPanel** - Invitaciones
- Lista de usuarios disponibles con checkboxes
- Selección múltiple de usuarios
- Envío de invitaciones al servidor
- Confirmación de envío

#### **SolicitudesPanel** - Gestión de Invitaciones
- Lista de invitaciones pendientes
- Información del canal (nombre, creador)
- Botones Aceptar/Rechazar por cada solicitud
- Actualización automática al responder

### 4.3 Capa de Controladores

#### **AuthController**
\`\`\`java
+ autenticar(email: String, password: String): boolean
+ cerrarSesion(): void
+ obtenerUsuarioActual(): Usuario
  \`\`\`
- Coordina autenticación entre UI y lógica de negocio
- Valida formato de credenciales
- Maneja errores de autenticación

#### **MensajeController**
\`\`\`java
+ enviarMensaje(contenido: String): void
+ obtenerHistorial(destinatario: String): List<MensajeTexto>
  \`\`\`
- Gestiona envío de mensajes
- Recupera historial de conversaciones
- Notifica a la UI de nuevos mensajes

#### **CanalController**
\`\`\`java
+ crearCanal(nombre: String, privado: boolean): void
+ invitarUsuarios(idCanal: String, usuarios: List<String>): void
+ responderInvitacion(idSolicitud: String, aceptar: boolean): void
+ obtenerCanalesDelUsuario(): List<Canal>
+ obtenerSolicitudesPendientes(): List<Solicitud>
  \`\`\`
- Coordina todas las operaciones de canales
- Valida permisos de usuario
- Gestiona el ciclo de vida de invitaciones

### 4.4 Capa de Lógica de Negocio

#### **AuthBusinessLogic**
- Comunicación con servidor para autenticación
- Almacenamiento de sesión de usuario actual
- Validación de credenciales
- Gestión de tokens/sesión

#### **MensajeBusinessLogic**
- Envío de mensajes al servidor
- Persistencia local de mensajes
- Recuperación de historial
- Notificación a observadores

#### **CanalBusinessLogic**
- Creación de canales (validación de permisos)
- Gestión de invitaciones
- Aceptación/rechazo de solicitudes
- Persistencia de canales y membresías
- Validación de usuario autenticado

#### **ServicioNotificaciones** (Patrón Observer)
\`\`\`java
- observadores: List<Observer>
+ agregarObservador(Observer): void
+ eliminarObservador(Observer): void
+ notificar(evento: String, datos: Object): void
  \`\`\`
- Implementa patrón Observer
- Notifica cambios a la UI
- Desacopla lógica de negocio de presentación

### 4.5 Capa de Comunicación

#### **GestorComunicacion**
- Abstracción de comunicación con servidor
- Envío/recepción de mensajes
- Manejo de conexión TCP/IP (preparado)
- Actualmente usa ServidorMock

#### **ServidorMock** (Temporal)
- Simula respuestas del servidor real
- Almacena datos en memoria:
    - Usuarios predeterminados
    - Canales predeterminados (General, Investigación, Clase-2025)
    - Mensajes enviados
    - Solicitudes de invitación
- Valida credenciales específicas: `cliente@uni.edu / 1234`
- Permite pruebas sin servidor real

#### **PoolReproductoresAudio** (Patrón Object Pool)
\`\`\`java
- pool: Queue<ReproductorAudio>
- tamanoMaximo: int
+ obtenerReproductor(): ReproductorAudio
+ liberarReproductor(ReproductorAudio): void
  \`\`\`
- Reutiliza instancias de reproductores
- Optimiza recursos multimedia
- Evita creación/destrucción constante

### 4.6 Capa de Datos

#### **RepositorioLocal**
Gestiona toda la persistencia en H2 Database:

**Usuarios:**
\`\`\`java
+ guardarUsuario(Usuario): void
+ obtenerUsuarioPorEmail(String): Usuario
+ obtenerTodosLosUsuarios(): List<Usuario>
  \`\`\`

**Canales:**
\`\`\`java
+ guardarCanal(Canal): void
+ obtenerCanalesDelUsuario(String email): List<Canal>
+ obtenerMiembrosDeCanal(String idCanal): List<String>
+ agregarMiembroACanal(String idCanal, String email): void
  \`\`\`

**Mensajes:**
\`\`\`java
+ guardarMensaje(MensajeTexto): void
+ obtenerMensajesEntre(String user1, String user2): List<MensajeTexto>
+ obtenerMensajesDeCanal(String idCanal): List<MensajeTexto>
  \`\`\`

**Solicitudes:**
\`\`\`java
+ guardarSolicitud(Solicitud): void
+ obtenerSolicitudesPendientes(String email): List<Solicitud>
+ actualizarEstadoSolicitud(String id, String estado): void
  \`\`\`

**Inicialización automática:**
- Crea tablas si no existen al iniciar
- Ejecuta scripts SQL de inicialización
- Configura índices y relaciones

#### **PoolConexiones** (Patrón Object Pool)
\`\`\`java
- pool: Queue<Connection>
- tamanoMaximo: int = 10
+ obtenerConexion(): Connection
+ liberarConexion(Connection): void
+ cerrarPool(): void
  \`\`\`
- Reutiliza conexiones a H2
- Mejora rendimiento de acceso a BD
- Limita recursos utilizados

---

## 5. PATRONES DE DISEÑO APLICADOS

### 5.1 Patrón Observer

**Implementación:** `ServicioNotificaciones`

**Propósito:** Actualizar la interfaz gráfica cuando ocurren eventos en la lógica de negocio sin acoplar las capas.

**Uso en el sistema:**
- La UI se registra como observador
- Cuando llega un nuevo mensaje → notifica a la UI
- Cuando se crea un canal → actualiza lista de canales
- Cuando se acepta una invitación → actualiza membresías

**Ejemplo:**
\`\`\`java
// En ChatUI (Observer)
servicioNotificaciones.agregarObservador((evento, datos) -> {
if (evento.equals("NUEVO_MENSAJE")) {
actualizarListaMensajes();
}
});

// En MensajeBusinessLogic (Subject)
servicioNotificaciones.notificar("NUEVO_MENSAJE", mensaje);
\`\`\`

### 5.2 Patrón Object Pool

**Implementaciones:**
1. `PoolConexiones` - Pool de conexiones H2
2. `PoolReproductoresAudio` - Pool de reproductores de audio

**Propósito:** Reutilizar objetos costosos de crear/destruir, optimizando recursos del sistema.

**Ventajas:**
- Reduce latencia de creación de objetos
- Controla uso de recursos (memoria, conexiones)
- Mejora rendimiento en operaciones frecuentes

**Ejemplo:**
\`\`\`java
// Obtener conexión del pool
Connection conn = poolConexiones.obtenerConexion();
try {
// Usar conexión
PreparedStatement stmt = conn.prepareStatement("...");
// ...
} finally {
// Devolver al pool (no cerrar)
poolConexiones.liberarConexion(conn);
}
\`\`\`

### 5.3 Patrón MVC (Model-View-Controller)

**Separación de responsabilidades:**
- **Model:** Clases en `modelo/` (Usuario, Canal, Mensaje, etc.)
- **View:** Clases en `ui/` (ClienteUI, ChatUI, LoginUI, etc.)
- **Controller:** Clases en `controladores/` (AuthController, MensajeController, CanalController)

**Flujo:**
\`\`\`
Usuario interactúa con UI
↓
UI llama a Controller
↓
Controller llama a BusinessLogic
↓
BusinessLogic actualiza Model y persiste
↓
BusinessLogic notifica a Observer
↓
UI se actualiza automáticamente
\`\`\`

### 5.4 Patrón Repository

**Implementación:** `RepositorioLocal`

**Propósito:** Abstraer el acceso a datos, separando la lógica de persistencia de la lógica de negocio.

**Ventajas:**
- Cambiar de H2 a otra BD sin afectar lógica de negocio
- Centraliza queries SQL
- Facilita testing (mock del repositorio)

---

## 6. FLUJO DE FUNCIONAMIENTO

### 6.1 Flujo de Inicio de Aplicación

\`\`\`
1. Usuario ejecuta JAR
   ↓
2. ClienteUI.main() inicia
   ↓
3. Inicializa componentes:
    - PoolConexiones (H2 Database en ./data/clientdb)
    - RepositorioLocal (crea tablas si no existen)
    - GestorComunicacion (conecta a ServidorMock)
    - ServicioNotificaciones
    - AuthBusinessLogic
    - CanalBusinessLogic
    - Controladores
      ↓
4. Muestra LoginUI
   ↓
5. Usuario ingresa credenciales
   ↓
6. AuthController valida con servidor
   ↓
7. Si éxito → Muestra ChatUI
   Si fallo → Muestra error
   \`\`\`

### 6.2 Flujo de Autenticación

\`\`\`
LoginUI
↓ (usuario ingresa email/password)
AuthController.autenticar(email, password)
↓
AuthBusinessLogic.autenticar(email, password)
↓
GestorComunicacion.enviar(MensajeAutenticacion)
↓
ServidorMock.procesarMensaje()
↓ (valida credenciales)
ServidorMock.responder(MensajeRespuesta)
↓
AuthBusinessLogic.guardarUsuarioActual(usuario)
↓
ClienteUI.mostrarChat()
↓
ChatUI se muestra con datos del usuario
\`\`\`

### 6.3 Flujo de Creación de Canal

\`\`\`
ChatUI - Usuario hace clic en "Crear Canal"
↓
ChatUI.mostrarCrearCanal()
↓
CrearCanalPanel se muestra en panel central
↓
Usuario completa formulario (nombre, privado/público)
↓
CrearCanalPanel → CanalController.crearCanal(nombre, privado)
↓
CanalController valida datos
↓
CanalBusinessLogic.crearCanal(nombre, privado)
↓ (verifica usuario autenticado)
CanalBusinessLogic crea objeto Canal
↓
GestorComunicacion.enviar(MensajeCrearCanal)
↓
ServidorMock.procesarCrearCanal()
↓ (almacena en memoria)
ServidorMock.responder(MensajeRespuesta con Canal)
↓
CanalBusinessLogic.guardarCanalLocal(canal)
↓
RepositorioLocal.guardarCanal(canal)
↓ (INSERT en H2)
RepositorioLocal.agregarMiembroACanal(idCanal, creadorEmail)
↓
ServicioNotificaciones.notificar("CANAL_CREADO", canal)
↓
ChatUI.actualizar() - Actualiza lista de canales
↓
ChatUI.mostrarPantallaInicio() - Vuelve a pantalla principal
\`\`\`

### 6.4 Flujo de Invitación a Canal

\`\`\`
ChatUI - Usuario selecciona canal y hace clic en "Invitar Usuarios"
↓
ChatUI.mostrarInvitarUsuarios(canal)
↓
InvitarUsuariosPanel se muestra
↓ (carga lista de usuarios disponibles)
RepositorioLocal.obtenerTodosLosUsuarios()
↓
Usuario selecciona usuarios con checkboxes
↓
InvitarUsuariosPanel → CanalController.invitarUsuarios(idCanal, usuarios)
↓
CanalController valida permisos (solo creador puede invitar)
↓
CanalBusinessLogic.invitarUsuarios(idCanal, usuarios)
↓
Para cada usuario:
CanalBusinessLogic crea MensajeInvitacion
↓
GestorComunicacion.enviar(MensajeInvitacion)
↓
ServidorMock.procesarInvitacion()
↓ (crea Solicitud con estado PENDIENTE)
ServidorMock.guardarSolicitud(solicitud)
↓
RepositorioLocal.guardarSolicitud(solicitud)
↓
ServicioNotificaciones.notificar("INVITACIONES_ENVIADAS")
↓
ChatUI muestra mensaje de confirmación
\`\`\`

### 6.5 Flujo de Respuesta a Invitación

\`\`\`
ChatUI - Usuario hace clic en "Ver Invitaciones"
↓
ChatUI.mostrarSolicitudes()
↓
SolicitudesPanel se muestra
↓
CanalController.obtenerSolicitudesPendientes()
↓
RepositorioLocal.obtenerSolicitudesPendientes(emailUsuario)
↓ (SELECT de solicitudes con estado PENDIENTE)
SolicitudesPanel muestra lista de invitaciones
↓
Usuario hace clic en "Aceptar" o "Rechazar"
↓
SolicitudesPanel → CanalController.responderInvitacion(idSolicitud, aceptar)
↓
CanalBusinessLogic.responderInvitacion(idSolicitud, aceptar)
↓
CanalBusinessLogic crea MensajeRespuestaInvitacion
↓
GestorComunicacion.enviar(MensajeRespuestaInvitacion)
↓
ServidorMock.procesarRespuestaInvitacion()
↓
Si ACEPTADA:
ServidorMock.agregarMiembroACanal(idCanal, emailUsuario)
RepositorioLocal.agregarMiembroACanal(idCanal, emailUsuario)
RepositorioLocal.actualizarEstadoSolicitud(idSolicitud, "ACEPTADA")
Si RECHAZADA:
RepositorioLocal.actualizarEstadoSolicitud(idSolicitud, "RECHAZADA")
↓
ServicioNotificaciones.notificar("SOLICITUD_RESPONDIDA")
↓
SolicitudesPanel.actualizarLista() - Remueve solicitud de la lista
\`\`\`

### 6.6 Flujo de Envío de Mensaje

\`\`\`
ChatUI - Usuario escribe mensaje y presiona Enter
↓
ChatUI → MensajeController.enviarMensaje(contenido)
↓
MensajeController.enviarMensaje(contenido)
↓
MensajeBusinessLogic.enviarMensajeTexto(contenido)
↓
MensajeBusinessLogic crea MensajeTexto
↓
GestorComunicacion.enviar(MensajeTexto)
↓
ServidorMock.procesarMensajeTexto()
↓ (almacena en memoria)
ServidorMock.guardarMensaje(mensaje)
↓
ServidorMock.responder(MensajeRespuesta confirmación)
↓
ServicioNotificaciones.notificar("MENSAJE_ENVIADO", mensaje)
↓
ChatUI.actualizar() - Muestra mensaje en área de chat
\`\`\`

---

## 7. PERSISTENCIA DE DATOS (H2 DATABASE)

### 7.1 Configuración de Base de Datos

**Ubicación:** `./data/clientdb` (relativo al JAR)

**Estructura de archivos:**
\`\`\`
Padre/
└── client/
├── app-client-1.0-SNAPSHOT.jar
└── data/
├── clientdb.mv.db      # Archivo de datos H2
└── clientdb.trace.db   # Logs de H2
\`\`\`

**Configuración de conexión:**
\`\`\`java
String url = "jdbc:h2:./data/clientdb;AUTO_SERVER=TRUE";
String user = "sa";
String password = "";
\`\`\`

**Ventajas de esta configuración:**
- Base de datos portátil (se mueve con el JAR)
- No requiere instalación de servidor
- Modo AUTO_SERVER permite múltiples conexiones
- Datos persisten entre ejecuciones

### 7.2 Esquema de Base de Datos

#### Tabla: **usuarios**
\`\`\`sql
CREATE TABLE IF NOT EXISTS usuarios (
id VARCHAR(36) PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
correo VARCHAR(100) UNIQUE NOT NULL,
contrasena VARCHAR(255) NOT NULL,
foto VARCHAR(255),
direccion_ip VARCHAR(45),
fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

#### Tabla: **canales**
\`\`\`sql
CREATE TABLE IF NOT EXISTS canales (
id VARCHAR(36) PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
privado BOOLEAN NOT NULL,
creador_email VARCHAR(100) NOT NULL,
creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (creador_email) REFERENCES usuarios(correo)
);
\`\`\`

#### Tabla: **canal_miembros**
\`\`\`sql
CREATE TABLE IF NOT EXISTS canal_miembros (
id_canal VARCHAR(36) NOT NULL,
correo_usuario VARCHAR(100) NOT NULL,
fecha_union TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (id_canal, correo_usuario),
FOREIGN KEY (id_canal) REFERENCES canales(id),
FOREIGN KEY (correo_usuario) REFERENCES usuarios(correo)
);
\`\`\`

#### Tabla: **mensajes**
\`\`\`sql
CREATE TABLE IF NOT EXISTS mensajes (
id VARCHAR(36) PRIMARY KEY,
remitente_email VARCHAR(100) NOT NULL,
destinatario_email VARCHAR(100),
id_canal VARCHAR(36),
contenido TEXT NOT NULL,
fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
tipo VARCHAR(20) DEFAULT 'TEXTO',
FOREIGN KEY (remitente_email) REFERENCES usuarios(correo),
FOREIGN KEY (destinatario_email) REFERENCES usuarios(correo),
FOREIGN KEY (id_canal) REFERENCES canales(id)
);
\`\`\`

#### Tabla: **solicitudes**
\`\`\`sql
CREATE TABLE IF NOT EXISTS solicitudes (
id VARCHAR(36) PRIMARY KEY,
id_canal VARCHAR(36) NOT NULL,
correo_usuario VARCHAR(100) NOT NULL,
estado VARCHAR(20) DEFAULT 'PENDIENTE',
fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (id_canal) REFERENCES canales(id),
FOREIGN KEY (correo_usuario) REFERENCES usuarios(correo)
);
\`\`\`

### 7.3 Inicialización Automática

**Proceso al iniciar la aplicación:**

1. `PoolConexiones` se crea con URL de H2
2. `RepositorioLocal` obtiene conexión del pool
3. `RepositorioLocal.inicializarTablas()` ejecuta:
    - Verifica si tablas existen
    - Si no existen, ejecuta CREATE TABLE para cada una
    - Crea índices para optimizar consultas
4. Base de datos queda lista para uso

**Código de inicialización:**
\`\`\`java
public void inicializarTablas() {
Connection conn = null;
try {
conn = poolConexiones.obtenerConexion();
Statement stmt = conn.createStatement();

        // Crear tabla usuarios
        stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (...)");
        
        // Crear tabla canales
        stmt.execute("CREATE TABLE IF NOT EXISTS canales (...)");
        
        // ... resto de tablas
        
        System.out.println("✓ Tablas inicializadas correctamente");
    } catch (SQLException e) {
        System.err.println("Error inicializando tablas: " + e.getMessage());
    } finally {
        if (conn != null) poolConexiones.liberarConexion(conn);
    }
}
\`\`\`

### 7.4 Operaciones de Persistencia

**Ejemplo: Guardar Canal**
\`\`\`java
public void guardarCanal(Canal canal) {
Connection conn = null;
try {
conn = poolConexiones.obtenerConexion();
String sql = "INSERT INTO canales (id, nombre, privado, creador_email, creado_en) " +
"VALUES (?, ?, ?, ?, ?)";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, canal.getId());
stmt.setString(2, canal.getNombre());
stmt.setBoolean(3, canal.isPrivado());
stmt.setString(4, canal.getCreadorEmail());
stmt.setTimestamp(5, Timestamp.valueOf(canal.getCreadoEn()));
stmt.executeUpdate();

        // Guardar miembros
        for (String miembro : canal.getMiembros()) {
            agregarMiembroACanal(canal.getId(), miembro);
        }
    } catch (SQLException e) {
        System.err.println("Error guardando canal: " + e.getMessage());
    } finally {
        if (conn != null) poolConexiones.liberarConexion(conn);
    }
}
\`\`\`

**Ejemplo: Recuperar Canales del Usuario**
\`\`\`java
public List<Canal> obtenerCanalesDelUsuario(String correoUsuario) {
List<Canal> canales = new ArrayList<>();
Connection conn = null;
try {
conn = poolConexiones.obtenerConexion();
String sql = "SELECT DISTINCT c.id, c.nombre, c.privado, c.creador_email, c.creado_en " +
"FROM canales c " +
"INNER JOIN canal_miembros cm ON c.id = cm.id_canal " +
"WHERE cm.correo_usuario = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, correoUsuario);
ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            List<String> miembros = obtenerMiembrosDeCanal(rs.getString("id"));
            Canal canal = new Canal(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getBoolean("privado"),
                rs.getString("creador_email"),
                miembros,
                rs.getTimestamp("creado_en").toLocalDateTime()
            );
            canales.add(canal);
        }
    } catch (SQLException e) {
        System.err.println("Error obteniendo canales: " + e.getMessage());
    } finally {
        if (conn != null) poolConexiones.liberarConexion(conn);
    }
    return canales;
}
\`\`\`

---

## 8. SERVIDOR MOCK (SIMULACIÓN)

### 8.1 Propósito

El `ServidorMock` permite desarrollar y probar el cliente sin depender del servidor real. Simula todas las respuestas del servidor y mantiene estado en memoria.

### 8.2 Funcionalidades Implementadas

**Autenticación:**
- Valida credenciales: `cliente@uni.edu / 1234`
- Retorna usuario autenticado o error

**Gestión de Usuarios:**
- Usuarios predeterminados:
    - cliente@uni.edu
    - profesor@uni.edu
    - estudiante1@uni.edu
    - estudiante2@uni.edu
    - admin@uni.edu

**Gestión de Canales:**
- Canales predeterminados:
    - **General** (público) - Todos los usuarios
    - **Investigación** (privado) - profesor@uni.edu, estudiante1@uni.edu
    - **Clase-2025** (privado) - profesor@uni.edu, estudiante1@uni.edu, estudiante2@uni.edu
- Creación de nuevos canales
- Almacenamiento en memoria de canales creados

**Mensajería:**
- Recepción de mensajes
- Almacenamiento en memoria
- Confirmación de envío

**Invitaciones:**
- Procesamiento de invitaciones
- Creación de solicitudes
- Aceptación/rechazo de solicitudes
- Actualización de membresías

### 8.3 Estructura de Datos en Memoria

\`\`\`java
public class ServidorMock {
// Usuarios del sistema
private Map<String, Usuario> usuarios = new HashMap<>();

    // Canales disponibles
    private List<Canal> canales = new ArrayList<>();
    
    // Mensajes enviados
    private List<MensajeTexto> mensajes = new ArrayList<>();
    
    // Solicitudes de invitación
    private List<Solicitud> solicitudes = new ArrayList<>();
    
    // ... métodos de procesamiento
}
\`\`\`

### 8.4 Transición a Servidor Real

**Pasos para conectar con servidor real:**

1. Implementar cliente TCP/IP en `GestorComunicacion`
2. Configurar IP y puerto del servidor en `ConfigManager`
3. Implementar serialización/deserialización de mensajes
4. Cambiar flag de modo mock a false
5. El resto del código NO requiere cambios (arquitectura desacoplada)

**Código a modificar:**
\`\`\`java
// En GestorComunicacion
public void conectar() {
if (ConfigManager.isModoMock()) {
this.servidor = new ServidorMock();
} else {
// Conectar a servidor real TCP/IP
Socket socket = new Socket(ConfigManager.getServidorIP(),
ConfigManager.getServidorPuerto());
// ... configurar streams
}
}
\`\`\`

---

## 9. CONSIDERACIONES TÉCNICAS

### 9.1 Seguridad

⚠️ **Contraseñas en texto plano (TEMPORAL)**
- Actualmente las contraseñas se almacenan sin cifrar
- **PENDIENTE:** Implementar hashing con BCrypt o similar
- **PENDIENTE:** Agregar campo "sal" a tabla usuarios

⚠️ **Validación de entrada**
- Se valida formato de email
- Se valida longitud de campos
- **PENDIENTE:** Sanitización contra SQL injection (usar PreparedStatement - ya implementado)

✅ **Gestión de sesión**
- Usuario autenticado se almacena en memoria
- Se valida usuario autenticado antes de operaciones sensibles

### 9.2 Rendimiento

✅ **Object Pool de Conexiones**
- Máximo 10 conexiones simultáneas a H2
- Reutilización de conexiones reduce latencia
- Cierre automático al finalizar aplicación

✅ **Índices en Base de Datos**
- Índice en `usuarios.correo` (UNIQUE)
- Índice compuesto en `canal_miembros(id_canal, correo_usuario)`
- Mejora velocidad de consultas JOIN

⚠️ **Carga de datos**
- Actualmente se cargan todos los usuarios/canales en memoria
- **PENDIENTE:** Implementar paginación para grandes volúmenes

### 9.3 Manejo de Errores

✅ **Excepciones capturadas**
- SQLException en operaciones de BD
- Mensajes de error informativos en consola
- Diálogos de error en UI

⚠️ **Logging**
- Actualmente se usa System.out/System.err
- **PENDIENTE:** Implementar logger profesional (Log4j, SLF4J)

### 9.4 Concurrencia

⚠️ **Thread-safety**
- PoolConexiones usa Queue thread-safe
- **PENDIENTE:** Sincronizar acceso a ServidorMock (múltiples clientes)
- **PENDIENTE:** Implementar locks en operaciones críticas

### 9.5 Configuración

✅ **Archivos de configuración**
- `ConfigManager` carga configuración
- Modo mock activable/desactivable
- **PENDIENTE:** Externalizar configuración a archivo .properties

### 9.6 Testing

⚠️ **Pruebas unitarias**
- **PENDIENTE:** Implementar JUnit tests
- **PENDIENTE:** Mock de RepositorioLocal para tests
- **PENDIENTE:** Tests de integración

---

## 10. ESTADO ACTUAL Y PRÓXIMOS PASOS

### 10.1 Funcionalidades Completadas ✅

- [x] Arquitectura en capas implementada
- [x] Autenticación de usuarios
- [x] Interfaz gráfica completa (Swing)
- [x] Creación de canales privados/públicos
- [x] Sistema de invitaciones a canales
- [x] Aceptación/rechazo de invitaciones
- [x] Envío de mensajes de texto
- [x] Persistencia local en H2
- [x] Patrón Observer implementado
- [x] Patrón Object Pool implementado (conexiones y reproductores)
- [x] Servidor Mock funcional
- [x] Inicialización automática de base de datos

### 10.2 Funcionalidades Pendientes ⏳

#### **Alta Prioridad**
- [ ] Mensajes de audio (grabación y reproducción)
- [ ] Conversión de audio a texto (requiere servidor)
- [ ] Conexión TCP/IP con servidor real
- [ ] Sincronización de datos offline → online
- [ ] Cifrado de contraseñas (BCrypt)

#### **Media Prioridad**
- [ ] Visualización de usuarios conectados/desconectados
- [ ] Indicador de "escribiendo..." en chat
- [ ] Notificaciones de escritorio
- [ ] Búsqueda de mensajes
- [ ] Exportar historial de conversaciones

#### **Baja Prioridad**
- [ ] Temas de interfaz (claro/oscuro)
- [ ] Emojis en mensajes
- [ ] Compartir archivos (imágenes, documentos)
- [ ] Videollamadas (fuera de alcance inicial)

### 10.3 Integración con Servidor Real

**Requisitos del servidor:**
1. Protocolo TCP/IP en puerto configurable
2. Formato de mensajes compatible (JSON o serialización Java)
3. Endpoints para:
    - Autenticación
    - Registro de usuarios
    - Envío/recepción de mensajes
    - Gestión de canales
    - Gestión de invitaciones
    - Sincronización de datos

**Pasos de integración:**
1. Definir protocolo de comunicación con equipo de servidor
2. Implementar serialización/deserialización de mensajes
3. Reemplazar ServidorMock con cliente TCP/IP real
4. Implementar reconexión automática
5. Implementar sincronización de datos locales con servidor
6. Pruebas de integración cliente-servidor

### 10.4 Mejoras Técnicas Sugeridas

**Arquitectura:**
- Implementar inyección de dependencias (Spring o manual)
- Separar configuración en archivos externos
- Implementar sistema de logging profesional

**Base de Datos:**
- Agregar migraciones de esquema (Flyway o Liquibase)
- Implementar caché de consultas frecuentes
- Optimizar queries con EXPLAIN PLAN

**UI/UX:**
- Mejorar diseño visual (colores, tipografía)
- Agregar animaciones de transición
- Implementar atajos de teclado
- Mejorar accesibilidad (screen readers)

**Testing:**
- Cobertura de tests unitarios > 80%
- Tests de integración para flujos completos
- Tests de carga (múltiples usuarios simultáneos)

---

## 11. CONCLUSIONES

### 11.1 Logros Principales

Se ha desarrollado exitosamente una aplicación cliente de chat académico con arquitectura sólida y escalable. La implementación de patrones de diseño (Observer, Object Pool, MVC, Repository) garantiza mantenibilidad y extensibilidad del código.

La persistencia local mediante H2 Database permite almacenar conversaciones offline, cumpliendo con el requisito de disponibilidad de datos sin conexión al servidor.

El uso de un servidor mock permite desarrollo y pruebas independientes del equipo de servidor, acelerando el ciclo de desarrollo.

### 11.2 Desafíos Superados

- Diseño de arquitectura en capas con bajo acoplamiento
- Implementación de patrón Observer para actualización reactiva de UI
- Gestión de pool de conexiones para optimizar recursos
- Persistencia relacional con H2 en modo embebido
- Diseño de interfaz gráfica compleja con múltiples vistas

### 11.3 Lecciones Aprendidas

- La separación en capas facilita enormemente el testing y mantenimiento
- Los patrones de diseño no son solo teoría, resuelven problemas reales
- Un servidor mock bien diseñado acelera el desarrollo
- La persistencia local es crucial para aplicaciones offline-first
- Swing sigue siendo viable para aplicaciones de escritorio empresariales

### 11.4 Recomendaciones para el Equipo

1. **Definir protocolo de comunicación cuanto antes** para facilitar integración
2. **Implementar tests unitarios** antes de integrar con servidor real
3. **Documentar APIs** de cada capa para facilitar colaboración
4. **Realizar code reviews** para mantener calidad del código
5. **Planificar migración de datos** de mock a servidor real

---

## 12. ANEXOS

### 12.1 Comandos de Ejecución

**Compilar proyecto:**
\`\`\`bash
mvn clean package
\`\`\`

**Ejecutar aplicación:**
\`\`\`bash
java -jar target/app-client-1.0-SNAPSHOT.jar
\`\`\`

**Ejecutar con modo debug:**
\`\`\`bash
java -Ddebug=true -jar target/app-client-1.0-SNAPSHOT.jar
\`\`\`

### 12.2 Credenciales de Prueba

**Usuario principal:**
- Email: `cliente@uni.edu`
- Contraseña: `1234`

**Usuarios adicionales (mock):**
- `profesor@uni.edu / 1234`
- `estudiante1@uni.edu / 1234`
- `estudiante2@uni.edu / 1234`
- `admin@uni.edu / 1234`

### 12.3 Estructura de Directorios

\`\`\`
app-client/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/example/client/
│       │       ├── modelo/
│       │       ├── ui/
│       │       ├── controladores/
│       │       ├── negocio/
│       │       ├── comunicacion/
│       │       ├── datos/
│       │       ├── config/
│       │       └── mock/
│       └── resources/
├── target/
│   └── app-client-1.0-SNAPSHOT.jar
├── data/                    # Creado automáticamente
│   ├── clientdb.mv.db
│   └── clientdb.trace.db
├── pom.xml
└── README.md
\`\`\`

### 12.4 Dependencias Maven

\`\`\`xml
<dependencies>
<!-- H2 Database -->
<dependency>
<groupId>com.h2database</groupId>
<artifactId>h2</artifactId>
<version>2.2.224</version>
</dependency>

    <!-- Otras dependencias según necesidad -->
</dependencies>
\`\`\`

### 12.5 Contacto y Soporte

Para dudas o problemas con la implementación del cliente, contactar al equipo de desarrollo del cliente.

---

**Documento generado:** Enero 2025  
**Versión:** 1.0  
**Estado:** Cliente funcional con servidor mock - Listo para integración con servidor real
