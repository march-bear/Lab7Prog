package commands

import collection.CollectionWrapper
import command.*
import organization.Organization
import message.Request
import message.Response
import java.util.*

/**
 * Класс команды remove_lower для удаления всех элементов коллекции, меньших, чем введенный
 */
class RemoveLowerCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var removedElements: LinkedList<Organization>? = null

    override val info: String
        get() = "удалить из коллекции все элементы, меньшие, чем заданный"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        val element = req.args.organization!!

        var output = ""

        removedElements = LinkedList()

        for (elem in collection.clone()) {
            if (elem < element) {
                collection.remove(elem)
                removedElements!!.add(elem)
                output += "Удален элемент с id ${elem.id}\n"
            }
        }

        if (output != "")
            return CommandResult(Response(req.key, true, output))

        return CommandResult(Response(req.key, true, "В коллекции нет элементов, меньших, чем введенный"))
    }
}