package commands

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import message.Request
import message.Response

/**
 * Класс команды remove_head вывода первого элемента коллекции и его последующего удаления
 */
class RemoveHeadCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var removedElement: Organization? = null

    override val info: String
        get() = "вывести первый элемент коллекции и удалить его"

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        if (collection.isEmpty())
            return CommandResult(Response(
                req.key, false,
                Messenger.message("Элемент не может быть удален - коллекция пуста", TextColor.RED),
            ))

        removedElement = collection.remove()
        return CommandResult(Response(req.key, true, "-------------------------\n" +
                removedElement.toString() +
                "\n-------------------------" +
                Messenger.message("\nЭлемент удален", TextColor.BLUE)))
    }
}