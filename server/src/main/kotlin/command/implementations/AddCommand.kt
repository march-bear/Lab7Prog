package command.implementations

import collection.CollectionWrapper
import command.*
import db.DataBaseManager
import db.addOrganization
import db.checkToken
import db.getUserByToken
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

    private val statCoordIdSelect = dbManager.connection.prepareStatement("SELECT id FROM COORDINATES WHERE x = ? AND y = ?")

    private val statAddressInsert = dbManager.connection.prepareStatement(
        "INSERT INTO ADDRESSES(zip_code) VALUES (?) ON CONFLICT DO NOTHING"
    )

    private val statAddressIdSelect = dbManager.connection.prepareStatement(
        "SELECT id FROM ADDRESSES WHERE zipcode = ?"
    )

    private val statFullNameSelect = dbManager.connection.prepareStatement(
        "SELECT 1 FROM ORGANIZATIONS WHERE full_name = ?"
    )

    private val statOrgTypeIdSelect = dbManager.connection.prepareStatement(
        "SELECT id FROM ORGANIZATION_TYPES WHERE name = ?"
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

        statCoordIdSelect.setDouble(1, elem.coordinates.x)
        statCoordIdSelect.setInt(2, elem.coordinates.y)
        val coordIds = statCoordIdSelect.executeQuery(); coordIds.next()
        val coordId = coordIds.getInt(1)

        val postalAddressId = if (elem.postalAddress != null) {
            statAddressInsert.setString(1, elem.postalAddress!!.zipCode)
            statAddressInsert.executeQuery()
            statAddressIdSelect.setString(1, elem.postalAddress!!.zipCode)
            val addressIds = statAddressIdSelect.executeQuery(); addressIds.next()
            addressIds.getInt(1)
        } else { null }

        statOrgTypeIdSelect.setString(1, elem.type.name)
        val orgTypeIds = statOrgTypeIdSelect.executeQuery(); orgTypeIds.next()
        val orgTypeId = orgTypeIds.getInt(1)

        val ownerId = dbManager.getUserByToken(req.token)!!

        val orgId = dbManager.addOrganization(elem.name, coordId, elem.annualTurnover, elem.fullName, elem.employeesCount,
            orgTypeId, postalAddressId, ownerId)

        println(orgId)
        return if (orgId == null) {
            Response(false, "Не удалось добавить элемент", req.key)
        } else {
            Response(true, "Элемент добавлен с id=$orgId", req.key)
        }
    }
}