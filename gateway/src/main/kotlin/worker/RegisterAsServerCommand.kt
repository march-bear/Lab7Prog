package worker

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import command.CommandResult
import message.Request
import message.Response

class RegisterAsServerCommand(
    private val service: GatewayLBService,
    private val id: String,
) : Command {
    override val info: String = "зарегистрировать подключенного пользователя как сервер"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.LONG, ArgumentType.STRING))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        val lastChange = req.args.primArgs[0].toLong()
        if (service.lastUpdate < 0)
            service.lastUpdate = lastChange

        val userWord = req.args.primArgs[1]
        println("Всё прошло")
        if (service.superSecretWord == userWord) {
            val sender = service.clientsMap[id]?.second ?: throw Exception("Wow")
            service.clientsMap[id] = Pair(SocketType.SERVER, sender)
            service.workersList.add(sender)
            println(service.workersList.toList())
            return CommandResult(Response(req.key, true, "Welcome to Our Dream Team!"))
        }

        return CommandResult(Response(req.key, false, "Ты не знаешь секретное слово, чёрт"))
    }
}