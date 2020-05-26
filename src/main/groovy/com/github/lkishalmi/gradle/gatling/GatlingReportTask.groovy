package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec

import java.nio.file.Path
import java.nio.file.Paths

class GatlingReportTask extends DefaultTask implements JvmConfigurable {

    @OutputDirectory
    File gatlingReportDir = project.file("${project.reportsDir}/gatling")

    @Option(option = 'l', description = 'Command line argument `--l`. Generates the reports for the simulation log files located in ${project.reportsDir}/<folderName>.')
    String logDirName = ""


    @InputDirectory
    File getGatlingLogDirFile() {
        return project.file("${project.reportsDir}/${this.logDirName}")
    }

    List<String> createGatlingArgs() {
        List<String> args = [
            "-rf", gatlingReportDir.absolutePath,
            "-ro", getGatlingLogDirFile().absolutePath
        ]
        return args
    }

    @TaskAction
    void gatlingReport() {
        def gatlingExt = project.extensions.getByType(GatlingPluginExtension)

        ExecResult result = project.javaexec({ JavaExecSpec exec ->
            exec.main = GatlingPluginExtension.GATLING_MAIN_CLASS
            exec.classpath = project.configurations.gatlingRuntimeClasspath

            exec.jvmArgs this.jvmArgs ?: gatlingExt.jvmArgs
            exec.systemProperties System.properties
            exec.systemProperties this.systemProperties ?: gatlingExt.systemProperties

            exec.args this.createGatlingArgs()

            exec.standardInput = System.in

            exec.ignoreExitValue = true
        } as Action<JavaExecSpec>)

        if (result.exitValue != 0) {
            throw new TaskExecutionException(this, new RuntimeException("There's failed exec: ${result}"))
        }
    }
}
