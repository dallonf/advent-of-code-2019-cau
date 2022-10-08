import com.dallonf.ktcause.CodeBundleBuilder
import java.io.File

class CodeLoader {
    val builder = CodeBundleBuilder()

    fun addFile(path: String) {
        val file = File(path)
        val text = file.readText()
        builder.addFile(path, text)
    }

    fun loadDependencies() {
        while (builder.requiredFilePaths.isNotEmpty()) {
            addFile(builder.requiredFilePaths.first())
        }
    }

    fun addFileWithDependencies(path: String) {
        addFile(path)
        loadDependencies()
    }
}