package command.implementations

import IdManager
import collection.CollectionWrapper
import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import iostreamers.Messenger
import iostreamers.TextColor
import organization.Organization

/**
 * Класс команды remove_head вывода первого элемента коллекции и его последующего удаления
 */
class RemoveHeadCommand(
    private val collection: CollectionWrapper<Organization>,
    private val idManager: IdManager,
) : Command {
    private var removedElement: Organization? = null

    override val info: String
        get() = "вывести первый элемент коллекции и удалить его"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        if (collection.isEmpty())
            return CommandResult(
                false,
                Messenger.message("Элемент не может быть удален - коллекция пуста", TextColor.RED)
            )

        removedElement = collection.remove()
        return CommandResult(true, "-------------------------\n" +
                removedElement.toString() +
                "\n-------------------------" +
                Messenger.message("\nЭлемент удален", TextColor.BLUE))
    }

    override fun cancel(): String {
        if (removedElement == null)
            throw CancellationException("Отмена запроса невозможна, так как он ещё не был выполнен или уже был отменен")

        if (!CollectionController.checkUniquenessFullName(removedElement!!.fullName, collection))
            throw CancellationException("Отмена запроса невозможна, так как в коллекции уже есть элемент с таким же полным именем")

        if (!CollectionController.checkUniquenessId(removedElement!!.id, collection)) {
            removedElement!!.id = idManager.generateId()
                ?: throw CancellationException("Отмена запроса невозможна: коллекции переполнена")
        }

        collection.add(removedElement!!)
        removedElement = null

        return "Команда на удаление элемента отменена"
    }
}