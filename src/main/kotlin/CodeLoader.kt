import com.dallonf.ktcause.CodeBundleBuilder
import java.nio.file.Path

class CodeLoader(private val rootDirectory: Path) {
    val builder = CodeBundleBuilder()

    init {
        addFile("aoc/input.cau")
        addFile("aoc/interface.cau")
        addFile("aoc/draw.cau")
    }

    fun addFile(path: String) {
        val file = rootDirectory.resolve(path).toFile()
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