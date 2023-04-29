package command.implementations

import IdManager
import collection.CollectionWrapper
import command.*
import exceptions.CancellationException
import organization.Organization
import java.util.*

/**
 * Класс команды remove_lower для удаления всех элементов коллекции, меньших, чем введенный
 */
class RemoveLowerCommand(
    private val collection: CollectionWrapper<Organization>,
    private val idManager: IdManager,
) : Command {
    private var removedElements: LinkedList<Organization>? = null

    override val info: String
        get() = "удалить из коллекции все элементы, меньшие, чем заданный"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        val element = args.organization!!

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
            return CommandResult(true, output)

        return CommandResult(
            true,
            "В коллекции нет элементов, меньших, чем введенный"
        )
    }

    override fun cancel(): String {
        for (removedElement in removedElements!!) {
            if (!CollectionController.checkUniquenessFullName(removedElement.fullName, collection) ||
                !CollectionController.checkUniquenessId(removedElement.id, collection)
            )
                throw CancellationException(
                    "Отмена команды невозможна, так как в коллекции уже есть элемент с таким же полным именем"
                )

            if (!CollectionController.checkUniquenessId(removedElement.id, collection)) {
                removedElement.id = idManager.generateId()
                    ?: throw CancellationException("Отмена команды невозможна: коллекции переполнена")
            }
        }

        for (removedElement in removedElements!!) {
            collection.add(removedElement)
        }

        removedElements = null

        return "Команда на удаление элементов отменена"
    }
}