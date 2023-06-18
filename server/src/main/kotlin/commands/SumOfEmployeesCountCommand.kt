package commands

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import message.Request
import message.Response

class SumOfEmployeesCountCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести сумму значений поля employeesCount для всех элементов коллекции"

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        if (collection.isEmpty())
            return CommandResult(Response(req.key, true, "Коллекция пуста"))

        val sum = collection.stream().mapToLong { it.employeesCount ?: 0 }.sum()

        val output = Messenger.message("Общее количество работников во всех организациях: ") +
                Messenger.message("$sum", TextColor.BLUE)

        return CommandResult(Response(req.key, true, output))
    }
}