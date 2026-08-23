// :core:model — Kotlin PURO. Nenhum plugin Android aqui, de proposito:
// e o que impede `import android.*` de compilar (ARCHITECTURE.md secao 2, invariante 1).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
