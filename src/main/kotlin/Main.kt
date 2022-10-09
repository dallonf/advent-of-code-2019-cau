import com.dallonf.ktcause.*
import kotlinx.cli.*
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.relativeTo
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val argParser = ArgParser("aoc-cau")
    val day by argParser.argument(ArgType.Int, "day")
    val partsArg by argParser.option(ArgType.String, "part", "p").multiple()
    val rootDirectory by argParser.option(ArgType.String, "rootDirectory", "r")

    argParser.parse(args)

    val parts = partsArg.ifEmpty { listOf("partOne", "partTwo") }

    val rootDirPath = rootDirectory?.let {
        Paths.get(it)
    } ?: Paths.get("")

    execute@ while (true) {
        val codeLoader = CodeLoader(rootDirPath)
        val filepath = run {
            val paddedDay = day.toString().padStart(2, '0')
            "project/puzzles/day${paddedDay}.cau"
        }
        codeLoader.addFileWithDependencies(filepath)

        val runner = Runner(LangVm(codeLoader.builder.build()))

        for (part in parts) {
            println()
            println("${part}:")
            runner.run(filepath, part)
        }

        println()

        val watchService = FileSystems.getDefault().newWatchService()
        val filesToWatch = runner.vm.codeBundle.files.keys.mapNotNull { file ->
            if (file.startsWith("core/")) {
                return@mapNotNull null
            }
            rootDirPath.resolve(file)
        }.toSet()
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