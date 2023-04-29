package command.implementations

import collection.*
import command.*
import exceptions.CancellationException
import organization.Organization

class ChangeCollectionTypeCommand(
    private val collection: CollectionWrapper<Organization>,
): Command {
    private var oldType: CollectionType? = null

    override val info: String
        get() = "изменить тип коллекции (QUEUE/SET/LIST)"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.STRING))

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        val type = when (args.primitiveTypeArguments!![0].lowercase()) {
            "queue" -> CollectionType.QUEUE
            "list" -> CollectionType.LIST
            "set" -> CollectionType.SET
            else -> return CommandResult(false, "Заданный тип коллекции не найден")
        }

        val wrapper: CollectionWrapperInterface<Organization> = getWrapperByType(type)
        oldType = collection.getCollectionType()
        collection.replaceCollectionWrapper(wrapper)

        return CommandResult(
            true, "Тип коллекции изменен с $oldType на ${collection.getCollectionType()}",
        )
    }

    override fun cancel(): String {
        if (oldType == null)
            throw CancellationException("Отмена выполнения невозможна, так как команда ещё не была выполнена или уже была отменена")

        val wrapper = getWrapperByType(oldType!!)
        collection.replaceCollectionWrapper(wrapper)

        return "Команда на смену типа коллекции отменена"
    }

    private fun getWrapperByType(type: CollectionType): CollectionWrapperInterface<Organization> {
        return when (type) {
            CollectionType.SET -> LinkedHashSetWrapper()
            CollectionType.QUEUE -> ConcurrentLinkedQueueWrapper()
            CollectionType.LIST -> LinkedListWrapper()
        }
    }
}