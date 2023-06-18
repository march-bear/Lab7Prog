package db.requests.transactions

import command.CommandArgument
import message.DataBaseChanges
import organization.Organization
import java.sql.Connection
import java.sql.SQLException
import java.sql.Types

class InsertOrganizationTransaction(
    private val elem: Organization,
    private val userId: Long,
) : AbstractTransaction() {
    override fun execute(conn: Connection): DataBaseChanges {
        conn.autoCommit = false

        val statCoordInsert = conn.prepareStatement("INSERT INTO COORDINATES(x, y) VALUES (?, ?) ON CONFLICT DO NOTHING ")

        statCoordInsert.setDouble(1, elem.coordinates.x)
        statCoordInsert.setInt(2, elem.coordinates.y)
        statCoordInsert.executeUpdate()

        val statCoordIdSelect = conn.prepareStatement("SELECT id FROM COORDINATES WHERE x = ? AND y = ?")

        statCoordIdSelect.setDouble(1, elem.coordinates.x)
        statCoordIdSelect.setInt(2, elem.coordinates.y)
        val coordIds = statCoordIdSelect.executeQuery(); coordIds.next()
        val coordId = coordIds.getInt(1)

        val statAddressInsert = conn.prepareStatement("INSERT INTO ADDRESSES(zip_code) VALUES (?) ON CONFLICT DO NOTHING")

        val postalAddressId = if (elem.postalAddress != null) {
            statAddressInsert.setString(1, elem.postalAddress!!.zipCode)
            statAddressInsert.executeQuery()
            val statAddressIdSelect = conn.prepareStatement("SELECT id FROM ADDRESSES WHERE zipcode = ?")

            statAddressIdSelect.setString(1, elem.postalAddress!!.zipCode)
            val addressIds = statAddressIdSelect.executeQuery(); addressIds.next()
            addressIds.getInt(1)
        } else { null }

        val statOrgTypeIdSelect = conn.prepareStatement(
            "SELECT id FROM ORGANIZATION_TYPES WHERE name = ?"
        )

        statOrgTypeIdSelect.setString(1, elem.type.name)
        val orgTypeIds = statOrgTypeIdSelect.executeQuery(); orgTypeIds.next()
        val orgTypeId = orgTypeIds.getInt(1)

        val statInsertOrg = conn.prepareStatement(
            "INSERT INTO ORGANIZATIONS(name, coord_id, annual_turnover, full_name, employees_count, " +
                    "organization_type_id, address_id, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"
        )
        statInsertOrg.setString(1, elem.name)
        statInsertOrg.setInt(2, coordId)
        statInsertOrg.setInt(3, elem.annualTurnover)
        statInsertOrg.setString(4, elem.fullName)
        if (elem.employeesCount != null)
            statInsertOrg.setLong(5, elem.employeesCount!!)
        else
            statInsertOrg.setNull(5, Types.BIGINT)

        statInsertOrg.setInt(6, orgTypeId)

        if (postalAddressId != null) statInsertOrg.setInt(7, postalAddressId) else statInsertOrg.setNull(7, Types.INTEGER)
        statInsertOrg.setInt(8, userId.toInt())

        val res = statInsertOrg.executeQuery()

        var orgId: Long? = null
        if (res.next()) {
            orgId = res.getInt("id").toLong()
        }

        val changeIds = conn.createStatement().executeQuery("UPDATE CHANGES SET number = number + 1 RETURNING number")
        var changeId: Long? = null
        if (changeIds.next()) {
            changeId = changeIds.getInt("number").toLong()
        }

        conn.commit()

        if (orgId == null || changeId == null) {
            throw SQLException("Траблы во время добавления элемента")
        } else {
            elem.id = orgId.toLong()
            val args = CommandArgument()
            args.setOrganization(elem)
            return DataBaseChanges("ORGANIZATIONS", changeId, listOf(Pair(ChangeType.INSERT, args)))
        }
    }
}