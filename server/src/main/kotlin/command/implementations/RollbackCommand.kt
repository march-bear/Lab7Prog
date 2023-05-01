package command.implementations

import command.*
import commandcallgraph.RequestGraph
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import request.Request
import request.Response

class RollbackCommand(
    private val requestGraph: RequestGraph,
): Command {
    private var oldCurrLeafId: String? = null
    private var currLeafId: String? = null

    override val info: String
        get() = "вернуть коллекцию к состоянию по id запроса. Для полного отката вводится ${RequestGraph.ROOT_NAME}"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val id = req.args.primArgs[0]

        val requestGraph = requestGraph
        oldCurrLeafId = requestGraph.getCurrLeafId()
        return if (requestGraph.rollback(id)) {
            currLeafId = requestGraph.getCurrLeafId()

            Response(true, "Коллекции возвращено состояние $id", req.key)
        } else {
            oldCurrLeafId = null
            Response(false, "Запрос с id $id не найден", req.key)
        }
    }

    override fun cancel(): String {
        if (oldCurrLeafId == null || currLeafId == null)
            throw CancellationException("Отмена запроса невозможна, так как он ещё не был выполнен или уже был отменен")

        if (requestGraph.getCurrLeafId() != currLeafId)
            throw CancellationException("Запрос не может быть отменен, т. к. текущий элемент в графе изменился")

        if (requestGraph.rollback(oldCurrLeafId!!)) {
            currLeafId = null
            oldCurrLeafId = null
            return Messenger.message("Запрос на rollback отменен", TextColor.BLUE)
        } else {
            throw CancellationException("Отмена невозможна - запрос (какого-то черта) не найден")
        }
    }
}