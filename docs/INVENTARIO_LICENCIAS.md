# Inventario de licencias y dependencias de terceros (K-09)

> **Aviso importante:** Este documento **no constituye asesoría legal**. Es un inventario técnico de **reducción de riesgo** para verificar la trazabilidad y compatibilidad de los componentes de terceros utilizados en **Visor de Texto** respecto a la licencia del proyecto (**GNU Affero General Public License v3.0 — `AGPL-3.0`**).

---

## Tabla de artefactos y licencias

- **Artefactos distintos resueltos por Gradle (A1):** **112** (`103` en `releaseRuntimeClasspath` con alcance `app` + `9` adicionales en `debugUnitTestRuntimeClasspath` con alcance `pruebas`).
- **Plugins de construcción declarados en Gradle (`libs.versions.toml` / `settings.gradle.kts`):** **3** (alcance `construcción`).
- **Filas condicionales:** **1** (`pendiente de K-04b`).
- **Total de filas en esta tabla:** **116**.

| Artefacto (grupo:nombre) | Versión | Alcance | Licencia (identificador SPDX) | Compatibilidad con AGPL-3.0 | Confianza | Fuente |
| --- | --- | --- | --- | --- | --- | --- |
| `androidx.activity:activity` | `1.8.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.activity:activity-compose` | `1.8.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.activity:activity-ktx` | `1.8.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.annotation:annotation` | `1.9.1` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.annotation:annotation-experimental` | `1.4.1` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.annotation:annotation-jvm` | `1.9.1` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.arch.core:core-common` | `2.2.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.arch.core:core-runtime` | `2.2.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.autofill:autofill` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.collection:collection` | `1.5.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.collection:collection-jvm` | `1.5.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.collection:collection-ktx` | `1.5.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose:compose-bom` | `2026.02.01` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.animation:animation` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.animation:animation-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.animation:animation-core` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.animation:animation-core-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.foundation:foundation` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.foundation:foundation-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.foundation:foundation-layout` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.foundation:foundation-layout-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.material:material-ripple` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.material:material-ripple-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.material3:material3` | `1.4.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.material3:material3-android` | `1.4.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-annotation` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-annotation-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-retain` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-retain-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-saveable` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.runtime:runtime-saveable-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-geometry` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-geometry-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-graphics` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-graphics-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-text` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-text-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-tooling-preview` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-tooling-preview-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-unit` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-unit-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-util` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-util-android` | `1.10.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.concurrent:concurrent-futures` | `1.1.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.core:core` | `1.16.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.core:core-ktx` | `1.16.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.core:core-viewtree` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.customview:customview-poolingcontainer` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.documentfile:documentfile` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.dynamicanimation:dynamicanimation` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.emoji2:emoji2` | `1.4.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.graphics:graphics-path` | `1.0.1` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.interpolator:interpolator` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.legacy:legacy-support-core-utils` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-common` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-common-java8` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-common-jvm` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-livedata` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-livedata-core` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-livedata-core-ktx` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-process` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-runtime` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-runtime-android` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-runtime-compose` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-runtime-compose-android` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-runtime-ktx-android` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-viewmodel` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-viewmodel-android` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate-android` | `2.9.4` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.loader:loader` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.localbroadcastmanager:localbroadcastmanager` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.print:print` | `1.0.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.profileinstaller:profileinstaller` | `1.4.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.savedstate:savedstate` | `1.3.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.savedstate:savedstate-android` | `1.3.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.savedstate:savedstate-compose` | `1.3.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.savedstate:savedstate-compose-android` | `1.3.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.savedstate:savedstate-ktx` | `1.3.2` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.startup:startup-runtime` | `1.1.1` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.tracing:tracing` | `1.2.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.transition:transition` | `1.6.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.versionedparcelable:versionedparcelable` | `1.1.1` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.window:window` | `1.5.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.window:window-core` | `1.5.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `androidx.window:window-core-android` | `1.5.0` | app | `Apache-2.0` | compatible | media | Familia AndroidX (sin leer POM individual) |
| `com.google.guava:listenablefuture` | `1.0` | app | `Apache-2.0` | compatible | alta | POM de com.google.guava:listenablefuture:1.0 (Maven Central) |
| `org.jetbrains:annotations` | `23.0.0` | app | `Apache-2.0` | compatible | alta | POM de org.jetbrains:annotations:23.0.0 (Maven Central) |
| `org.jetbrains.kotlin:kotlin-stdlib` | `2.2.10` | app | `Apache-2.0` | compatible | alta | POM de org.jetbrains.kotlin:kotlin-stdlib:2.2.10 (Maven Central) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.9.0` | app | `Apache-2.0` | compatible | media | Familia JetBrains Kotlinx (sin leer POM individual) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-bom` | `1.9.0` | app | `Apache-2.0` | compatible | media | Familia JetBrains Kotlinx (sin leer POM individual) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `1.9.0` | app | `Apache-2.0` | compatible | alta | POM de org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0 (Maven Central) |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm` | `1.9.0` | app | `Apache-2.0` | compatible | media | Familia JetBrains Kotlinx (sin leer POM individual) |
| `org.jetbrains.kotlinx:kotlinx-serialization-bom` | `1.7.3` | app | `Apache-2.0` | compatible | media | Familia JetBrains Kotlinx (sin leer POM individual) |
| `org.jetbrains.kotlinx:kotlinx-serialization-core` | `1.7.3` | app | `Apache-2.0` | compatible | media | Familia JetBrains Kotlinx (sin leer POM individual) |
| `org.jetbrains.kotlinx:kotlinx-serialization-core-jvm` | `1.7.3` | app | `Apache-2.0` | compatible | media | Familia JetBrains Kotlinx (sin leer POM individual) |
| `org.jspecify:jspecify` | `1.0.0` | app | `Apache-2.0` | compatible | alta | POM de org.jspecify:jspecify:1.0.0 (Maven Central) |
| `androidx.compose.material:material` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.material:material-android` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-test-manifest` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-tooling` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-tooling-android` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-tooling-data` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `androidx.compose.ui:ui-tooling-data-android` | `1.10.4` | pruebas | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia AndroidX (sin leer POM individual) |
| `junit:junit` | `4.13.2` | pruebas | `EPL-1.0` | no se distribuye (solo pruebas/construcción) | alta | POM de junit:junit:4.13.2 (https://maven.org) |
| `org.hamcrest:hamcrest-core` | `1.3` | pruebas | `BSD-3-Clause` | no se distribuye (solo pruebas/construcción) | alta | POM de org.hamcrest:hamcrest-core:1.3 (Maven Central / hamcrest-parent:1.3) |
| `com.android.application` | `9.5.1` | construcción | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia Android Gradle Plugin (`gradle/libs.versions.toml`) |
| `org.gradle.toolchains.foojay-resolver-convention` | `1.0.0` | construcción | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia Gradle / Foojay (`settings.gradle.kts`) |
| `org.jetbrains.kotlin.plugin.compose` | `2.2.10` | construcción | `Apache-2.0` | no se distribuye (solo pruebas/construcción) | media | Familia JetBrains Kotlin (`gradle/libs.versions.toml`) |
| `androidx.lifecycle:lifecycle-viewmodel-compose` *(pendiente de K-04b)* | `pendiente de K-04b` | app | `Apache-2.0` | compatible | media | Familia AndroidX (fila condicional pendiente de K-04b) |

---

## (a) Dependencias directas vs. transitivas

- **Directas declaradas en `app/build.gradle.kts` (`implementation` — alcance `app`):**
    - `androidx.compose:compose-bom` (`2026.02.01`)
    - `androidx.activity:activity-compose` (`1.8.0` → resuelta a `1.8.2`)
    - `androidx.compose.material3:material3` (`1.4.0`)
    - `androidx.compose.ui:ui` (`1.10.4`)
    - `androidx.compose.ui:ui-graphics` (`1.10.4`)
    - `androidx.compose.ui:ui-tooling-preview` (`1.10.4`)
    - `androidx.core:core-ktx` (`1.10.1` → resuelta a `1.16.0`)
    - `androidx.lifecycle:lifecycle-runtime-ktx` (`2.6.1` → resuelta a `2.9.4`)
    - Además, `org.jetbrains.kotlin:kotlin-stdlib` (`2.2.10`) se inyecta directamente por el compilador Kotlin/AGP.
- **Directas de pruebas / depuración en `app/build.gradle.kts` (`testImplementation` / `debugImplementation` — alcance `pruebas`):**
    - `junit:junit` (`4.13.2`), `androidx.compose.ui:ui-test-manifest` (`1.10.4`) y `androidx.compose.ui:ui-tooling` (`1.10.4`).
    - *(Nota: `androidTestImplementation` declara `androidx.compose.ui:ui-test-junit4`, `androidx.test.espresso:espresso-core` y `androidx.test.ext:junit`, que no aparecen en `releaseRuntimeClasspath` ni `debugUnitTestRuntimeClasspath`).*
- **Directas de construcción (`libs.versions.toml` / `settings.gradle.kts` — alcance `construcción`):**
    - `com.android.application` (`9.5.1`), `org.jetbrains.kotlin.plugin.compose` (`2.2.10`) y `org.gradle.toolchains.foojay-resolver-convention` (`1.0.0`).
- **Transitivas:**
    - Las **100** dependencias restantes resueltas por Gradle (94 en `releaseRuntimeClasspath` y 6 en `debugUnitTestRuntimeClasspath`, como `kotlinx-coroutines-*`, `kotlinx-serialization-core`, `androidx.lifecycle:*`, `androidx.savedstate:*`, `androidx.window:*`, `org.hamcrest:hamcrest-core`, `org.jspecify:jspecify`, `com.google.guava:listenablefuture`).

---

## (b) Recursos no-código (íconos, fuentes, imágenes) y su origen

- **Fuentes tipográficas externas:** Ninguna declarada ni empaquetada; la interfaz utiliza la tipografía predeterminada de Material 3 / sistema Android.
- **Imágenes y multimedia de terceros:** Ninguna declarada en `app/build.gradle.kts` ni referenciada en el núcleo.
- **Íconos de aplicación (`app/src/main/res`):** Plantilla estándar generada por Android Studio (`ic_launcher` / `ic_launcher_foreground` / `ic_launcher_background` bajo AOSP / `Apache-2.0`). No se inspeccionó el directorio `app/src/main/res` en esta revisión; si se incorporan íconos de Material Icons o recursos gráficos nuevos en futuras tarjetas, deberán registrarse aquí con su origen y licencia.

---

## (c) Filas con confianza media, baja o REVISAR

- **Filas con `REVISAR`:** Ninguna (0).
- **Filas con confianza `baja`:** Ninguna (0).
- **Filas con confianza `media` (109 filas — licencia conocida de la familia AndroidX / JetBrains / Gradle sin leer el archivo POM individual de cada artefacto):**
    1. `androidx.activity:activity` (`1.8.2`) — `Apache-2.0`
    2. `androidx.activity:activity-compose` (`1.8.2`) — `Apache-2.0`
    3. `androidx.activity:activity-ktx` (`1.8.2`) — `Apache-2.0`
    4. `androidx.annotation:annotation` (`1.9.1`) — `Apache-2.0`
    5. `androidx.annotation:annotation-experimental` (`1.4.1`) — `Apache-2.0`
    6. `androidx.annotation:annotation-jvm` (`1.9.1`) — `Apache-2.0`
    7. `androidx.arch.core:core-common` (`2.2.0`) — `Apache-2.0`
    8. `androidx.arch.core:core-runtime` (`2.2.0`) — `Apache-2.0`
    9. `androidx.autofill:autofill` (`1.0.0`) — `Apache-2.0`
    10. `androidx.collection:collection` (`1.5.0`) — `Apache-2.0`
    11. `androidx.collection:collection-jvm` (`1.5.0`) — `Apache-2.0`
    12. `androidx.collection:collection-ktx` (`1.5.0`) — `Apache-2.0`
    13. `androidx.compose:compose-bom` (`2026.02.01`) — `Apache-2.0`
    14. `androidx.compose.animation:animation` (`1.10.4`) — `Apache-2.0`
    15. `androidx.compose.animation:animation-android` (`1.10.4`) — `Apache-2.0`
    16. `androidx.compose.animation:animation-core` (`1.10.4`) — `Apache-2.0`
    17. `androidx.compose.animation:animation-core-android` (`1.10.4`) — `Apache-2.0`
    18. `androidx.compose.foundation:foundation` (`1.10.4`) — `Apache-2.0`
    19. `androidx.compose.foundation:foundation-android` (`1.10.4`) — `Apache-2.0`
    20. `androidx.compose.foundation:foundation-layout` (`1.10.4`) — `Apache-2.0`
    21. `androidx.compose.foundation:foundation-layout-android` (`1.10.4`) — `Apache-2.0`
    22. `androidx.compose.material:material` (`1.10.4`) — `Apache-2.0`
    23. `androidx.compose.material:material-android` (`1.10.4`) — `Apache-2.0`
    24. `androidx.compose.material:material-ripple` (`1.10.4`) — `Apache-2.0`
    25. `androidx.compose.material:material-ripple-android` (`1.10.4`) — `Apache-2.0`
    26. `androidx.compose.material3:material3` (`1.4.0`) — `Apache-2.0`
    27. `androidx.compose.material3:material3-android` (`1.4.0`) — `Apache-2.0`
    28. `androidx.compose.runtime:runtime` (`1.10.4`) — `Apache-2.0`
    29. `androidx.compose.runtime:runtime-android` (`1.10.4`) — `Apache-2.0`
    30. `androidx.compose.runtime:runtime-annotation` (`1.10.4`) — `Apache-2.0`
    31. `androidx.compose.runtime:runtime-annotation-android` (`1.10.4`) — `Apache-2.0`
    32. `androidx.compose.runtime:runtime-retain` (`1.10.4`) — `Apache-2.0`
    33. `androidx.compose.runtime:runtime-retain-android` (`1.10.4`) — `Apache-2.0`
    34. `androidx.compose.runtime:runtime-saveable` (`1.10.4`) — `Apache-2.0`
    35. `androidx.compose.runtime:runtime-saveable-android` (`1.10.4`) — `Apache-2.0`
    36. `androidx.compose.ui:ui` (`1.10.4`) — `Apache-2.0`
    37. `androidx.compose.ui:ui-android` (`1.10.4`) — `Apache-2.0`
    38. `androidx.compose.ui:ui-geometry` (`1.10.4`) — `Apache-2.0`
    39. `androidx.compose.ui:ui-geometry-android` (`1.10.4`) — `Apache-2.0`
    40. `androidx.compose.ui:ui-graphics` (`1.10.4`) — `Apache-2.0`
    41. `androidx.compose.ui:ui-graphics-android` (`1.10.4`) — `Apache-2.0`
    42. `androidx.compose.ui:ui-test-manifest` (`1.10.4`) — `Apache-2.0`
    43. `androidx.compose.ui:ui-text` (`1.10.4`) — `Apache-2.0`
    44. `androidx.compose.ui:ui-text-android` (`1.10.4`) — `Apache-2.0`
    45. `androidx.compose.ui:ui-tooling` (`1.10.4`) — `Apache-2.0`
    46. `androidx.compose.ui:ui-tooling-android` (`1.10.4`) — `Apache-2.0`
    47. `androidx.compose.ui:ui-tooling-data` (`1.10.4`) — `Apache-2.0`
    48. `androidx.compose.ui:ui-tooling-data-android` (`1.10.4`) — `Apache-2.0`
    49. `androidx.compose.ui:ui-tooling-preview` (`1.10.4`) — `Apache-2.0`
    50. `androidx.compose.ui:ui-tooling-preview-android` (`1.10.4`) — `Apache-2.0`
    51. `androidx.compose.ui:ui-unit` (`1.10.4`) — `Apache-2.0`
    52. `androidx.compose.ui:ui-unit-android` (`1.10.4`) — `Apache-2.0`
    53. `androidx.compose.ui:ui-util` (`1.10.4`) — `Apache-2.0`
    54. `androidx.compose.ui:ui-util-android` (`1.10.4`) — `Apache-2.0`
    55. `androidx.concurrent:concurrent-futures` (`1.1.0`) — `Apache-2.0`
    56. `androidx.core:core` (`1.16.0`) — `Apache-2.0`
    57. `androidx.core:core-ktx` (`1.16.0`) — `Apache-2.0`
    58. `androidx.core:core-viewtree` (`1.0.0`) — `Apache-2.0`
    59. `androidx.customview:customview-poolingcontainer` (`1.0.0`) — `Apache-2.0`
    60. `androidx.documentfile:documentfile` (`1.0.0`) — `Apache-2.0`
    61. `androidx.dynamicanimation:dynamicanimation` (`1.0.0`) — `Apache-2.0`
    62. `androidx.emoji2:emoji2` (`1.4.0`) — `Apache-2.0`
    63. `androidx.graphics:graphics-path` (`1.0.1`) — `Apache-2.0`
    64. `androidx.interpolator:interpolator` (`1.0.0`) — `Apache-2.0`
    65. `androidx.legacy:legacy-support-core-utils` (`1.0.0`) — `Apache-2.0`
    66. `androidx.lifecycle:lifecycle-common` (`2.9.4`) — `Apache-2.0`
    67. `androidx.lifecycle:lifecycle-common-java8` (`2.9.4`) — `Apache-2.0`
    68. `androidx.lifecycle:lifecycle-common-jvm` (`2.9.4`) — `Apache-2.0`
    69. `androidx.lifecycle:lifecycle-livedata` (`2.9.4`) — `Apache-2.0`
    70. `androidx.lifecycle:lifecycle-livedata-core` (`2.9.4`) — `Apache-2.0`
    71. `androidx.lifecycle:lifecycle-livedata-core-ktx` (`2.9.4`) — `Apache-2.0`
    72. `androidx.lifecycle:lifecycle-process` (`2.9.4`) — `Apache-2.0`
    73. `androidx.lifecycle:lifecycle-runtime` (`2.9.4`) — `Apache-2.0`
    74. `androidx.lifecycle:lifecycle-runtime-android` (`2.9.4`) — `Apache-2.0`
    75. `androidx.lifecycle:lifecycle-runtime-compose` (`2.9.4`) — `Apache-2.0`
    76. `androidx.lifecycle:lifecycle-runtime-compose-android` (`2.9.4`) — `Apache-2.0`
    77. `androidx.lifecycle:lifecycle-runtime-ktx` (`2.9.4`) — `Apache-2.0`
    78. `androidx.lifecycle:lifecycle-runtime-ktx-android` (`2.9.4`) — `Apache-2.0`
    79. `androidx.lifecycle:lifecycle-viewmodel` (`2.9.4`) — `Apache-2.0`
    80. `androidx.lifecycle:lifecycle-viewmodel-android` (`2.9.4`) — `Apache-2.0`
    81. `androidx.lifecycle:lifecycle-viewmodel-compose` *(pendiente de K-04b)* — `Apache-2.0`
    82. `androidx.lifecycle:lifecycle-viewmodel-ktx` (`2.9.4`) — `Apache-2.0`
    83. `androidx.lifecycle:lifecycle-viewmodel-savedstate` (`2.9.4`) — `Apache-2.0`
    84. `androidx.lifecycle:lifecycle-viewmodel-savedstate-android` (`2.9.4`) — `Apache-2.0`
    85. `androidx.loader:loader` (`1.0.0`) — `Apache-2.0`
    86. `androidx.localbroadcastmanager:localbroadcastmanager` (`1.0.0`) — `Apache-2.0`
    87. `androidx.print:print` (`1.0.0`) — `Apache-2.0`
    88. `androidx.profileinstaller:profileinstaller` (`1.4.0`) — `Apache-2.0`
    89. `androidx.savedstate:savedstate` (`1.3.2`) — `Apache-2.0`
    90. `androidx.savedstate:savedstate-android` (`1.3.2`) — `Apache-2.0`
    91. `androidx.savedstate:savedstate-compose` (`1.3.2`) — `Apache-2.0`
    92. `androidx.savedstate:savedstate-compose-android` (`1.3.2`) — `Apache-2.0`
    93. `androidx.savedstate:savedstate-ktx` (`1.3.2`) — `Apache-2.0`
    94. `androidx.startup:startup-runtime` (`1.1.1`) — `Apache-2.0`
    95. `androidx.tracing:tracing` (`1.2.0`) — `Apache-2.0`
    96. `androidx.transition:transition` (`1.6.0`) — `Apache-2.0`
    97. `androidx.versionedparcelable:versionedparcelable` (`1.1.1`) — `Apache-2.0`
    98. `androidx.window:window` (`1.5.0`) — `Apache-2.0`
    99. `androidx.window:window-core` (`1.5.0`) — `Apache-2.0`
    100. `androidx.window:window-core-android` (`1.5.0`) — `Apache-2.0`
    101. `org.jetbrains.kotlinx:kotlinx-coroutines-android` (`1.9.0`) — `Apache-2.0`
    102. `org.jetbrains.kotlinx:kotlinx-coroutines-bom` (`1.9.0`) — `Apache-2.0`
    103. `org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm` (`1.9.0`) — `Apache-2.0`
    104. `org.jetbrains.kotlinx:kotlinx-serialization-bom` (`1.7.3`) — `Apache-2.0`
    105. `org.jetbrains.kotlinx:kotlinx-serialization-core` (`1.7.3`) — `Apache-2.0`
    106. `org.jetbrains.kotlinx:kotlinx-serialization-core-jvm` (`1.7.3`) — `Apache-2.0`
    107. `com.android.application` (`9.5.1`) — `Apache-2.0`
    108. `org.gradle.toolchains.foojay-resolver-convention` (`1.0.0`) — `Apache-2.0`
    109. `org.jetbrains.kotlin.plugin.compose` (`2.2.10`) — `Apache-2.0`

---

## (d) Supuestos

1. Las entradas marcadas con `(c)` en la salida de Gradle son restricciones de versión (*dependency constraints*) declaradas por BOMs, no artefactos resueltos adicionales.
2. Las dependencias `androidTestImplementation` (`ui-test-junit4`, `espresso-core`, `androidx.test.ext:junit`) no se listan como filas resueltas porque no forman parte de `releaseRuntimeClasspath` ni `debugUnitTestRuntimeClasspath`.
3. Se asigna confianza `alta` únicamente a los 7 artefactos cuyo POM oficial en Maven Central fue leído directamente (`junit`, `hamcrest-core`, `kotlin-stdlib`, `jetbrains:annotations`, `kotlinx-coroutines-core`, `jspecify`, `listenablefuture`).
4. Se asigna confianza `media` al resto de artefactos AndroidX, JetBrains Kotlinx y plugins de Gradle bajo su licencia estándar de familia (`Apache-2.0`), sin haber leído el archivo POM individual de cada módulo.
5. `Apache-2.0` es compatible unidireccionalmente con la distribución del proyecto bajo `AGPL-3.0`.
6. Los artefactos con alcance `pruebas` o `construcción` no se empaquetan dentro del APK distribuido (`release`).