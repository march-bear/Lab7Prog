package command

data class CommandResult(
    val commandCompleted: Boolean,
    val message: String? = null,
    val archivable: Boolean = true,
)