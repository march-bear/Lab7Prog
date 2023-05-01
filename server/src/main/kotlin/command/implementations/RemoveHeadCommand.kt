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

    override fun cancel(): String {
        if (removedElement == null)
            throw CancellationException("Отмена запроса невозможна, так как он ещё не был выполнен или уже был отменен")

        if (!CollectionController.checkUniquenessFullName(removedElement!!.fullName, collection))
            throw CancellationException("Отмена запроса невозможна, так как в коллекции уже есть элемент с таким же полным именем")

        if (!CollectionController.checkUniquenessId(removedElement!!.id, collection)) {
            throw CancellationException("Отмена запроса невозможна: коллекции переполнена")
        }

        collection.add(removedElement!!)
        removedElement = null

        return "Команда на удаление элемента отменена"
    }
}