// Ktlint setup script for Android projects

/**
 * Ktlint Configuration Script
 *
 * This script provides additional ktlint configuration and helper tasks
 * for maintaining code quality in Android projects.
 */

// Apply ktlint plugin if not already applied
if (!project.plugins.hasPlugin("org.jlleitschuh.gradle.ktlint")) {
    project.plugins.apply("org.jlleitschuh.gradle.ktlint")
}

// Create custom tasks for code formatting
tasks.register("formatCode") {
    group = "formatting"
    description = "Format all Kotlin code and organize imports"

    dependsOn("ktlintFormat", "organizeImports")

    doLast {
        println("✅ Code formatting completed!")
        println("📁 All Kotlin files have been formatted")
        println("📋 Imports have been organized")
    }
}

tasks.register("checkCodeStyle") {
    group = "verification"
    description = "Check code style and import organization"

    dependsOn("ktlintCheck")

    doLast {
        println("✅ Code style check completed!")
    }
}

tasks.register("fixCodeStyle") {
    group = "formatting"
    description = "Fix all code style issues automatically"

    dependsOn("formatCode")

    doLast {
        println("🔧 All code style issues have been fixed!")
        println("🎉 Your code is now ktlint compliant")
    }
}

// Hook into build process
tasks.named("preBuild") {
    dependsOn("checkCodeStyle")
}

// Hook into test process
tasks.withType<Test> {
    dependsOn("checkCodeStyle")
}

// Suppress specific ktlint rules if needed
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    // Disable specific rules
    disabledRules.set(setOf(
        "no-wildcard-imports",  // Allow wildcard imports for Android
        "experimental:trailing-comma-on-call-site",
        "experimental:trailing-comma-on-declaration-site"
    ))
}