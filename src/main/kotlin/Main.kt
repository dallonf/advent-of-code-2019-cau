import com.dallonf.ktcause.*
import kotlinx.cli.*
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.relativeTo

fun main(args: Array<String>) {
    AnsiConsole.systemInstall()

    val window = VizWindow()
    window.open()

    val argParser = ArgParser("aoc-cau")
    val day by argParser.argument(ArgType.Int, "day")
    val partsArg by argParser.option(ArgType.String, "part", "p").multiple()
    val rootDirectory by argParser.option(ArgType.String, "rootDirectory", "r")
    val debugVm by argParser.option(ArgType.Boolean, "debugVm", "v")

    argParser.parse(args)

    val parts = partsArg.ifEmpty { listOf("part_one", "part_two") }

    val rootDirPath = rootDirectory?.let {
        Paths.get(it)
    } ?: Paths.get("")

    execute@ while (true) {
        val filepath = run {
            val paddedDay = day.toString().padStart(2, '0')
            "project/puzzles/day${paddedDay}/day${paddedDay}.cau"
        }
        var filesToWatch = setOf(rootDirPath.resolve(filepath))
        try {
            val codeLoader = CodeLoader(rootDirPath)
            codeLoader.addFileWithDependencies(filepath)

            val runner = Runner(LangVm(codeLoader.builder.build(), LangVm.Options(debugInstructionLevelExecution = debugVm ?: false)), rootDirPath)
            filesToWatch = runner.vm.codeBundle.files.keys.mapNotNull { file ->
                if (file.startsWith("core/")) {
                    return@mapNotNull null
                }
                rootDirPath.resolve(file)
            }.toSet()

            val compileErrors = runner.vm.codeBundle.compileErrors
            if (compileErrors.isNotEmpty()) {
                print(Ansi.ansi().fg(Ansi.Color.RED))
                println("Found errors:")
                for (error in compileErrors) {
                    println()
                    println(error.debug())
                    val file = runner.vm.codeBundle.files[error.position.path]!!
                    val source = file.debugCtx?.getSourceContext(error.position.breadcrumbs)
                    if (source != null) {
                        println("```")
                        print(source)
                        println("```")
                    } else {
                        println("Can't show error for ${file.path}")
                    }
                }
                print(Ansi.ansi().reset())
            }

            for (part in parts) {
                println()
                println("${part}:")
                runner.run(filepath, part)
            }
        } catch (e: Throwable) {
            print(Ansi.ansi().fg(Ansi.Color.RED))
            e.printStackTrace()
            print(Ansi.ansi().reset())
        }

        println()

        val watchService = FileSystems.getDefault().newWatchService()
        val pathsToWatch = filesToWatch.map { it.parent }.toSet()
        val watchKeys = pathsToWatch.associateBy { dir ->
            dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )
        }

        while (true) {
            val triggeredKey = watchService.take()
            val path = watchKeys[triggeredKey]!!

            val events = triggeredKey.pollEvents()
            var fileChanged = false
            for (event in events) {
                val changedFile = path.resolve(event.context() as Path)
                if (filesToWatch.contains(changedFile)) {
                    val relativePath = changedFile.relativeTo(rootDirPath)
                    println("Detected change to $relativePath...")
                    fileChanged = true
                }
            }
            if (fileChanged) {
                for (watchKey in watchKeys.keys) {
                    watchKey.cancel()
                }
                continue@execute
            }

            triggeredKey.reset()
        }
    }
}