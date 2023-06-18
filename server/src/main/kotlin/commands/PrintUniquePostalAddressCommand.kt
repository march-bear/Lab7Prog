package commands

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import message.Request
import message.Response
import java.util.stream.Collectors

class PrintUniquePostalAddressCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести уникальные значения поля postalAddress всех элементов в коллекции"

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        if (collection.isEmpty()) {
            return CommandResult(Response(req.key, true, "Коллекция пуста"))
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

        return CommandResult(Response(req.key, true, output))
    }
}