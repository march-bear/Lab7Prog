package command.implementations

import collection.CollectionWrapper
import command.*
import db.DataBaseManager
import exceptions.CancellationException
import organization.Organization
import request.Request
import request.Response

class AddCommand(
    private val collection: CollectionWrapper<Organization>,
    private val dbManager: DataBaseManager
) : Command {
    private var newElem: Organization? = null

    override val info: String
        get() = "добавить новый элемент в коллекцию (поля элемента указываются на отдельных строках)"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val elem: Organization = req.args.organization!!

        if (collection.stream().filter { it.fullName == elem.fullName } != null) {
            return Response(false, "Полное имя не уникально", req.key)
        }

        val statCoordInsert = dbManager.connection.prepareStatement(
            "INSERT INTO COORDINATES(x, y) VALUES (?, ?) ON CONFLICT DO NOTHING "
        )
        statCoordInsert.setDouble(0, elem.coordinates.x)
        statCoordInsert.setInt(1, elem.coordinates.y)
        statCoordInsert.executeUpdate()

        val statCoordId = dbManager.connection.prepareStatement("SELECT id FROM COORDINATES WHERE x = ? AND y = ?")

        statCoordId.setDouble(0, elem.coordinates.x)
        statCoordId.setInt(1, elem.coordinates.y)
        val coordIds = statCoordId.executeQuery()
        val coordId = if (coordIds.next())
            coordIds.getInt(0)
        else
            throw Exception()

        newElem = elem.clone()

        collection.add(newElem!!)

        return Response(true, "Элемент добавлен в коллекцию", req.key)
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