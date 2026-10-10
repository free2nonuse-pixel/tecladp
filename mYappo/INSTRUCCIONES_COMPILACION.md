# Instrucciones de Compilación y Generación del APK

## Requisitos
- Android Studio Ladybug (2024.2+) o Hedgehog.
- JDK 17 o superior.
- Dispositivo Android físico o emulador con Android 8.0+ (API 26 a 35).

## Pasos para Compilar

1. **Descomprimir el archivo descargado**:
   Descomprime `LinguaKey-Android-Project.zip` en tu ordenador.

2. **Abrir en Android Studio**:
   - Abre Android Studio.
   - Selecciona **File > Open...** y elige la carpeta descomprimida.
   - Espera a que Gradle sincronice las dependencias automáticamente.

3. **Compilar el APK de depuración (Debug APK)**:
   - Desde la terminal integrada de Android Studio:
     ```bash
     ./gradlew assembleDebug
     ```
   - El archivo APK generado se encontrará en:
     `app/build/outputs/apk/debug/app-debug.apk`

4. **Instalar en tu teléfono**:
   - Conecta tu teléfono mediante cable USB con la depuración USB activada:
     ```bash
     adb install -r app/build/outputs/apk/debug/app-debug.apk
     ```
   - O copia el archivo `app-debug.apk` a tu teléfono y ejecútalo para instalarlo.

5. **Activar el Teclado LinguaKey en Android**:
   - Abre los **Ajustes** de tu teléfono.
   - Ve a **Sistema > Idiomas e introducción de texto > Teclado en pantalla**.
   - Pulsa **Gestionar teclados en pantalla** y activa la casilla de **LinguaKey**.
   - ¡Listo! Ahora al pulsar cualquier campo de texto podrás cambiar a LinguaKey y traducir al instante con el botón ⚡ Traducir.
