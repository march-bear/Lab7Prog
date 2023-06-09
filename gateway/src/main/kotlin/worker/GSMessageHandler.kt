package worker

import message.*
import message.handler.AbstractMessageHandler
import message.handler.UnexpectedMessageTypeException

class GSMessageHandler(
    private val service: GatewayLBService,
) : AbstractMessageHandler() {
    override fun processInfarct(inf: DataBaseChanges) {
        service.sendToAllServers(inf)

        service.updatingInProgress = false
        println("Отправка уведомлений осуществлена")
    }

    override fun processRequest(req: Request) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processResponse(resp: Response) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processMessageCase(case: MessageCase) {
        service.sendToClient(case.message, case.key)
    }
}