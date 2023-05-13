package command.implementations

import CollectionController
import collection.CollectionWrapper
import command.Command
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization
import request.Request
import request.Response

/**
 * Класс команды remove_head вывода первого элемента коллекции и его последующего удаления
 */
class RemoveHeadCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var removedElement: Organization? = null

    override val info: String
        get() = "вывести первый элемент коллекции и удалить его"

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        if (collection.isEmpty())
            return Response(
                false,
                Messenger.message("Элемент не может быть удален - коллекция пуста", TextColor.RED),
                req.key
            )

        removedElement = collection.remove()
        return Response(true, "-------------------------\n" +
                removedElement.toString() +
                "\n-------------------------" +
                Messenger.message("\nЭлемент удален", TextColor.BLUE), req.key)
    }
}