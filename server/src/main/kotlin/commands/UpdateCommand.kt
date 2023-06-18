package commands

import collection.CollectionWrapper
import organization.Organization
import command.*
import message.Request
import message.Response

class UpdateCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var oldValue: Organization? = null
    private var newValue: Organization? = null

    override val info: String
        get() = "обновить значение элемента коллекции, id которого равен заданному"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.LONG, ArgumentType.ORGANIZATION))

    override fun execute(req: Request): CommandResult {
        argumentValidator.check(req.args)

        val id: Long = req.args.primArgs[0].toLong()

        if (!Organization.idIsValid(id))
            return CommandResult(Response(req.key, false, "Введенное значение не является id"))

        oldValue = collection.find { it.id == id }
        if (oldValue != null) {
            newValue = req.args.organization!!
            newValue!!.id = id
            collection.replace(oldValue!!, newValue!!.clone())
            return CommandResult(Response(req.key, true, "Значение элемента с id $id обновлено"))
        }

        return CommandResult(Response(req.key, false, "Элемент с id=$id не найден"))
    }
}