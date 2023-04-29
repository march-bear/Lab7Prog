package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization

class InfoCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести в стандартный поток вывода информацию о коллекции"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        var output = Messenger.message("Информация о коллекции:\n")

        output += Messenger.message("-------------------------\n")

        output += Messenger.message("Тип коллекции: ")
        output += Messenger.message("${collection.getCollectionName()}\n", TextColor.BLUE)

        output += Messenger.message("Дата инициализации: ")
        output += Messenger.message("${collection.initializationDate}\n", TextColor.BLUE)

        output += Messenger.message("Количество элементов: ")
        output += Messenger.message("${collection.size}\n", TextColor.BLUE)

        output += Messenger.message("id максимального элемента: ")
        output += Messenger.message("${if (collection.isEmpty()) "<not found>" else collection.max().id}\n",
            TextColor.BLUE)

        output += Messenger.message("id минимального элемента: ")
        output += Messenger.message("${if (collection.isEmpty()) "<not found>" else collection.min().id}\n",
            TextColor.BLUE)
        output += Messenger.message("-------------------------\n")

        output += Messenger.message("\n\u00a9 ООО \"Мартовский Мишка\". Все права защищены от вас")

        return CommandResult(true, output, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}