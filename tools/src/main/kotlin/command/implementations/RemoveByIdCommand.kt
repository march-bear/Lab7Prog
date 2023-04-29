package command.implementations

import CollectionController
import IdManager
import collection.CollectionWrapper
import organization.Organization
import command.*
import exceptions.CancellationException

class RemoveByIdCommand(
    private val collection: CollectionWrapper<Organization>,
    private val idManager: IdManager,
) : Command {
    private var removedElement: Organization? = null

    override val info: String
        get() = "удалить элемент из коллекции по его id (id указывается после имени команды)"
    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.LONG))

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        val id: Long = args.primitiveTypeArguments?.get(0)?.toLong() ?: -1

        if (!Organization.idIsValid(id))
            return CommandResult(false, "Введенное значение не является id")

        return try {
            removedElement = collection.stream().filter { it.id == id }.findFirst().get()
            collection.remove(removedElement!!)

            CommandResult(true, "Элемент удален")
        } catch (ex: NullPointerException) {
            CommandResult(false, "Элемент с id $id не найден")
        }
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

        return "Запрос на удаление элемента отменен"
    }
}