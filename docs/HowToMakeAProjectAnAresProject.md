# Ares-Protected Student Exercise Manual

This guide describes the minimal steps to turn a student exercise into an Ares-protected project.

## 1. Add Ares dependencies and agent setup

### Gradle (recommended)

Add an Ares agent configuration and dependency:

```gradle
configurations {
    aresAgent
}

dependencies {
    aresAgent "de.tum.cit.ase:ares:2.0.1-Beta6"
    testImplementation "de.tum.cit.ase:ares:2.0.1-Beta6"
    testImplementation "org.junit.vintage:junit-vintage-engine"
}
```

Make sure the repository lookup checks `mavenLocal()` before `mavenCentral()` when Ares is published locally:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
}
```

Build a proper Java agent JAR with the correct manifest:

```gradle
tasks.register("prepareInstrumentationAgentFiles", Copy) {
    dependsOn configurations.aresAgent
    from configurations.aresAgent.filter { it.name.startsWith("ares-") }
    into layout.buildDirectory.dir("agents")
}

tasks.register("buildInstrumentationAgent", Jar) {
    dependsOn prepareInstrumentationAgentFiles
    archiveFileName = "ares-agent.jar"
    destinationDirectory = layout.buildDirectory.dir("agents")
    from zipTree(layout.buildDirectory.file("agents/ares-2.0.1-Beta6.jar").get().asFile)
    manifest {
        attributes(
            "Manifest-Version": "1.0",
            "Premain-Class": "de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent",
            "Can-Redefine-Classes": "true",
            "Can-Retransform-Classes": "true",
            "Can-Set-Native-Method-Prefix": "true",
            "Boot-Class-Path": "",
            "Main-Class": "de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent",
            "Multi-Release": "true"
        )
    }
}
```

Attach the agent when running tests:

```gradle
test {
    useJUnitPlatform()
    jvmArgs += [
        "-javaagent:${new File(buildDir, "agents/ares-agent.jar").absolutePath}",
        "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-exports", "java.base/java.lang=ALL-UNNAMED"
    ]
    dependsOn buildInstrumentationAgent
}
```

### Maven (alternative)

Add the Ares dependency:

```xml
<dependency>
  <groupId>de.tum.cit.ase</groupId>
  <artifactId>ares</artifactId>
  <version>2.0.1-Beta6</version>
</dependency>
```

Unpack the Ares JAR and rebuild it as a proper agent:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-dependency-plugin</artifactId>
  <executions>
    <execution>
      <id>unpack-ares</id>
      <phase>process-test-classes</phase>
      <goals>
        <goal>unpack</goal>
      </goals>
      <configuration>
        <artifactItems>
          <artifactItem>
            <groupId>de.tum.cit.ase</groupId>
            <artifactId>ares</artifactId>
            <version>2.0.1-Beta6</version>
            <type>jar</type>
            <outputDirectory>${project.build.directory}/agents/ares-unpacked</outputDirectory>
          </artifactItem>
        </artifactItems>
      </configuration>
    </execution>
  </executions>
</plugin>

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <executions>
    <execution>
      <id>build-ares-agent</id>
      <phase>process-test-classes</phase>
      <goals>
        <goal>jar</goal>
      </goals>
      <configuration>
        <classesDirectory>${project.build.directory}/agents/ares-unpacked</classesDirectory>
        <classifier>ares-agent</classifier>
        <archive>
          <manifestEntries>
            <Premain-Class>de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent</Premain-Class>
            <Can-Redefine-Classes>true</Can-Redefine-Classes>
            <Can-Retransform-Classes>true</Can-Retransform-Classes>
            <Can-Set-Native-Method-Prefix>true</Can-Set-Native-Method-Prefix>
            <Boot-Class-Path></Boot-Class-Path>
            <Main-Class>de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent</Main-Class>
            <Multi-Release>true</Multi-Release>
          </manifestEntries>
        </archive>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Attach the agent via surefire:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <argLine>
      -javaagent:target/${project.artifactId}-${project.version}-ares-agent.jar
      --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
      --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
      --add-opens java.base/java.lang=ALL-UNNAMED
      --add-exports java.base/java.lang=ALL-UNNAMED
    </argLine>
  </configuration>
</plugin>
```

## 2. Add a security policy file

Create a policy file in the project root (for example `SecurityConfiguration.yaml`):

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: com.student
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - "com.student.SomeTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: []
    regardingNetworkConnections: []
    regardingCommandExecutions: []
    regardingThreadCreations: []
    regardingPackageImports: []
    regardingTimeouts:
      - timeout: 3000
```

Start restrictive (empty lists) and add explicit permissions only when a course requires them.

## 3. Annotate tests with Ares

Attach Ares to test methods with `@Public` or `@Hidden` and a `@Policy` that points to the policy file.

Gradle compiled classes live under `build/classes/java/main`, so the `withinPath` should start with
`classes/java/main/<package>`.

Example:

```java
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import org.junit.jupiter.api.Test;

public class SomeSecurityTest {

    @Public
    @Policy(
            value = "SecurityConfiguration.yaml",
            withinPath = "classes/java/main/com/student"
    )
    @Test
    void blocksUnauthorizedAccess() {
        // test logic
    }
}
```

For Maven projects, the compiled output is typically `target/classes`, so use
`withinPath = "classes/com/student"`.

## 4. Optional: reset policies between tests

If you mix protected and unprotected tests, reset Ares settings after each test to prevent a
restrictive policy from leaking into later tests. You can call the same reset method used by the
Jupiter security extension.

## 5. Run and verify

Run the test task and confirm that:

- The Ares agent JAR is built.
- Tests annotated with `@Policy` block unauthorized operations.
- Allowed operations still pass when explicitly permitted in the policy.
