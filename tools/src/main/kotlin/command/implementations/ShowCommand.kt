package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import organization.Organization

class ShowCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)
        if (collection.isEmpty()) {
            return CommandResult(true, "Коллекция пуста", false)
        }

        var output = "Элементы коллекции:"

        collection.stream().forEach {
            output += "\n------------------------"
            output += "\n" + it.toString()
            output += "\n------------------------"
        }

        return CommandResult(true, output, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}