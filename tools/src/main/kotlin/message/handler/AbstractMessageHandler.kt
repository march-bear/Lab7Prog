package message.handler

import message.*

abstract class AbstractMessageHandler {
    fun process(msg: Message) {
        when (msg::class.java) {
            Infarct::class.java -> processInfarct(msg as Infarct)
            Request::class.java -> processRequest(msg as Request)
            Response::class.java -> processResponse(msg as Response)
            RequestCase::class.java -> processMessageCase(msg as MessageCase)
            else -> throw UnexpectedMessageTypeException("Неожиданный тип сообщения")
        }
    }

    protected abstract fun processInfarct(inf: Infarct)
    protected abstract fun processRequest(req: Request)
    protected abstract fun processResponse(resp: Response)
    protected abstract fun processMessageCase(case: MessageCase)
}