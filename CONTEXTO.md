# Visor de Texto: contexto para IAs

Visor de texto y documentos para Android: gratuito, sin anuncios, privado y de código abierto (AGPL-3.0).

- **Nombre:** Visor de Texto
- **Paquete:** `io.github.cake.visordetexto`
- **Plataforma:** Android
- **Lenguaje:** Kotlin
- **Interfaz:** Jetpack Compose
- **Entorno:** Android Studio

## Reglas obligatorias

1. No declarar permisos de `INTERNET` ni de almacenamiento. Los archivos se abren mediante el selector de documentos del sistema Android.
2. El código del núcleo (paquete `io.github.cake.visordetexto.core`) no debe importar clases de `android.*`. Debe poder probarse mediante pruebas unitarias locales con JUnit.
3. No agregar dependencias sin aprobación. Las dependencias deben tener licencias compatibles con AGPL-3.0.
4. Nunca modificar el archivo original del usuario.
5. La seguridad se comunica como "reducción de riesgo", nunca como garantía absoluta.
6. Entregar código completo por archivo, pruebas correspondientes y lista de supuestos. Ante ambigüedades, formular preguntas en lugar de asumir.
7. No modificar archivos ajenos a la tarea asignada sin autorización.

## Trazabilidad

- `RF-xx`: identificadores de requerimientos funcionales.
- `K-xx`: identificadores de tarjetas de trabajo.
- Las pruebas deben incluir el identificador de la tarjeta correspondiente.

## Criterios de desarrollo

- Priorizar la lectura eficiente de archivos grandes.
- Evitar cargar archivos completos en memoria cuando no sea necesario.
- Mantener separada la interfaz Android de la lógica del núcleo.
- Validar los resultados mediante pruebas reproducibles.
- No considerar una tarea terminada sin evidencia de funcionamiento.

## Organización del equipo

El proyecto es desarrollado por Camilo y Kevin, con asistencia de distintas IA.

Las IA pueden proponer, implementar y revisar código, pero las decisiones de alcance y aceptación corresponden al equipo humano.