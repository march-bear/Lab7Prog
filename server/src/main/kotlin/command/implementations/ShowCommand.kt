package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import exceptions.CancellationException
import organization.Organization
import request.Request
import request.Response

class ShowCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        if (collection.isEmpty()) {
            return Response(true, "Коллекция пуста", req.key)
        }

        var output = "Элементы коллекции:"

        collection.stream().forEach {
            output += "\n------------------------"
            output += "\n" + it.toString()
            output += "\n------------------------"
        }

        return Response(true, output, req.key)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}