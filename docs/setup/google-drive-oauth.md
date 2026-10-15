# Configuración de Google Drive API y OAuth para NotiFast

Este documento detalla los pasos de configuración externa en Google Cloud Console para habilitar el guardado y sincronización de respaldos en Google Drive (`appDataFolder`).

---

## 1. Habilitar Google Drive API en Google Cloud Console
1. Ingresa a [Google Cloud Console](https://console.cloud.google.com/) con la misma cuenta del proyecto Firebase de NotiFast.
2. Selecciona el proyecto NotiFast.
3. Ve a **APIs & Services** > **Library** (Biblioteca de APIs).
4. Busca **Google Drive API** y haz clic en **Enable** (Habilitar).

---

## 2. Configurar la Pantalla de Consentimiento OAuth
1. En **APIs & Services** > **OAuth consent screen** (Pantalla de consentimiento de OAuth):
   - Tipo de usuario: **External** (Externo).
   - Datos básicos: Nombre de la app (`NotiFast`), correo de asistencia y desarrollador.
2. En la sección **Scopes** (Permisos):
   - Haz clic en **Add or Remove Scopes**.
   - Añade el scope: `https://www.googleapis.com/auth/drive.appdata` (Ver y administrar los datos de configuración de la app en Google Drive).
   - *Nota:* Este scope solo da acceso a la carpeta oculta de la app (`appDataFolder`), garantizando máxima privacidad (la app no puede ver fotos, documentos ni otros archivos personales del usuario).
3. En **Test users** (Usuarios de prueba):
   - Añade los correos de Google de los desarrolladores/testers que probarán la función mientras la app esté en modo prueba.

---

## 3. Crear Credenciales de Cliente OAuth 2.0
En **APIs & Services** > **Credentials** (Credenciales):

### A. Cliente Android (para autorización y Play Services)
- Tipo: **Android**
- Nombre: `NotiFast Android (Debug)`
- Package name: `com.notifast.app`
- SHA-1 fingerprint: El SHA-1 de tu keystore de depuración (obtenido mediante `./gradlew signingReport` o `keytool`).
- *Para release:* Crear otro cliente con el SHA-1 de producción o de Google Play App Signing.

### B. Cliente Web (para Credential Manager `serverClientId`)
- Tipo: **Web application** (Aplicación web)
- Nombre: `NotiFast Web Client (Auth)`
- Copia el **Client ID** generado (termina en `.apps.googleusercontent.com`).

---

## 4. Configuración Local en el Proyecto
En la raíz del proyecto, añade el Client ID Web a tu archivo `local.properties` (que está ignorado en Git):

```properties
google.webClientId=TU_CLIENT_ID_WEB.apps.googleusercontent.com
```

El build system inyectará automáticamente este valor en `BuildConfig.GOOGLE_WEB_CLIENT_ID` durante la compilación.
