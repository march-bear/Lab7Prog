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

/**
 * Класс команды group_counting_by_employees_count для объединения элементов в группы
 * по значению полей employeesCount и вывод количества элементов в каждой из групп
 */
class GroupCountingByEmployeesCountCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    override val info: String
        get() = "сгруппировать элементы коллекции по значению поля employeesCount, " +
                "вывести количество элементов в каждой группе"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        if (collection.isEmpty())
            return CommandResult(true, "Коллекция пуста", false)

        var output = ""
        collection.stream()
            .collect(Collectors.groupingBy { it.employeesCount ?: -1 })
            .forEach {
                output += Messenger.message("employeesCount=${if (it.key != -1L) it.key else null}: ", TextColor.DEFAULT)
                output += Messenger.message("${it.value.size}\n", TextColor.BLUE)
            }

        return CommandResult(true, output, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}