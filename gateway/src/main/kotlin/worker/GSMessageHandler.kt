package worker

import message.*
import message.handler.AbstractMessageHandler
import message.handler.UnexpectedMessageTypeException

class GSMessageHandler(
    private val service: GatewayLBService,
) : AbstractMessageHandler() {
    override fun processInfarct(inf: Infarct) {
        for (serv in service.workersList) {
            serv.second.send(inf)
        }

        service.updatingInProgress = false
    }

    override fun processRequest(req: Request) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processResponse(resp: Response) {
        throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
    }

    override fun processMessageCase(case: MessageCase) {

    }
}