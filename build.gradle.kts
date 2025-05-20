buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-database-hsqldb:11.2.0")
    }
}

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.2.0"
}

group = "com.example.auction"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects") // Needed by spring-retry but for some reason not declared as a dependency
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.hsqldb:hsqldb") // Needs custom transaction manager
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-hsqldb")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(platform("dev.forkhandles:forkhandles-bom:2.22.3.0"))
    implementation("dev.forkhandles:result4k")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.hsqldb:hsqldb")
    testImplementation("com.oneeyedmen:okeydoke:2.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.natpryce:snodge:3.7.0.0")
    testRuntimeOnly("org.glassfish:javax.json:1.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.3")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    inheritSystemProperties()
}

val dbPort = 8999
val auctionPort = 9090
val piiVaultPort = 9091
val settlementPort = 9092
val dbUrl = "jdbc:hsqldb:hsql://localhost:$dbPort/auctions;hsqldb.tx=mvcc"

tasks.create<JavaExec>("runDatabaseServer") {
    group = "run locally"
    mainClass.set("org.hsqldb.Server")
    classpath(sourceSets["main"].runtimeClasspath)
    args = listOf(
        "--address", "localhost",
        "--port", "$dbPort",
        "--database.0", "mem:auctions;hsqldb.tx=mvcc",
        "--dbname.0", "auctions"
    )
}

tasks.create<JavaExec>("runAuctionService") {
    dependsOn("jar")
    group = "run locally"
    mainClass.set("com.example.auction.AuctionApplication")
    classpath(sourceSets["main"].runtimeClasspath)
    workingDir = projectDir
    environment = mapOf(
        "SERVER_PORT" to auctionPort,
        "PII_VAULT_URL" to "http://localhost:$piiVaultPort",
        "SETTLEMENT_URL" to "http://localhost:$settlementPort",
        "SPRING_DATASOURCE_HIKARI_JDBC_URL" to dbUrl
    )
}

tasks.create<JavaExec>("runPiiVaultSimulator") {
    dependsOn("testClasses")
    group = "run locally"
    mainClass.set("com.example.simulators.pii_vault.PiiVaultSimulator")
    classpath(sourceSets["test"].runtimeClasspath)
    workingDir = projectDir
    environment = mapOf(
        "SERVER_PORT" to piiVaultPort,
    )
}

tasks.create<JavaExec>("runSettlementSimulator") {
    dependsOn("testClasses")
    group = "run locally"
    mainClass.set("com.example.simulators.settlement.SettlementSimulator")
    classpath(sourceSets["test"].runtimeClasspath)
    workingDir = projectDir
    environment = mapOf(
        "SERVER_PORT" to settlementPort,
    )
}

flyway {
    workingDirectory = projectDir.absolutePath
    url = dbUrl
    
    /* WARNING: this has to be kept in sync with the locations in the testDataSource.
     * The config cannot be shared because of bugs in the Flyway Gradle task.
     */
    locations = arrayOf("filesystem:${projectDir.resolve("src/main/resources/db/migration").absolutePath}")
}

private fun Test.inheritSystemProperties() {
    systemProperties = System.getProperties().map {
        it.key.toString() to it.value
    }.toMap()
}
