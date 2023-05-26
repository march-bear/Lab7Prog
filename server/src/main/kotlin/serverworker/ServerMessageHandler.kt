package serverworker

import message.*
import message.handler.AbstractMessageHandler
import message.handler.UnexpectedMessageTypeException

class ServerMessageHandler : AbstractMessageHandler() {
    override fun processInfarct(inf: Infarct) {
        throw UnexpectedMessageTypeException("Обработка сообщения типа RequestCase невозможна")
    }

    override fun processRequest(req: Request) {
        TODO("Not yet implemented")
    }

    override fun processResponse(resp: Response) {
        throw UnexpectedMessageTypeException("Обработка сообщения типа Response невозможна")
    }

    override fun processMessageCase(case: MessageCase) {
        val msg = case.message
        when (msg::class.java) {
            Request::class.java -> {

            }
            Response::class.java -> {

            }
            MessageCase::class.java -> {

            }
        }
    }
}