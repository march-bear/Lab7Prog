package command.implementations

import collection.CollectionWrapper
import organization.Organization
import command.*
import exceptions.CancellationException

class UpdateCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var oldValue: Organization? = null
    private var newValue: Organization? = null

    override val info: String
        get() = "обновить значение элемента коллекции, id которого равен заданному"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.LONG, ArgumentType.ORGANIZATION))

    override fun execute(args: CommandArgument): CommandResult {
        argumentValidator.check(args)

        val id: Long = args.primitiveTypeArguments?.get(0)?.toLong() ?: -1

        if (!Organization.idIsValid(id))
            return CommandResult(false, "Введенное значение не является id")

        oldValue = collection.find { it.id == id }
        if (oldValue != null) {
            newValue = args.organization!!
            newValue!!.id = id
            collection.replace(oldValue!!, newValue!!.clone())
            return CommandResult(true, "Значение элемента с id $id обновлено")
        }

        return CommandResult(false, "Элемент с id=$id не найден")
    }

    override fun cancel(): String {
        if (oldValue == null || newValue == null)
            throw CancellationException("Отмена выполнения невозможна, так как команда ещё не была выполнена или уже была отменена")

        val value = collection.find { it == newValue }
        if (value != null) {
            oldValue!!.id = value.id
            collection.replace(value, oldValue!!)
            oldValue = null
            newValue = null

            return "Команда обновления значения элемента отменена"
        }

        throw CancellationException("Отмена выполнения команды невозможна, так как коллекция уже была модифицирована")
    }
}