import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import java.math.BigDecimal
import java.nio.file.Path

class Runner(val vm: LangVm, val rootDir: Path) {
    fun run(file: String, part: String) {
        val startTime = System.nanoTime()
        var executionState = vm.executeFunction(file, part, listOf())
        while (executionState is RunResult.Caused) {
            val signal = executionState.signal

            val resultValue = when (signal.typeDescriptor.id) {
                vm.codeBundle.getBuiltinTypeId("Debug") -> {
                    val value = signal.values[0]
                    if (value is RuntimeValue.Text) {
                        println(value.value)
                    } else {
                        println(value.debug())
                    }
                    RuntimeValue.Action
                }

                vm.codeBundle.getTypeId("aoc/input.cau", "NeedInputLines") -> {
                    val path = signal.values[0] as RuntimeValue.Text
                    val inputFile = rootDir.resolve("project/puzzles").resolve(path.value).toFile()
                    val lines = inputFile.readLines()

                    val initialEmpty = RuntimeValue.RuntimeObject(
                        vm.codeBundle.getType("core/stopgap/collections.cau", "Empty"),
                        listOf()
                    )
                    lines.foldRight(initialEmpty) { line, prev ->
                        RuntimeValue.RuntimeObject(
                            vm.codeBundle.getType("core/stopgap/collections.cau", "Stack"),
                            listOf(
                                RuntimeValue.Text(line),
                                prev
                            )
                        )
                    }
                }

                vm.codeBundle.getTypeId("aoc/input.cau", "ParseNumber") -> {
                    val text = signal.values[0] as RuntimeValue.Text
                    val parsed = text.value.toDouble()
                    RuntimeValue.Number(parsed)
                }

                else -> {
                    throw Error("Unrecognized signal: ${signal.typeDescriptor.id}")
                }
            }

            executionState = vm.resumeExecution(resultValue)
        }

        val returnVal = executionState.expectReturnValue()
        val endTime = System.nanoTime()

        val elapsedMs = (endTime - startTime) / 1_000_000

        println("Done in ${elapsedMs}ms")
        if (returnVal !is RuntimeValue.Action) {
            println("Result: ${returnVal.debug()}")
        }
    }
}