package worker

import AbstractCommandManager
import message.*
import message.handler.AbstractMessageHandler
import message.handler.UnexpectedMessageTypeException

class GCMessageHandler(
    private val service: GatewayLBService,
    private val commandManager: AbstractCommandManager,
) : AbstractMessageHandler() {
    override fun processInfarct(inf: Infarct) {
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
                val command = commandManager.getCommandForUser(msg.key, case.key)
                if (command != null) {
                    command.execute(msg)
                    return
                }
            }
            Infarct::class.java -> {
                processInfarct(msg as Infarct)
                return
            }
        }


    }
}