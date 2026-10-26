# ngsi-ld-java-mapping

## Overview
Library (`io.github.wistefan:ngsi-ld-java-mapping`) that maps NGSI-LD entities to Java POJOs and back. It also ships a Micronaut declarative HTTP client for an NGSI-LD broker, generated from `api/broker-client.yaml`.

## Tech Stack
- Language: Java 17 (`jdk.version`/`release.version`). CI runs JDK 17; the local container has JDK 21.
- Build: Maven (single module, `pom.xml`)
- Framework: Micronaut (DI and declarative HTTP client), Jackson (JSON), Project Reactor (`Mono` in the generated client), Lombok, MapStruct
- Codegen: `openapi-generator-maven-plugin` 6.5.0 with the `micronaut` generator from `io.kokuwa.micronaut:micronaut-openapi-codegen`. It runs in `generate-sources` and writes to `target/generated-sources/openapi` (packages `org.fiware.ngsi.api` and `org.fiware.ngsi.model`, model suffix `VO`).
- Test: JUnit 5 (`junit-jupiter-params`), Mockito. No `micronaut-test`.
- All framework dependencies are `provided` scope: consumers bring their own Micronaut and Jackson.

## Project Structure
```
api/broker-client.yaml                    OpenAPI spec -> generated client + VOs (never edit generated code)
src/main/java/io/github/wistefan/mapping/
  JavaObjectMapper.java                   POJO -> EntityVO
  EntityVOMapper.java                     EntityVO -> POJO (uses EntitiesRepository for relationships)
  EntitiesRepository.java                 thin wrapper over generated client
  CacheSerdeableObjectMapper.java         ObjectMapper subclass with discriminator mixins (for caching)
  EntityVOSerializer.java                 Micronaut ObjectSerializer backed by CacheSerdeableObjectMapper
  EscapeCleaningJsonFactory/Parser.java   @Primary JsonFactory that unescapes reserved-word field names
  AdditionalProperty{Deserializer,TypeResolver,Mixin}.java  custom polymorphic deser of AdditionalPropertyVO
  GeoQueryDeserializer.java               returns empty GeoQueryVO
  ReservedWordHandler.java, MappingProperties.java (@ConfigurationProperties("mapping"))
  annotations/                            @MappingEnabled, @EntityId, @AttributeGetter/Setter, ...
  mixins/                                 Jackson mixins used by CacheSerdeableObjectMapper
src/test/java/io/github/wistefan/mapping/
  ReservedWordHandlerTest.java
  desc/*Test.java                         mapper/serializer tests; build ObjectMappers by hand (no DI)
  desc/pojos/                             test POJOs
.github/workflows/                        test.yaml (mvn clean test jacoco:report), check.yml, release.yaml
```

## Build & Test
```
mvn clean verify          # generate client, compile, run all tests
mvn clean test            # what CI runs (plus jacoco:report)
mvn generate-sources      # just regenerate client into target/generated-sources/openapi
```
Release version comes from the merged PR's semver label (`release.yaml`, `semver-release-action`), not from the `<version>` in the pom.

## Key Conventions
- Indentation: mostly tabs (`EntityVOMapper`, `JavaObjectMapper`, `Escape*`). A few files use 4 spaces (`EntityVOSerializer`, `CacheSerdeableObjectMapper`, `GeoQueryDeserializer`). Match the file you edit.
- Doc density is sparse. Public mapper methods have short Javadoc. Many small classes have one class-level comment or none. Don't add more than the surrounding code has.
- Lombok (`@Slf4j`, `@RequiredArgsConstructor`, ...) is used freely.
- Beans: `jakarta.inject.Singleton`, with `@Bean`/`@Primary` from `io.micronaut.context.annotation`. `EntityVOMapper` and `JavaObjectMapper` still import `javax.inject.Singleton`.
- Dependency versions live as `<version.groupId.artifactId>` properties in the pom.
- Tests: JUnit 5, `@ParameterizedTest` + `@MethodSource` for case tables.

## Important Files
- `pom.xml`: all versions, codegen config (`openapi-ngsi-ld-api` execution), compiler annotation processors (lombok, mapstruct, `micronaut-inject-java`)
- `api/broker-client.yaml`: client spec
- `IMPLEMENTATION_PLAN.md`: current ticket plan (when present)
