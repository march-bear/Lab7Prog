package commands

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import iostreamers.Messenger
import iostreamers.TextColor
import message.Request
import message.Response
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

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)
        if (collection.isEmpty()) {
            return CommandResult(Response(req.key, true, "Коллекция пуста"))
        }

        var output = ""
        collection.stream()
            .collect(Collectors.groupingBy { it.employeesCount ?: -1 })
            .forEach {
                output += Messenger.message("employeesCount=${if (it.key != -1L) it.key else null}: ", TextColor.DEFAULT)
                output += Messenger.message("${it.value.size}\n", TextColor.BLUE)
            }

        return CommandResult(Response(req.key, true, output))
    }
}