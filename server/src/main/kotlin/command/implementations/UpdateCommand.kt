package command.implementations

import collection.CollectionWrapper
import organization.Organization
import command.*
import exceptions.CancellationException
import request.Request
import request.Response

class UpdateCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var oldValue: Organization? = null
    private var newValue: Organization? = null

    override val info: String
        get() = "обновить значение элемента коллекции, id которого равен заданному"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.LONG, ArgumentType.ORGANIZATION))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val id: Long = req.args.primArgs[0].toLong()

        if (!Organization.idIsValid(id))
            return Response(false, "Введенное значение не является id", req.key)

        oldValue = collection.find { it.id == id }
        if (oldValue != null) {
            newValue = req.args.organization!!
            newValue!!.id = id
            collection.replace(oldValue!!, newValue!!.clone())
            return Response(true, "Значение элемента с id $id обновлено", req.key)
        }

        return Response(false, "Элемент с id=$id не найден", req.key)
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