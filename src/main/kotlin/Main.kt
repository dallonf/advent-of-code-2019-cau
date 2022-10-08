import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue
import kotlin.system.exitProcess

fun main() {
    val vm = LangVm {
        addFile(
            "project/init.cau", """
                function main() {
                    cause Debug("Hello, Reese!")
                }
            """.trimIndent()
        )
    }

    var executionState = vm.executeFunction("project/init.cau", "main", listOf())
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

            else -> {
                throw Error("Unrecognized signal: ${signal.typeDescriptor.id}")
            }
        }

        executionState = vm.resumeExecution(resultValue)
    }

    executionState.expectReturnValue()
    exitProcess(0)
}