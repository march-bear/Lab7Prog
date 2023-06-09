package command

import message.Request

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
     * @param req аргументы, подаваемые с командой, представленные строкой
     */
    fun execute(req: Request): CommandResult
}