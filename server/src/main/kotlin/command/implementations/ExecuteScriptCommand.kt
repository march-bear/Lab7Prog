package command.implementations

import CommandManager
import command.*
import exceptions.*
import iostreamers.Messenger
import request.Request
import request.Response
import java.util.*


class ExecuteScriptCommand(
    private val commandManager: CommandManager,
) : Command {
    override val info: String
        get() = "считать и исполнить скрипт из указанного файла (название файла указывается на после команды)"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    private var requests: Stack<Command>? = null

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val commands = req.args.script

        var output = ""
        try {
            for (i in commands.indices) {
                val command = commandManager.getCommand(commands[i].first)!!
                val (commandCompleted, message, _) = command.execute(Request("", "", commands[i].second, req.user, req.name))
                if (commandCompleted) {
                    if (message != null) output += Messenger.message(message) + "\n"
                } else
                    throw CommandIsNotCompletedException("Команда не была выполнена. Сообщение о выполнении:\n" +
                            "$message")
            }
        } catch (ex: CommandIsNotCompletedException) {
            cancel()
            return Response(false, "Ошибка во время исполнения скрипта. Сообщение ошибки:\n$ex", req.key)
        } catch (ex: InvalidArgumentsForCommandException) {
            cancel()
            return Response(false, "Ошибка во время исполнения скрипта. Сообщение ошибки:\n$ex", req.key)
        }
        return Response(true, "Скрипт выполнен. Вывод:\n$output", req.key)
    }

    override fun cancel(): String {
        if (requests == null)
            throw CancellationException("Отмена выполнения невозможна, так как команда ещё не была выполнена или уже была отменена")

        val canceledRequests: Stack<Command> = Stack()
        try {
            while (requests!!.isNotEmpty()) {
                canceledRequests.add(requests!!.pop())
                canceledRequests.peek().cancel()
            }
        } catch (ex: CancellationException) {
            canceledRequests.pop()
            while(canceledRequests.isNotEmpty())
                canceledRequests.pop().execute(Request("", ""))
            throw CancellationException("Отмена запроса невозможна. Коллекция уже была модифицирована")
        }

        requests?.clear()
        return "Запрос на исполнение скрипта отменен"
    }
}