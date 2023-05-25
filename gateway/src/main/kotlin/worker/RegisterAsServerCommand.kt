package worker

import command.ArgumentType
import command.ArgumentValidator
import command.Command
import message.Request
import message.Response

class RegisterAsServerCommand(
    private val service: GatewayLBService,
    private val id: String,
) : Command {
    override val info: String = "зарегистрировать подключенного пользователя как сервер"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val userWord = req.args.primArgs[0]
        if (service.superSecretWord == userWord) {
            val (sock, _) = service.clientsMap[id]!!
            service.clientsMap.remove(id)


        }
        return Response(req.key, true, "Welcome to Our Dream Team!")
    }
}