package command.implementations

import collection.CollectionWrapper
import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import organization.Organization

class ClearCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var oldCollection: CollectionWrapper<Organization>? = null

    override val info: String
        get() = "очистить коллекцию"

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        oldCollection = collection.clone() as CollectionWrapper<Organization>
        collection.clear()

        return CommandResult(true, "Коллекция очищена")
    }

    override fun cancel(): String {
        if (oldCollection == null)
            throw CancellationException("Отмена запроса невозможна, так как он ещё не был выполнен или уже был отменен")
        if (!collection.isEmpty())
            throw CancellationException("Отмена запроса невозможна - коллекция уже была модифицирована")

        collection.addAll(oldCollection!!)
        oldCollection = null

        return "Запрос на очистку коллекции отменен"
    }
}