# Documentación del Sistema de Mensajes de Audio

## Descripción General

El sistema de mensajes de audio permite a los usuarios grabar, enviar y reproducir mensajes de voz dentro de la aplicación de chat. Esta funcionalidad está diseñada para trabajar tanto en modo offline (almacenamiento local) como online (sincronización con el servidor).

---

## Arquitectura del Cliente

### 1. **Componentes Principales**

#### 1.1 `GrabadorAudio.java`
**Ubicación:** `src/main/java/org/example/client/comunicacion/GrabadorAudio.java`

**Responsabilidad:** Captura de audio desde el micrófono del usuario.

**Funcionalidades:**
- **Iniciar grabación:** Captura audio en formato WAV (16 kHz, 16 bits, mono)
- **Pausar grabación:** Detiene temporalmente la captura sin perder el audio grabado
- **Continuar grabación:** Reanuda la captura después de una pausa
- **Cancelar grabación:** Descarta el audio capturado
- **Finalizar grabación:** Guarda el audio en un archivo WAV

**Flujo de trabajo:**
\`\`\`
Usuario presiona 🎤 → iniciarGrabacion()
  ↓
Usuario graba audio → captura continua en buffer
  ↓
Usuario pausa → pausarGrabacion() (solo en pausa se puede enviar)
  ↓
Usuario envía → finalizarGrabacion() → guarda archivo WAV
\`\`\`

**Características técnicas:**
- Usa `javax.sound.sampled` para captura de audio
- Formato: PCM 16 kHz, 16 bits, mono
- Calcula duración real excluyendo pausas
- Thread-safe para operaciones concurrentes

---

#### 1.2 `ReproductorAudio.java`
**Ubicación:** `src/main/java/org/example/client/comunicacion/ReproductorAudio.java`

**Responsabilidad:** Reproducción de mensajes de audio recibidos.

**Funcionalidades:**
- **Cargar audio:** Lee archivo WAV desde disco
- **Reproducir:** Inicia la reproducción
- **Pausar:** Detiene temporalmente la reproducción
- **Detener:** Reinicia la reproducción al inicio
- **Verificar estado:** Indica si está reproduciendo

**Características técnicas:**
- Usa `javax.sound.sampled.Clip` para reproducción
- Soporta archivos WAV estándar
- Permite control de reproducción (play/pause/stop)

---

#### 1.3 `PoolReproductoresAudio.java`
**Ubicación:** `src/main/java/org/example/client/comunicacion/PoolReproductoresAudio.java`

**Responsabilidad:** Gestión eficiente de instancias de `ReproductorAudio`.

**Funcionalidades:**
- **Pool de objetos:** Reutiliza reproductores para optimizar recursos
- **Adquirir reproductor:** Obtiene un reproductor disponible o crea uno nuevo
- **Liberar reproductor:** Devuelve el reproductor al pool

**Ventajas:**
- Reduce la creación/destrucción de objetos
- Mejora el rendimiento en chats con múltiples audios
- Limita el número máximo de reproductores activos

---

#### 1.4 `AudioRecorderPanel.java`
**Ubicación:** `src/main/java/org/example/client/ui/AudioRecorderPanel.java`

**Responsabilidad:** Interfaz gráfica para grabar audio.

**Componentes visuales:**
- **Botón Grabar (🎤):** Inicia la grabación
- **Botón Pausar (⏸):** Pausa la grabación
- **Botón Continuar (▶):** Reanuda la grabación
- **Botón Cancelar (✖):** Descarta el audio
- **Botón Enviar (📤):** Finaliza y envía el audio (solo disponible en pausa)
- **Indicador de duración:** Muestra el tiempo grabado en formato MM:SS
- **Indicador de estado:** Muestra el estado actual (grabando, pausado, etc.)

**Flujo de usuario:**
\`\`\`
1. Usuario abre panel de grabación
2. Presiona "Grabar" → comienza captura
3. Puede pausar/continuar múltiples veces
4. Cuando está pausado, puede:
   - Continuar grabando
   - Enviar el audio
   - Cancelar
5. Al enviar, se guarda el archivo y se notifica al listener
\`\`\`

---

#### 1.5 `AudioPlayerPanel.java`
**Ubicación:** `src/main/java/org/example/client/ui/AudioPlayerPanel.java`

**Responsabilidad:** Interfaz gráfica para reproducir audio.

**Componentes visuales:**
- **Botón Play (▶):** Inicia la reproducción
- **Botón Pause (⏸):** Pausa la reproducción
- **Botón Restart (⏮):** Reinicia desde el inicio
- **Barra de progreso:** Muestra el progreso de reproducción
- **Duración:** Muestra la duración total del audio

**Características:**
- Diseño compacto estilo WhatsApp
- Detección automática de fin de reproducción
- Gestión de recursos (cierra el reproductor al terminar)

---

#### 1.6 `MensajeCelda.java`
**Ubicación:** `src/main/java/org/example/client/ui/MensajeCelda.java`

**Responsabilidad:** Renderiza mensajes de texto y audio en el chat.

**Funcionalidades:**
- **Renderizado de texto:** Muestra mensajes de texto en burbujas
- **Renderizado de audio:** Muestra controles de reproducción para audios
- **Diferenciación visual:** Colores diferentes para mensajes propios y recibidos
- **Gestión de recursos:** Limpia reproductores al destruir la celda

**Características visuales:**
- Burbujas verdes para mensajes propios
- Burbujas blancas para mensajes recibidos
- Icono 🎤 para mensajes de audio
- Controles integrados de reproducción

---

### 2. **Modelos de Datos**

#### 2.1 `MensajeAudio.java`
**Ubicación:** `src/main/java/org/example/client/modelo/MensajeAudio.java`

**Propiedades:**
- `rutaArchivo`: Ruta local del archivo de audio
- `duracionSegundos`: Duración del audio en segundos
- `tamanoBytes`: Tamaño del archivo en bytes

**Herencia:** Extiende `Mensaje` (clase base abstracta)

---

#### 2.2 `MensajeAudioPrivado.java`
**Ubicación:** `src/main/java/org/example/client/modelo/MensajeAudioPrivado.java`

**Propiedades adicionales:**
- `receptorCorreo`: Correo del destinatario
- `audioData`: Datos del audio en bytes (para envío por red)

**Uso:** Mensajes de audio entre dos usuarios específicos

---

### 3. **Persistencia Local (H2 Database)**

#### 3.1 Tabla `mensajes`
\`\`\`sql
CREATE TABLE mensajes (
    id VARCHAR(255) PRIMARY KEY,
    remitente_correo VARCHAR(255) NOT NULL,
    destinatario_correo VARCHAR(255),
    id_canal VARCHAR(255),
    contenido TEXT NOT NULL,  -- Para audio: "ruta_archivo|duracion_segundos"
    tipo VARCHAR(50) NOT NULL,  -- "TEXTO", "AUDIO", "PRIVADO"
    fecha_hora TIMESTAMP NOT NULL
)
\`\`\`

#### 3.2 Método `guardarMensajeAudio()`
**Ubicación:** `RepositorioLocal.java`

**Parámetros:**
- `id`: ID único del mensaje
- `remitenteCorreo`: Correo del remitente
- `destinatarioCorreo`: Correo del destinatario
- `idCanal`: ID del canal (null para mensajes privados)
- `rutaArchivo`: Ruta del archivo de audio
- `duracionSegundos`: Duración del audio

**Funcionamiento:**
- Guarda la ruta del archivo y duración en el campo `contenido`
- Marca el tipo como "AUDIO"
- Permite recuperar audios incluso sin conexión al servidor

---

### 4. **Integración con ChatUI**

#### 4.1 Botón de Audio
- Ubicado junto al campo de texto de mensajes
- Icono: 🎤
- Al hacer clic, abre un diálogo modal con `AudioRecorderPanel`

#### 4.2 Envío de Audio
\`\`\`java
1. Usuario graba audio → AudioRecorderPanel
2. Usuario envía → onAudioGrabado() callback
3. Se crea MensajeAudioPrivado con:
   - Archivo de audio
   - Duración
   - Datos en bytes (para envío)
4. Se guarda en base de datos local
5. Se envía al servidor (protocolo AUDIO_PRIVADO)
6. Se agrega a la lista de mensajes en la UI
\`\`\`

#### 4.3 Recepción de Audio
\`\`\`java
1. Servidor envía mensaje de audio
2. GestorComunicacion procesa el mensaje
3. Se guarda el archivo localmente
4. Se guarda en base de datos
5. Se notifica al listener
6. ChatUI actualiza la lista de mensajes
7. MensajeCelda renderiza el audio con controles
\`\`\`

---

## Protocolo de Comunicación Cliente-Servidor

### 1. **Envío de Audio (Cliente → Servidor)**

**Formato del protocolo:**
\`\`\`
AUDIO_PRIVADO|<correo_destinatario>|<duracion_segundos>|<audio_base64>
\`\`\`

**Ejemplo:**
\`\`\`
AUDIO_PRIVADO|usuario2@example.com|15|UklGRiQAAABXQVZFZm10IBAAAAABAAEA...
\`\`\`

**Componentes:**
- `AUDIO_PRIVADO`: Tipo de mensaje
- `correo_destinatario`: Correo del usuario que recibirá el audio
- `duracion_segundos`: Duración del audio en segundos
- `audio_base64`: Datos del archivo WAV codificados en Base64

---

### 2. **Recepción de Audio (Servidor → Cliente)**

**Formato del protocolo:**
\`\`\`
MSG_AUDIO_PRIVADO|<correo_remitente>|<duracion_segundos>|<audio_base64>
\`\`\`

**Ejemplo:**
\`\`\`
MSG_AUDIO_PRIVADO|usuario1@example.com|15|UklGRiQAAABXQVZFZm10IBAAAAABAAEA...
\`\`\`

**Procesamiento en el cliente:**
1. Decodificar Base64 → bytes del audio
2. Guardar archivo WAV en `audios_temp/audio_<timestamp>.wav`
3. Crear `MensajeAudioPrivado` con la ruta del archivo
4. Guardar en base de datos local
5. Notificar a la UI para mostrar el mensaje

---

## Implementación Recomendada en el Servidor

### 1. **Recepción de Audio**

\`\`\`java
// Pseudocódigo del servidor
if (mensaje.startsWith("AUDIO_PRIVADO|")) {
    String[] partes = mensaje.split("\\|", 4);
    String destinatario = partes[1];
    long duracion = Long.parseLong(partes[2]);
    String audioBase64 = partes[3];
    
    // Decodificar audio
    byte[] audioData = Base64.getDecoder().decode(audioBase64);
    
    // Guardar en base de datos del servidor (opcional)
    guardarAudioEnBD(remitente, destinatario, audioData, duracion);
    
    // Reenviar al destinatario si está conectado
    if (clienteConectado(destinatario)) {
        enviarMensaje(destinatario, 
            "MSG_AUDIO_PRIVADO|" + remitente + "|" + duracion + "|" + audioBase64);
    }
}
\`\`\`

### 2. **Almacenamiento en el Servidor**

**Opción 1: Base de datos**
\`\`\`sql
CREATE TABLE mensajes_audio (
    id VARCHAR(255) PRIMARY KEY,
    remitente VARCHAR(255) NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    duracion_segundos BIGINT NOT NULL,
    audio_data BLOB NOT NULL,
    fecha_envio TIMESTAMP NOT NULL
)
\`\`\`

**Opción 2: Sistema de archivos**
\`\`\`
/audios/
  ├── usuario1_usuario2_1234567890.wav
  ├── usuario2_usuario1_1234567891.wav
  └── ...
\`\`\`

### 3. **Sincronización Offline**

Cuando un usuario se reconecta después de estar offline:

\`\`\`java
// Servidor envía audios pendientes
List<MensajeAudio> audiosPendientes = obtenerAudiosPendientes(usuario);
for (MensajeAudio audio : audiosPendientes) {
    enviarMensaje(usuario, 
        "MSG_AUDIO_PRIVADO|" + audio.remitente + "|" + 
        audio.duracion + "|" + audio.audioBase64);
}
\`\`\`

---

## Flujo Completo de Uso

### Escenario: Usuario1 envía audio a Usuario2

\`\`\`
1. Usuario1 abre chat con Usuario2
2. Usuario1 presiona botón 🎤
3. Se abre AudioRecorderPanel
4. Usuario1 presiona "Grabar"
   → GrabadorAudio.iniciarGrabacion()
   → Captura audio del micrófono
5. Usuario1 habla durante 10 segundos
6. Usuario1 presiona "Pausar"
   → GrabadorAudio.pausarGrabacion()
   → Botón "Enviar" se habilita
7. Usuario1 presiona "Enviar"
   → GrabadorAudio.finalizarGrabacion()
   → Guarda archivo: audios_temp/audio_1234567890.wav
   → Crea MensajeAudioPrivado
   → RepositorioLocal.guardarMensajeAudio()
   → GestorComunicacion.enviarMensaje()
   → Envía: AUDIO_PRIVADO|usuario2@example.com|10|<base64>
8. Servidor recibe el mensaje
   → Decodifica Base64
   → Guarda en BD (opcional)
   → Reenvía a Usuario2: MSG_AUDIO_PRIVADO|usuario1@example.com|10|<base64>
9. Usuario2 recibe el mensaje
   → GestorComunicacion procesa MSG_AUDIO_PRIVADO
   → Decodifica Base64 → bytes
   → Guarda archivo local: audios_temp/audio_1234567891.wav
   → RepositorioLocal.guardarMensajeAudio()
   → Notifica a ChatUI
   → MensajeCelda renderiza audio con controles
10. Usuario2 presiona ▶ en el mensaje
    → ReproductorAudio.cargar(archivo)
    → ReproductorAudio.reproducir()
    → Usuario2 escucha el audio
\`\`\`

---

## Ventajas del Diseño

1. **Persistencia Local:** Los audios se guardan en H2, permitiendo acceso offline
2. **Eficiencia:** Pool de reproductores reduce overhead de creación de objetos
3. **Control de Envío:** Solo se puede enviar cuando está pausado, evitando envíos accidentales
4. **Formato Estándar:** WAV es compatible con todas las plataformas
5. **Escalabilidad:** El servidor puede almacenar audios para sincronización posterior
6. **UX Intuitiva:** Controles similares a WhatsApp, familiares para los usuarios

---

## Consideraciones de Seguridad

1. **Validación de Tamaño:** Limitar el tamaño máximo de archivos de audio
2. **Validación de Formato:** Verificar que los archivos sean WAV válidos
3. **Sanitización:** Evitar inyección de código en nombres de archivo
4. **Encriptación:** Considerar encriptar audios en tránsito y en reposo
5. **Permisos:** Verificar permisos de micrófono en el cliente

---

## Mejoras Futuras

1. **Compresión:** Usar MP3 o Opus para reducir tamaño
2. **Streaming:** Enviar audio en chunks para audios largos
3. **Visualización:** Agregar forma de onda del audio
4. **Edición:** Permitir recortar o editar antes de enviar
5. **Transcripción:** Convertir audio a texto automáticamente
6. **Notificaciones:** Alertar cuando llega un nuevo audio

---

## Clases Principales y su Rol

### Clases de Comunicación
- **GrabadorAudio:** Captura de audio desde micrófono
- **ReproductorAudio:** Reproducción de archivos de audio
- **PoolReproductoresAudio:** Gestión eficiente de reproductores
- **GestorComunicacion:** Envío/recepción de mensajes de audio

### Clases de UI
- **AudioRecorderPanel:** Interfaz de grabación
- **AudioPlayerPanel:** Interfaz de reproducción
- **MensajeCelda:** Renderizado de mensajes en el chat
- **ChatUI:** Integración de audio en el chat

### Clases de Modelo
- **MensajeAudio:** Modelo base de mensaje de audio
- **MensajeAudioPrivado:** Mensaje de audio entre dos usuarios

### Clases de Datos
- **RepositorioLocal:** Persistencia en H2 Database

---

## Conclusión

El sistema de mensajes de audio está completamente integrado en el cliente, con soporte para grabación, envío, recepción y reproducción. La arquitectura modular permite fácil mantenimiento y extensión. El servidor debe implementar el protocolo descrito para completar la funcionalidad end-to-end.
