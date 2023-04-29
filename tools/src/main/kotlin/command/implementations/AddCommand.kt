package command.implementations

import IdManager
import collection.CollectionWrapper
import command.*
import exceptions.CancellationException
import organization.Organization

class AddCommand(
    private val collection: CollectionWrapper<Organization>,
    private val idManager: IdManager,
) : Command {
    private var newElem: Organization? = null

    override val info: String
        get() = "добавить новый элемент в коллекцию (поля элемента указываются на отдельных строках)"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        val elem = args.organization!!

        if (collection.find { it.fullName == elem.fullName } != null) {
            return CommandResult(false, "Полное имя не уникально")
        } else if (!CollectionController.checkUniquenessId(elem.id, collection)) {
            elem.id = idManager.generateId()
                ?: return CommandResult(false, "Коллекция переполнена")
            newElem = elem.clone()
        }

        collection.add(newElem!!)

        return CommandResult(true, "Элемент добавлен в коллекцию")
    }

    override fun cancel(): String {
        if (newElem == null)
            throw CancellationException("Отмена выполнения невозможна, так как команда ещё не была выполнен или уже была отменен")

        val res = collection.remove(newElem!!)

        if (res)
            return "Команда на добавление элемента отменена"
        else
            throw CancellationException("Отмена выполнения невозможна - добавленный элемент уже был подвергнут изменениям")
    }
}