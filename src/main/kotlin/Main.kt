import com.dallonf.ktcause.*
import kotlinx.cli.*
import java.nio.file.Paths
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

    exitProcess(0)
}