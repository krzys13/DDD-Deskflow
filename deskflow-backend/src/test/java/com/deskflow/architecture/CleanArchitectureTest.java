package com.deskflow.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class CleanArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter().importPackages("com.deskflow");
    }

    @Test
    void domain_must_not_depend_on_infrastructure() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        rule.check(classes);
    }

    @Test
    void domain_must_not_depend_on_adapters() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..adapters..");
        rule.check(classes);
    }

    @Test
    void domain_must_not_depend_on_application_service() {
        ArchRule rule = noClasses().that().resideInAPackage("..application.domain..")
                .should().dependOnClassesThat().resideInAPackage("..application.service..");
        rule.check(classes);
    }

    @Test
    void domain_must_not_depend_on_application_port() {
        ArchRule rule = noClasses().that().resideInAPackage("..application.domain..")
                .should().dependOnClassesThat().resideInAPackage("..application.port..");
        rule.check(classes);
    }

    @Test
    void application_must_not_depend_on_infrastructure() {
        ArchRule rule = noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        rule.check(classes);
    }

    @Test
    void application_must_not_depend_on_adapters() {
        ArchRule rule = noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapters..");
        rule.check(classes);
    }

    @Test
    void domain_contexts_have_no_cyclic_dependencies() {
        slices().matching("com.deskflow.(*)..domain..").should().beFreeOfCycles()
                .check(classes);
    }
}
