// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {

  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply falseplugins {
    // ... outros plugins que já existam aí
    id("com.google.gms.google-services") version "4.4.2" apply false
plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // <-- Adicione esta linha aqui
    // ... outros plugins
dependencies {
    // Importa o Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    
    // Produtos do Firebase que vamos usar
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")


}
