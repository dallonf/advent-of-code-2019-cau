import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue

object Utils {
    fun List<RuntimeValue>.toCauseStack(vm: LangVm): RuntimeValue.RuntimeObject {
        val initialEmpty = RuntimeValue.RuntimeObject(
            vm.codeBundle.getType("core/stopgap/collections.cau", "Empty"),
            listOf()
        )
        return this.foldRight(initialEmpty) { item, prev ->
            RuntimeValue.RuntimeObject(
                vm.codeBundle.getType("core/stopgap/collections.cau", "Stack"),
                listOf(
                    item,
                    prev
                )
            )
        }
    }
}