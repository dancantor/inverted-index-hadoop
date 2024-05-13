
plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = "hpc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("org.apache.hadoop:hadoop-common:3.3.5")
    implementation ("org.apache.hadoop:hadoop-mapreduce-client-core:3.3.5")
}

tasks.test {
    useJUnitPlatform()
}
