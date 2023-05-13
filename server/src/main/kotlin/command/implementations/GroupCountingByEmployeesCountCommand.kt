package command.implementations

import collection.CollectionWrapper
import command.Command
import db.DataBaseManager
import db.checkToken
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import request.Request
import request.Response
import java.util.concurrent.Executors
import java.util.stream.Collectors

/**
 * Класс команды group_counting_by_employees_count для объединения элементов в группы
 * по значению полей employeesCount и вывод количества элементов в каждой из групп
 */
class GroupCountingByEmployeesCountCommand(
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "сгруппировать элементы коллекции по значению поля employeesCount, " +
                "вывести количество элементов в каждой группе"

    private val statGroupByEmpCount = dbManager.connection.prepareStatement(
        "SELECT employees_count, COUNT(id) FROM ORGANIZATIONS GROUP BY employees_count"
    )

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)
        if (!dbManager.checkToken(req.token))
            return Response(false, "Токен невалиден", req.key, "identify")

        var output = ""

        val res = statGroupByEmpCount.executeQuery()

        while (res.next()) {
            output += "employees_count=${res.getLong(1)}: ${res.getInt(2)}\n"
        }

        return Response(true, if (output != "") output else "Коллекция пуста", req.key)
    }
}