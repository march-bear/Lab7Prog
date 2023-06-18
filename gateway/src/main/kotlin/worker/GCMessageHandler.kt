package worker

import AbstractCommandManager
import message.*
import message.handler.AbstractMessageHandler
import message.handler.UnexpectedMessageTypeException

class GCMessageHandler(
    private val service: GatewayLBService,
    private val commandManager: AbstractCommandManager,
) : AbstractMessageHandler() {
    override fun processInfarct(inf: DataBaseChanges) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processRequest(req: Request) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processResponse(resp: Response) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processMessageCase(case: MessageCase) {
        val msg = case.message
        when (msg::class.java) {
            Request::class.java -> {
                msg as Request
                println(msg)
                val command = commandManager.getCommandForUser(msg.name, case.key)
                if (command != null) {
                    command.execute(msg)
                    return
                }
            }
            DataBaseChanges::class.java -> {
                processInfarct(msg as DataBaseChanges)
                return
            }
        }

        if (!service.sendToServer(case)) {
            service.sendToClient(Response(msg.key, false, "Услуги сервиса пока недоступны"), case.key)
        }
    }
}