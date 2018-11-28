plugins {
    java
}

group = "jimador"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile("com.google.guava", "guava", "27.0.1-jre")
    testCompile("junit", "junit", "4.12")
    testCompile("org.mockito", "mockito-core", "2.21.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}