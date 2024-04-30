plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.20"
  id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.dbn"
version = "3.4.1710.0"

repositories {
  mavenCentral()
}
dependencies {
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

  implementation("org.projectlombok:lombok:1.18.30")
  implementation("org.dom4j:dom4j:2.1.4")
  implementation("org.apache.poi:poi:5.2.3")
  implementation("org.apache.poi:poi-ooxml:5.2.3")
  implementation("org.apache.poi:poi-ooxml-schemas:4.1.2")
  implementation("com.github.mwiede:jsch:0.2.11")
}

sourceSets{
  main {
    resources {
      srcDir("src/main/java")
      include("**/*.xml")
    }
    resources {
      include(
              "**/*.png",
              "**/*.jpg",
              "**/*.xml",
              "**/*.svg",
              "**/*.css",
              "**/*.html",
              "**/*.template",
              "**/*.properties")
    }
  }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("java"))

}

tasks.register<Zip>("packageDistribution") {
  archiveFileName.set("DBN.zip")
  destinationDirectory.set(layout.buildDirectory.dir("dist"))

  from("lib/ext/") {
    include("**/*.jar")
    into("dbn/lib/ext")
  }
  from(layout.buildDirectory.dir("libs")) {
    include("${project.name}-${project.version}.jar")
    into("dbn/lib")
  }
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
  }
//  withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "11"
//  }

  withType<JavaCompile>{
    copy {
      from("lib/ext")
      include("**/*.jar")
      into(layout.buildDirectory.dir("idea-sandbox/plugins/${project.name}/lib/ext"))
    }
  }

  patchPluginXml {
    sinceBuild.set("200.0001")
//    untilBuild.set("233.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
  runIde {
        systemProperties["idea.auto.reload.plugins"] = true
        systemProperties["fake.services"] = false
        systemProperties["idea.log.debug.categories"] =  "com.dbn.oracleAI, com.dbn.oracleAI.config"
        jvmArgs = listOf(
            "-Xms512m",
            "-Xmx2048m",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044",
        )
   }
}
