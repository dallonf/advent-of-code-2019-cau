import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RunResult
import com.dallonf.ktcause.RuntimeValue

class Runner(val vm: LangVm) {
    fun run(file: String, part: String) {
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

                else -> {
                    throw Error("Unrecognized signal: ${signal.typeDescriptor.id}")
                }
            }

            executionState = vm.resumeExecution(resultValue)
        }

        val returnVal = executionState.expectReturnValue()

        if (returnVal !is RuntimeValue.Action) {
            println("Result: ${returnVal.debug()}")
        } else {
            println("Done!")
        }
    }
}