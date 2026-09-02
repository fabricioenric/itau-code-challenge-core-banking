package br.com.itau.challenge;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "br.com.itau.challenge";
    private static final String DOMAIN = BASE_PACKAGE + ".hello.domain..";
    private static final String PORT = BASE_PACKAGE + ".hello.port..";
    private static final String APPLICATION = BASE_PACKAGE + ".hello.application..";
    private static final String ADAPTER = BASE_PACKAGE + ".hello.adapter..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    void domainNaoDeveDependerDeNenhumaOutraCamada() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat()
                .resideInAnyPackage(PORT, APPLICATION, ADAPTER);

        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..");

        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeKafka() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.apache.kafka..", "org.springframework.kafka..");

        rule.check(importedClasses);
    }

    @Test
    void domainNaoDeveDependerDeDynamoDb() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN)
                .should().dependOnClassesThat()
                .resideInAnyPackage("software.amazon.awssdk..");

        rule.check(importedClasses);
    }

    @Test
    void portNaoDeveDependerDeAdapter() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(PORT)
                .should().dependOnClassesThat()
                .resideInAnyPackage(ADAPTER);

        rule.check(importedClasses);
    }

    @Test
    void applicationNaoDeveDependerDeAdapter() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat()
                .resideInAnyPackage(ADAPTER);

        rule.check(importedClasses);
    }

    @Test
    void applicationSoDeveDependerDePortEDomain() {
        ArchRule rule = classes()
                .that().resideInAPackage(APPLICATION)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        APPLICATION, PORT, DOMAIN,
                        "java..", "org.springframework..", "org.slf4j..",
                        "org.mockito..", "org.junit.."
                );

        rule.check(importedClasses);
    }

    @Test
    void adapterPodeDependerDePortEDomain() {
        ArchRule rule = classes()
                .that().resideInAPackage(ADAPTER)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        ADAPTER, PORT, DOMAIN,
                        "java..", "org.springframework..", "org.slf4j..",
                        "tools.jackson..", "software.amazon.awssdk..",
                        "org.apache.kafka..", "org.mockito..", "org.junit.."
                );

        rule.check(importedClasses);
    }

    @Test
    void nenhumaCamadaDeveTerDependenciaCiclica() {
        ArchRule rule = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                .matching(BASE_PACKAGE + ".hello.(*)..")
                .should().beFreeOfCycles();

        rule.check(importedClasses);
    }
}