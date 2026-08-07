# 2. Maven Central Publishing

Part of `kmp-release`. Load this file when working on: 2. maven central publishing.

---

### Plugin setup

```toml
# gradle/libs.versions.toml
[versions]
vanniktech-publish = "0.37.0"

[libraries]
vanniktech-publish-gradlePlugin = { module = "com.vanniktech:gradle-maven-publish-plugin", version.ref = "vanniktech-publish" }

[plugins]
vanniktech-publish = { id = "com.vanniktech.maven.publish", version.ref = "vanniktech-publish" }
# Convention plugin — id matches the precompiled script filename in build-logic
GROUP_ID-library-publish = { id = "GROUP_ID.library.publish", version = "unspecified" }
```

```kotlin
// build-logic/convention/build.gradle.kts
compileOnly(libs.vanniktech.publish.gradlePlugin)
```

```kotlin
// build-logic/convention/src/main/kotlin/GROUP_ID.library.publish.gradle.kts
// Centralized publish convention — apply this in every publishable module.
// Shared POM metadata, signing, and Central Portal target live here once.
// Each module overrides only its own artifactId via mavenPublishing { coordinates() }.
plugins {
    alias(libs.plugins.vanniktech.publish)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set(project.name)
        description.set(project.description ?: project.name)
        url.set("https://github.com/yourhandle/your-repo")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("yourhandle")
                name.set("Your Name")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/yourhandle/your-repo.git")
            developerConnection.set("scm:git:ssh://github.com/yourhandle/your-repo.git")
            url.set("https://github.com/yourhandle/your-repo")
        }
    }
}
```

Each publishable module applies the convention plugin and sets its own coordinates:

```kotlin
// feature/core/build.gradle.kts
plugins {
    alias(libs.plugins.GROUP_ID.library.publish)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.yourhandle",
        artifactId = "your-library-core",
        version = providers.gradleProperty("VERSION").getOrElse(error("VERSION not set")),
    )
}
```

### Credentials via `ORG_GRADLE_PROJECT_*` env vars

Gradle automatically maps `ORG_GRADLE_PROJECT_X` → project property `X`. No `-P` flags needed.

| Env var | Purpose |
|---|---|
| `ORG_GRADLE_PROJECT_mavenCentralUsername` | Sonatype Central Portal username |
| `ORG_GRADLE_PROJECT_mavenCentralPassword` | Sonatype Central Portal password (user token) |
| `ORG_GRADLE_PROJECT_signingInMemoryKey` | ASCII-armored GPG private key |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` | GPG key passphrase |

Store these in your secrets manager of choice and inject them at publish time. Never commit them.

### Publish command

```bash
./gradlew publishAllPublicationsToMavenCentralRepository --no-configuration-cache
```

`--no-configuration-cache` is required — the vanniktech plugin is not configuration-cache compatible as of v0.37.

### Secrets management options

| Approach | When to use |
|---|---|
| **GitHub Secrets** (default) | Simple projects; secrets injected automatically in Actions workflows |
| **Doppler** | Teams already using Doppler; local publish scripts need the same secrets as CI |
| **1Password / AWS Secrets Manager** | Enterprise setups with existing secrets infrastructure |

For GitHub Secrets, add `ORG_GRADLE_PROJECT_*` variables directly in Settings → Secrets → Actions. They are available as env vars in the workflow with no extra configuration.

---

