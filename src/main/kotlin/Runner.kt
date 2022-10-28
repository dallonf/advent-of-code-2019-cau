import Utils.toCauseStack
import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import org.fusesource.jansi.Ansi
import java.nio.file.Path

class Runner(val vm: LangVm, val rootDir: Path, val window: VizWindow) {
    fun run(file: String, part: String) {
        val startTime = System.nanoTime()
        var executionState = vm.executeFunction(file, part, listOf())

        fun getInputFile(path: String) = rootDir.resolve("project/puzzles").resolve(path).toFile()

        var drawQueue = mutableListOf<RuntimeValue.RuntimeObject>()

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

                vm.codeBundle.getBuiltinTypeId("AssumptionBroken") -> {
                    val value = signal.values[0] as RuntimeValue.Text
                    println(
                        Ansi.ansi().fg(Ansi.Color.RED).render(vm.getExecutionTrace())
                            .render("Assumption broken: " + value.value).reset()
                    )
                    break
                }

                vm.codeBundle.getTypeId("aoc/input.cau", "NeedInput") -> {
                    val path = signal.values[0] as RuntimeValue.Text
                    val inputFile = getInputFile(path.value)
                    val text = inputFile.readText()

                    RuntimeValue.Text(text)
                }

                vm.codeBundle.getTypeId("aoc/input.cau", "NeedInputLines") -> {
                    val path = signal.values[0] as RuntimeValue.Text
                    val inputFile = rootDir.resolve("project/puzzles").resolve(path.value).toFile()
                    val lines = inputFile.readLines()

                    lines.map { RuntimeValue.Text(it) }.toCauseStack(vm)
                }

                vm.codeBundle.getTypeId("aoc/input.cau", "ParseNumber") -> {
                    val text = signal.values[0] as RuntimeValue.Text
                    val parsed = text.value.toDouble()
                    RuntimeValue.Number(parsed)
                }

                vm.codeBundle.getTypeId("aoc/input.cau", "Split") -> {
                    val text = signal.values[0] as RuntimeValue.Text
                    val separator = signal.values[1] as RuntimeValue.Text
                    val result = text.value.split(separator.value)

                    result.map { RuntimeValue.Text(it) }.toCauseStack(vm)
                }

                vm.codeBundle.getTypeId("aoc/interface.cau", "ReportProgress") -> {
                    val message = signal.values[0] as RuntimeValue.Text

                    println(message.value)
                    vm.reportTick()
                    RuntimeValue.Action
                }

                vm.codeBundle.getTypeId("aoc/draw.cau", "Draw") -> {
                    val drawable = signal.values[0].validate() as RuntimeValue.RuntimeObject
                    drawQueue.add(drawable)
                    RuntimeValue.Action
                }

                vm.codeBundle.getTypeId("aoc/draw.cau", "FrameDone") -> {
                    window.drawables = drawQueue.toList()
                    drawQueue.clear()
                    RuntimeValue.Action
                }

                else -> {
                    throw Error(vm.getExecutionTrace() + "Unrecognized signal: ${signal.typeDescriptor.id}")
                }
            }

            executionState = vm.resumeExecution(resultValue)
        }
        val returnVal = (executionState as? RunResult.Returned)?.returnValue

        val endTime = System.nanoTime()

        val elapsedMs = (endTime - startTime) / 1_000_000

        println("Done in ${elapsedMs}ms")
        if (returnVal != null && returnVal !is RuntimeValue.Action) {
            println("Result: ${returnVal.debug()}")
        }
    }
}