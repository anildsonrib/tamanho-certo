plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "br.com.tamanhocerto"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "br.com.tamanhocerto"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        // Sobe para 1.0.0 antes do primeiro envio (SKELETON-SPEC secao 6).
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Permite debug e release convivendo no mesmo aparelho.
            applicationIdSuffix = ".debug"

            // IDs de TESTE oficiais do Google. Nunca testar com o ID real:
            // clique proprio em anuncio de producao e trafego invalido e
            // derruba a conta (ADS-SPEC secao 1).
            buildConfigField(
                "String",
                "ADMOB_APP_ID",
                "\"ca-app-pub-3940256099942544~3347511713\"",
            )
            buildConfigField(
                "String",
                "REWARDED_UNIT_ID",
                "\"ca-app-pub-3940256099942544/5224354917\"",
            )
            // O SDK exige o App ID no manifesto: sem ele o app nem inicia.
            // Declarar o ID NAO inicializa o SDK — a inicializacao continua
            // dentro de requestUnlock (D12).
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }
        release {
            // PLACEHOLDER — IDs reais do AdMob dependem do responsavel
            // (ADS-SPEC secao 1). Enquanto forem estes, o app NAO pode ir
            // para producao: os IDs de teste nao geram receita e a unidade
            // real precisa existir na conta.
            buildConfigField(
                "String",
                "ADMOB_APP_ID",
                "\"ca-app-pub-3940256099942544~3347511713\"",
            )
            buildConfigField(
                "String",
                "REWARDED_UNIT_ID",
                "\"ca-app-pub-3940256099942544/5224354917\"",
            )
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:files"))
    // :core:ads e declarado SOMENTE aqui (ARCHITECTURE.md secao 2, invariante 3).
    implementation(project(":core:ads"))
    implementation(project(":imaging"))
    implementation(project(":pdf"))
    implementation(project(":engine"))
    implementation(project(":feature:tools"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
