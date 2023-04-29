package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization

class SumOfEmployeesCountCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "вывести сумму значений поля employeesCount для всех элементов коллекции"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        if (collection.isEmpty())
            return CommandResult(true, "Коллекция пуста", false)

        val sum = collection.stream().mapToLong { it.employeesCount ?: 0 }.sum()

        val output = Messenger.message("Общее количество работников во всех организациях: ") +
                Messenger.message("$sum", TextColor.BLUE)

        return CommandResult(true, output, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}