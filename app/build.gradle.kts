import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    jacoco
}

android {
    namespace = "com.cabral.lucrovarejo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.cabral.lucrovarejo"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "2.0"

        testInstrumentationRunner = "com.cabral.lucrovarejo.HiltTestRunner"
    }

    // Credenciais e keystore em /keystore (fora do git). Sem o arquivo, o release fica sem assinatura.
    val keystoreDir = rootProject.file("keystore")
    val keystoreProperties = Properties().apply {
        val file = File(keystoreDir, "keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
    val releaseSigning = if (keystoreProperties.containsKey("storePassword")) {
        signingConfigs.create("release") {
            storeFile = File(keystoreDir, keystoreProperties.getProperty("storeFile", "lucro_varejo"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    } else {
        null
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            buildConfigField("String", "STORES_COLLECTION", "\"stores_debug\"")
            buildConfigField("String", "USERS_COLLECTION", "\"users_debug\"")
        }
        release {
            buildConfigField("String", "STORES_COLLECTION", "\"stores\"")
            buildConfigField("String", "USERS_COLLECTION", "\"users\"")
            signingConfig = releaseSigning
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
//    testOptions {
//        unitTests.isIncludeAndroidResources = true
//        unitTests.all {
//            it.extensions.configure(JacocoTaskExtension::class.java) {
//                // Robolectric's android-all classes are already instrumented; JaCoCo must not touch them
//                isIncludeNoLocationClasses = false
//                excludes = listOf("jdk.internal.*", "android.*", "androidx.*", "com.android.*", "org.robolectric.*", "sun.*")
//            }
//        }
//    }

    //RoboEletric
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-noverify")
                it.extensions.configure(JacocoTaskExtension::class.java) {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.hilt.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    detektPlugins(libs.detekt.compose.rules)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    source.setFrom("src/main/java", "src/test/java", "src/androidTest/java")
}