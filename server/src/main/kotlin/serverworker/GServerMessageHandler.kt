package serverworker

import CollectionController
import iostreamers.Messenger
import iostreamers.TextColor
import message.*
import message.handler.AbstractMessageHandler

class GServerMessageHandler(
    private val server: GStreamServerWorker,
    private val cController: CollectionController,
) : AbstractMessageHandler() {
    override fun processInfarct(inf: DataBaseChanges) {
        cController.updateLocalCollection(inf)
    }

    override fun processRequest(req: Request) {
        server.send(Response(req.key, false, "Прямой запрос не может быть обработан, используйте MessageCase"))
    }

    override fun processResponse(resp: Response) {
        Messenger.print(resp.message, if (resp.success) TextColor.BLUE else TextColor.RED)
    }

    override fun processMessageCase(case: MessageCase) {
        val msg = case.message
        var infarct: DataBaseChanges? = null
        val res = when (msg::class.java) {
            Request::class.java -> {
                val commandResult = cController.process(msg as Request)
                infarct = commandResult.inf
                commandResult.resp
            }
            MessageCase::class.java -> {
                Response(msg.key, false, "Получено достижение \"Игра с огнём\"", "breakALeg")
            }
            else -> {
                Response(msg.key, false, "Неожиданный тип сообщения", "breakALeg")
            }
        }

        val resCase = MessageCase(case.key, res)

        if (infarct != null)
            server.send(infarct)
        println("отправлен1")
        server.send(resCase)
        println("отправлен2")
    }
}