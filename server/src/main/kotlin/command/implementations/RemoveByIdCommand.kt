package command.implementations

import CollectionController
import collection.CollectionWrapper
import organization.Organization
import command.*
import exceptions.CancellationException
import request.Request
import request.Response

class RemoveByIdCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var removedElement: Organization? = null

    override val info: String
        get() = "удалить элемент из коллекции по его id (id указывается после имени команды)"
    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.LONG))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val id: Long = req.args.primArgs[0].toLong()

        if (!Organization.idIsValid(id))
            return Response(false, "Введенное значение не является id", req.key)

        return try {
            removedElement = collection.stream().filter { it.id == id }.findFirst().get()
            collection.remove(removedElement!!)

            Response(true, "Элемент удален", req.key)
        } catch (ex: NullPointerException) {
            Response(false, "Элемент с id $id не найден", req.key)
        }
    }
}