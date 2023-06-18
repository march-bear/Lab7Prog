package commands

import collection.CollectionWrapper
import command.Command
import command.CommandResult
import db.manager.DataBaseManager
import db.requests.queries.SelectAllFromAddressesQuery
import db.requests.queries.SelectAllFromCoordinatesQuery
import db.requests.queries.SelectAllFromOrganizationTypesQuery
import db.requests.queries.SelectAllFromOrganizationsQuery
import message.Request
import message.Response
import organization.Address
import organization.Coordinates
import organization.Organization
import organization.OrganizationType
import java.sql.Timestamp
import java.util.*

class UpdateLocalCollectionCommand(
    private val collection: CollectionWrapper<Organization>,
    private val dbManager: DataBaseManager,
) : Command {
    override val info: String
        get() = "загрузить в кеш всю коллекцию"

    override fun execute(req: Request): CommandResult {
        val error = CommandResult(Response(req.key, false, "Не удалось загрузить коллекцию"))

        val addresses = handleAddresses(dbManager.execute(SelectAllFromAddressesQuery()) ?: return error)
        val coordinates = handleCoordinates(dbManager.execute(SelectAllFromCoordinatesQuery()) ?: return error)
        val orgTypes = handleOrgTypes(dbManager.execute(SelectAllFromOrganizationTypesQuery()) ?: return error)
        val resOrganizations = dbManager.execute(SelectAllFromOrganizationsQuery()) ?: return error

        for (org in resOrganizations) {
            val id = (org["id"] as Number).toLong()
            val name = org["name"] as String
            val date = Date((org["date"] as Number).toLong())
            val coordId = (org["coord_id"] as Number).toLong()
            val annualTurnover = (org["annual_turnover"] as Number).toInt()
            val fullName = org["fullName"] as String?
            val employeesCount = (org["employees_count"] as Number?)?.toLong()
            val typeId = (org["organization_type_id"] as Number).toLong()
            val addressId = (org["address_id"]as Number?)?.toLong()
            val ownerId = (org["owner_id"] as Number).toLong()

            collection.add(
                Organization(
                    id, name, date, coordinates[coordId]!!,
                    annualTurnover, fullName, if (employeesCount != 0L) employeesCount else null,
                    orgTypes[typeId]!!, addresses[addressId], ownerId
                )
            )
        }

        return CommandResult(Response(req.key, true, "Готовенько"))
    }

    private fun handleAddresses(res: List<Map<String, Any?>>): Map<Long, Address> {
        val addresses = mutableMapOf<Long, Address>()
        for (i in res) {
            val id = (i["id"] as Number).toLong()
            val zipCode = i["zip_code"] as String
            addresses[id] = Address(zipCode)
        }

        return addresses
    }

    private fun handleCoordinates(res: List<Map<String, Any?>>): Map<Long, Coordinates> {
        val coordinates = mutableMapOf<Long, Coordinates>()
        for (i in res) {
            val id = (i["id"] as Number).toLong()
            val x = i["x"] as Double
            val y = i["y"] as Int
            coordinates[id] = Coordinates(x, y)
        }

        return coordinates
    }

    private fun handleOrgTypes(res: List<Map<String, Any?>>): Map<Long, OrganizationType> {
        val types = mutableMapOf<Long, OrganizationType>()
        println(res)
        for (i in res) {
            val id = (i["id"] as Number).toLong()
            val type = OrganizationType.valueOf(i["name"] as String)
            types[id] = type
        }
        return types
    }
}