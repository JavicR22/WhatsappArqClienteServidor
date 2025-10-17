# Protocolo de Comunicación Cliente-Servidor para Canales

Este documento describe el protocolo de comunicación entre el cliente Java y el servidor para la funcionalidad de canales.

## 1. CREAR CANAL

### Cliente → Servidor
\`\`\`
CREAR_CANAL|nombre|descripcion|privado|correoCreador
\`\`\`

**Parámetros:**
- `nombre`: Nombre del canal (String)
- `descripcion`: Descripción del canal (String, puede estar vacía)
- `privado`: "true" o "false" (String)
- `correoCreador`: Email del usuario que crea el canal (String)

**Ejemplo:**
\`\`\`
CREAR_CANAL|General|Canal para todos|false|usuario@example.com
CREAR_CANAL|Privado VIP|Solo miembros selectos|true|admin@example.com
\`\`\`

### Servidor → Cliente (Respuesta)
\`\`\`
CANAL_CREADO|idCanal|nombre|descripcion|privado|correoCreador|timestamp
\`\`\`

**Parámetros:**
- `idCanal`: ID único generado por el servidor (String/UUID)
- `nombre`: Nombre del canal
- `descripcion`: Descripción del canal
- `privado`: "true" o "false"
- `correoCreador`: Email del creador
- `timestamp`: Timestamp de creación (long)

**Ejemplo:**
\`\`\`
CANAL_CREADO|550e8400-e29b-41d4-a716-446655440000|General|Canal para todos|false|usuario@example.com|1704067200000
\`\`\`

### Servidor → Todos los clientes (Si es canal público)
\`\`\`
NOTIF_CANAL_PUBLICO|idCanal|nombre|descripcion|correoCreador|timestamp
\`\`\`

**Descripción:** Cuando se crea un canal público, el servidor notifica a TODOS los usuarios conectados para que aparezca en su lista de canales disponibles.

---

## 2. INVITAR USUARIOS A CANAL PRIVADO

### Cliente → Servidor
\`\`\`
INVITAR_CANAL|idCanal|nombreCanal|correoInvitador|correo1,correo2,correo3
\`\`\`

**Parámetros:**
- `idCanal`: ID del canal (String)
- `nombreCanal`: Nombre del canal (String)
- `correoInvitador`: Email del usuario que invita (debe ser el creador del canal)
- `correos`: Lista de correos separados por comas (String)

**Ejemplo:**
\`\`\`
INVITAR_CANAL|550e8400-e29b-41d4-a716-446655440000|Privado VIP|admin@example.com|user1@example.com,user2@example.com
\`\`\`

### Servidor → Cliente Invitador (Confirmación)
\`\`\`
INVITACION_ENVIADA|idCanal|cantidadInvitados
\`\`\`

**Ejemplo:**
\`\`\`
INVITACION_ENVIADA|550e8400-e29b-41d4-a716-446655440000|2
\`\`\`

### Servidor → Cada Usuario Invitado
\`\`\`
NOTIF_INVITACION|idInvitacion|idCanal|nombreCanal|correoInvitador|timestamp
\`\`\`

**Parámetros:**
- `idInvitacion`: ID único de la invitación (String/UUID)
- `idCanal`: ID del canal
- `nombreCanal`: Nombre del canal
- `correoInvitador`: Email de quien invita
- `timestamp`: Timestamp de la invitación

**Ejemplo:**
\`\`\`
NOTIF_INVITACION|inv-123|550e8400-e29b-41d4-a716-446655440000|Privado VIP|admin@example.com|1704067200000
\`\`\`

---

## 3. RESPONDER INVITACIÓN

### Cliente → Servidor (Aceptar)
\`\`\`
RESPONDER_INVITACION|idInvitacion|idCanal|correoUsuario|ACEPTAR
\`\`\`

### Cliente → Servidor (Rechazar)
\`\`\`
RESPONDER_INVITACION|idInvitacion|idCanal|correoUsuario|RECHAZAR
\`\`\`

**Parámetros:**
- `idInvitacion`: ID de la invitación
- `idCanal`: ID del canal
- `correoUsuario`: Email del usuario que responde
- `accion`: "ACEPTAR" o "RECHAZAR"

**Ejemplo:**
\`\`\`
RESPONDER_INVITACION|inv-123|550e8400-e29b-41d4-a716-446655440000|user1@example.com|ACEPTAR
\`\`\`

### Servidor → Cliente que respondió
\`\`\`
INVITACION_ACEPTADA|idCanal|nombreCanal
\`\`\`
o
\`\`\`
INVITACION_RECHAZADA|idInvitacion
\`\`\`

### Servidor → Creador del canal (Notificación)
\`\`\`
NOTIF_USUARIO_UNIDO|idCanal|correoUsuario|nombreUsuario
\`\`\`

**Descripción:** Notifica al creador del canal que un usuario aceptó la invitación y se unió.

---

## 4. OBTENER CANALES DEL USUARIO

### Cliente → Servidor
\`\`\`
OBTENER_MIS_CANALES|correoUsuario
\`\`\`

### Servidor → Cliente
\`\`\`
LISTA_CANALES|canal1:nombre1:privado1;canal2:nombre2:privado2;...
\`\`\`

**Formato de cada canal:**
\`\`\`
idCanal:nombre:privado
\`\`\`

**Ejemplo:**
\`\`\`
LISTA_CANALES|550e8400:General:false;660e8400:VIP:true
\`\`\`

---

## 5. ENVIAR MENSAJE A CANAL

### Cliente → Servidor
\`\`\`
CANAL|idCanal|contenido
\`\`\`

**Parámetros:**
- `idCanal`: ID del canal
- `contenido`: Contenido del mensaje

**Ejemplo:**
\`\`\`
CANAL|550e8400-e29b-41d4-a716-446655440000|Hola a todos!
\`\`\`

### Servidor → Todos los miembros del canal
\`\`\`
MSG_TEXT_CANAL|idCanal|nombreCanal|correoRemitente|nombreRemitente|contenido|timestamp
\`\`\`

**Ejemplo:**
\`\`\`
MSG_TEXT_CANAL|550e8400|General|user@example.com|Juan Pérez|Hola a todos!|1704067200000
\`\`\`

---

## 6. VERIFICAR PERMISOS

### Cliente → Servidor (Verificar si es creador)
\`\`\`
VERIFICAR_CREADOR|idCanal|correoUsuario
\`\`\`

### Servidor → Cliente
\`\`\`
ES_CREADOR|idCanal|true
\`\`\`
o
\`\`\`
ES_CREADOR|idCanal|false
\`\`\`

---

## FLUJO COMPLETO DE CASOS DE USO

### Caso 1: Crear Canal Público
1. Usuario A crea canal público "General"
2. Cliente A → Servidor: `CREAR_CANAL|General|Canal para todos|false|userA@example.com`
3. Servidor → Cliente A: `CANAL_CREADO|id123|General|...|false|userA@example.com|...`
4. Servidor → TODOS: `NOTIF_CANAL_PUBLICO|id123|General|...|userA@example.com|...`
5. Todos los clientes agregan el canal a su lista local

### Caso 2: Crear Canal Privado e Invitar
1. Usuario A crea canal privado "VIP"
2. Cliente A → Servidor: `CREAR_CANAL|VIP|Solo VIP|true|userA@example.com`
3. Servidor → Cliente A: `CANAL_CREADO|id456|VIP|...|true|userA@example.com|...`
4. Usuario A invita a Usuario B y C
5. Cliente A → Servidor: `INVITAR_CANAL|id456|VIP|userA@example.com|userB@example.com,userC@example.com`
6. Servidor → Cliente A: `INVITACION_ENVIADA|id456|2`
7. Servidor → Cliente B: `NOTIF_INVITACION|inv1|id456|VIP|userA@example.com|...`
8. Servidor → Cliente C: `NOTIF_INVITACION|inv2|id456|VIP|userA@example.com|...`

### Caso 3: Aceptar Invitación
1. Usuario B recibe invitación
2. Usuario B acepta
3. Cliente B → Servidor: `RESPONDER_INVITACION|inv1|id456|userB@example.com|ACEPTAR`
4. Servidor → Cliente B: `INVITACION_ACEPTADA|id456|VIP`
5. Servidor → Cliente A: `NOTIF_USUARIO_UNIDO|id456|userB@example.com|Usuario B`
6. Cliente B agrega el canal a su lista local

---

## NOTAS IMPORTANTES PARA EL SERVIDOR

### Validaciones que debe hacer el servidor:

1. **Al crear canal:**
   - Verificar que el nombre no esté vacío
   - Generar ID único para el canal
   - Guardar en BD con relación al usuario creador
   - Si es público, notificar a todos los usuarios conectados

2. **Al invitar usuarios:**
   - Verificar que el usuario que invita sea el creador del canal
   - Verificar que el canal sea privado
   - Verificar que los usuarios invitados existan
   - Crear registros de invitación en BD
   - Enviar notificación a cada usuario invitado

3. **Al responder invitación:**
   - Verificar que la invitación exista y esté pendiente
   - Si acepta: agregar usuario como miembro del canal
   - Actualizar estado de invitación en BD
   - Notificar al creador del canal

4. **Al enviar mensaje a canal:**
   - Verificar que el usuario sea miembro del canal
   - Distribuir mensaje a todos los miembros conectados
   - Guardar mensaje en BD para historial

### Estructura de BD sugerida para el servidor:

\`\`\`sql
-- Tabla de canales
CREATE TABLE canales (
    id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    privado BOOLEAN NOT NULL,
    correo_creador VARCHAR(255) NOT NULL,
    creado_en BIGINT NOT NULL
);

-- Tabla de miembros de canales
CREATE TABLE canal_miembros (
    id_canal VARCHAR(255) NOT NULL,
    correo_usuario VARCHAR(255) NOT NULL,
    fecha_union BIGINT NOT NULL,
    PRIMARY KEY (id_canal, correo_usuario),
    FOREIGN KEY (id_canal) REFERENCES canales(id) ON DELETE CASCADE
);

-- Tabla de invitaciones
CREATE TABLE invitaciones (
    id VARCHAR(255) PRIMARY KEY,
    id_canal VARCHAR(255) NOT NULL,
    correo_invitado VARCHAR(255) NOT NULL,
    correo_invitador VARCHAR(255) NOT NULL,
    estado VARCHAR(50) NOT NULL, -- PENDIENTE, ACEPTADA, RECHAZADA
    fecha_invitacion BIGINT NOT NULL,
    FOREIGN KEY (id_canal) REFERENCES canales(id) ON DELETE CASCADE
);

-- Tabla de mensajes de canal
CREATE TABLE mensajes_canal (
    id VARCHAR(255) PRIMARY KEY,
    id_canal VARCHAR(255) NOT NULL,
    correo_remitente VARCHAR(255) NOT NULL,
    contenido TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- TEXTO, AUDIO
    fecha_hora BIGINT NOT NULL,
    FOREIGN KEY (id_canal) REFERENCES canales(id) ON DELETE CASCADE
);
\`\`\`

---

## RESUMEN DE MENSAJES DEL PROTOCOLO

| Dirección | Mensaje | Descripción |
|-----------|---------|-------------|
| C→S | `CREAR_CANAL\|nombre\|desc\|privado\|correo` | Crear nuevo canal |
| S→C | `CANAL_CREADO\|id\|nombre\|desc\|privado\|correo\|ts` | Confirmación de creación |
| S→Todos | `NOTIF_CANAL_PUBLICO\|id\|nombre\|desc\|correo\|ts` | Notificar canal público |
| C→S | `INVITAR_CANAL\|id\|nombre\|invitador\|correos` | Invitar usuarios |
| S→C | `INVITACION_ENVIADA\|id\|cantidad` | Confirmación de invitaciones |
| S→C | `NOTIF_INVITACION\|idInv\|idCanal\|nombre\|invitador\|ts` | Notificar invitación |
| C→S | `RESPONDER_INVITACION\|idInv\|idCanal\|correo\|accion` | Aceptar/rechazar |
| S→C | `INVITACION_ACEPTADA\|idCanal\|nombre` | Confirmación aceptación |
| S→C | `INVITACION_RECHAZADA\|idInv` | Confirmación rechazo |
| S→C | `NOTIF_USUARIO_UNIDO\|idCanal\|correo\|nombre` | Usuario se unió |
| C→S | `OBTENER_MIS_CANALES\|correo` | Solicitar canales |
| S→C | `LISTA_CANALES\|canal1:..;canal2:..` | Lista de canales |
| C→S | `CANAL\|idCanal\|contenido` | Enviar mensaje a canal |
| S→Miembros | `MSG_TEXT_CANAL\|id\|nombre\|correo\|nombre\|msg\|ts` | Mensaje de canal |
| C→S | `VERIFICAR_CREADOR\|idCanal\|correo` | Verificar permisos |
| S→C | `ES_CREADOR\|idCanal\|true/false` | Respuesta permisos |
