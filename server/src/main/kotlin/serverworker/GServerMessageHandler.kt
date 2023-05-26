package serverworker

import CollectionController
import message.*
import message.handler.AbstractMessageHandler
import message.handler.UnexpectedMessageTypeException

class GServerMessageHandler(
    private val server: GStreamServerWorker,
    private val cController: CollectionController,
) : AbstractMessageHandler() {
    override fun processInfarct(inf: Infarct) {
        cController.updateLocalCollection(inf)
    }

    override fun processRequest(req: Request) {
        TODO("Not yet implemented")
    }

    override fun processResponse(resp: Response) {
        throw UnexpectedMessageTypeException("Обработка сообщения типа Response невозможна")
    }

    override fun processMessageCase(case: MessageCase) {
        val msg = case.message
        val res = when (msg::class.java) {
            Request::class.java -> {
                // TODO
                cController.process(msg as Request)
            }
            MessageCase::class.java -> {
                Response(msg.key, false, "Получено достижение \"Игра с огнём\"", "breakALeg")
            }
            else -> {
                Response(msg.key, false, "Неожиданный тип сообщения", "breakALeg")
            }
        }

        val resCase = MessageCase(case.key, res)
    }
}