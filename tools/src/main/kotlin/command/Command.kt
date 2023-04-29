package command

/**
 * Интерфейс, реализуемый всеми классами команд
 */
interface Command {
    /**
     * Строка с информацией о команде
     */
    val info: String

    /**
     * Список типов аргументов (объектов data-класса ArgumentType)
     */
    val argumentValidator: ArgumentValidator
        get() = ArgumentValidator(listOf())

    /**
     * Исполняет команду
     * @param args аргументы, подаваемые с командой, представленные строкой
     */
    fun execute(args: CommandArgument): CommandResult

    fun cancel(): String
}