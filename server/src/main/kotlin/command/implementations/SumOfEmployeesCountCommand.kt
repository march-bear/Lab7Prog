package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import request.Request
import request.Response

class SumOfEmployeesCountCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести сумму значений поля employeesCount для всех элементов коллекции"

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        if (collection.isEmpty())
            return Response(true, "Коллекция пуста", req.key)

        val sum = collection.stream().mapToLong { it.employeesCount ?: 0 }.sum()

        val output = Messenger.message("Общее количество работников во всех организациях: ") +
                Messenger.message("$sum", TextColor.BLUE)

        return Response(true, output, req.key)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}