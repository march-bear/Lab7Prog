package command.implementations

import collection.CollectionWrapper
import command.Command
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import request.Request
import request.Response
import java.util.stream.Collectors

class PrintUniquePostalAddressCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести уникальные значения поля postalAddress всех элементов в коллекции"

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        if (collection.isEmpty()) {
            return Response(true, "Коллекция пуста", req.key)
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

        return Response(true, output, req.key)
    }
}