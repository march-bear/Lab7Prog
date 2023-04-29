package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import java.util.stream.Collectors

class PrintUniquePostalAddressCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести уникальные значения поля postalAddress всех элементов в коллекции"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        if (collection.isEmpty()) {
            return CommandResult(
                true,
                "Коллекция пуста",
                false
            )
        }

        val setOfAddresses = collection.stream()
            .map { it.employeesCount }
            .filter { it != null }
            .collect(Collectors.toSet())
        var output = if (setOfAddresses.isNotEmpty()) {
            Messenger.message("Уникальные ZIP-коды элементов:")
        } else {
            Messenger.message("Все значения поля postalAddress являются null", TextColor.YELLOW)
        }

        setOfAddresses.forEach {
            output += Messenger.message("\n$it", TextColor.BLUE)
        }

        return CommandResult(true, output, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}