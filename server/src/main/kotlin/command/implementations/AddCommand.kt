package command.implementations

import collection.CollectionWrapper
import command.*
import db.DataBaseManager
import db.checkToken
import exceptions.CancellationException
import organization.Organization
import request.Request
import request.Response

class AddCommand(
    private val dbManager: DataBaseManager
) : Command {
    override val info: String
        get() = "добавить новый элемент в коллекцию (поля элемента указываются на отдельных строках)"

    override val argumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    private val statCoordInsert = dbManager.connection.prepareStatement(
        "INSERT INTO COORDINATES(x, y) VALUES (?, ?) ON CONFLICT DO NOTHING "
    )

    private val statAddressInsert = dbManager.connection.prepareStatement(
        "INSERT INTO ADDRESSES(zip_code) VALUES (?) ON CONFLICT DO NOTHING"
    )

    private val statFullNameSelect = dbManager.connection.prepareStatement(
        "SELECT * FROM ORGANIZATIONS WHERE full_name = ?"
    )

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)
        if (!dbManager.checkToken(req.token))
            return Response(false, "Токен некорректен", req.key, "identify")

        val elem: Organization = req.args.organization!!

        if (elem.fullName != null) {
            statFullNameSelect.setString(1, elem.fullName)
            if (statFullNameSelect.executeQuery().next())
                return Response(false, "Полное имя не уникально", req.key)
        }

        statCoordInsert.setDouble(1, elem.coordinates.x)
        statCoordInsert.setInt(2, elem.coordinates.y)
        statCoordInsert.executeUpdate()

        if (elem.postalAddress != null) {
            statAddressInsert.setString(1, elem.postalAddress!!.zipCode)
        }

        val statCoordId = dbManager.connection.prepareStatement("SELECT id FROM COORDINATES WHERE x = ? AND y = ?")

        statCoordId.setDouble(0, elem.coordinates.x)
        statCoordId.setInt(1, elem.coordinates.y)
        val coordIds = statCoordId.executeQuery()
        val coordId = if (coordIds.next())
            coordIds.getInt(1)
        else
            throw Exception()

        return Response(true, "Элемент добавлен в коллекцию", req.key)
    }

    companion object {

    }
}