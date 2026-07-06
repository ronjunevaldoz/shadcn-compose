plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.7")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.7")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test-utils:1.23.7")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
