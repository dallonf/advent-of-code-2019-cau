import com.dallonf.ktcause.Debug.debug
import com.dallonf.ktcause.LangVm
import com.dallonf.ktcause.RuntimeValue
import com.dallonf.ktcause.types.InstanceValueLangType

object Utils {
    fun List<RuntimeValue>.toCauseStack(vm: LangVm): RuntimeValue.RuntimeObject {
        val initialEmpty = RuntimeValue.RuntimeObject(
            vm.codeBundle.getType("core/stopgap/collections.cau", "Empty"), listOf()
        )
        return this.foldRight(initialEmpty) { item, prev ->
            RuntimeValue.RuntimeObject(
                vm.codeBundle.getType("core/stopgap/collections.cau", "Stack"), listOf(
                    item, prev
                )
            )
        }
    }

    fun getTypeName(value: RuntimeValue): String {
        val typeDescriptor = (value as? RuntimeValue.RuntimeObject)?.typeDescriptor
            ?: ((value as? RuntimeValue.RuntimeTypeConstraint)?.valueType as? InstanceValueLangType)?.canonicalType

        return typeDescriptor?.id?.name ?: throw IllegalArgumentException("Couldn't find the name of ${value.debug()}")
    }
}