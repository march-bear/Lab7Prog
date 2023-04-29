package command.implementations

import DataFileManager
import command.*
import exceptions.CancellationException

class SaveCommand(
    private val dataFileManager: DataFileManager,
) : Command {
    override val info: String
        get() = "сохранить коллекцию в файл"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)
        try {
            dataFileManager.saveData()
        } catch (ex: Exception) {
            return CommandResult(
                false,
                "Ошибка во время сохранения коллекции. Сообщение ошибки: $ex",
                false
            )
        }

        return CommandResult(true,"Коллекция сохранена", false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}