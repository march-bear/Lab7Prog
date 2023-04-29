package command.implementations

import IdManager
import collection.CollectionWrapper
import command.*
import exceptions.CancellationException
import organization.Organization

class AddIfMaxCommand(
    private val collection: CollectionWrapper<Organization>,
    private val idManager: IdManager,
) : Command {
    private var newElem: Organization? = null

    override val info: String
        get() = "добавить новый элемент в коллекцию, если его значение " +
                "превышает значение наибольшего элемента этой коллекции"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        val elem = args.organization!!

        if (!CollectionController.checkUniquenessId(elem.id, collection)) {
            elem.id = idManager.generateId()
                ?: return CommandResult(false, "Коллекция переполнена")
            newElem = elem.clone()
        }

        if (collection.isEmpty() || newElem!! > collection.max()) {
            collection.add(newElem!!)
            return CommandResult(true,"Элемент добавлен в коллекцию")
        }

        return CommandResult(true, "Элемент не является максимальным")
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