import exceptions.InvalidFieldValueException
import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import org.valiktor.ConstraintViolationException
import organization.Address
import organization.Organization
import organization.OrganizationType
import java.util.function.Consumer

class OrganizationFactory(private val reader: Reader? = null) {
    private fun newOrganizationFromScript(): Organization {
        val newOrganization = Organization()
        getValueForField("String") { newOrganization.name = reader!!.readString() }

        getValueForField("Double") { newOrganization.coordinates.x = reader!!.readString().toDoubleOrNull()
            ?: throw NumberFormatException("Ожидалось дробное числовое значение для поля: x") }

        getValueForField("Int") { newOrganization.coordinates.y = reader!!.readString().toIntOrNull()
            ?: throw NumberFormatException("Ожидалось целочисленное значение для поля: y") }

        getValueForField("Int") { newOrganization.annualTurnover = reader!!.readString().toIntOrNull()
            ?: throw NumberFormatException("Ожидалось целочисленное значение для поля: годовой оборот") }

        getValueForField("String или null") { newOrganization.fullName = reader!!.readStringOrNull() }

        getValueForField("Long или null") {
            newOrganization.employeesCount = reader!!.readStringOrNull()?.toLong()
        }

        getValueForField("organization.OrganizationType") {
            newOrganization.type = OrganizationType.valueOf(reader!!.readString())
        }

        getValueForField("String или null") {
            val input = reader!!.readStringOrNull()
            if (input == null)
                newOrganization.postalAddress = null
            else
                newOrganization.postalAddress = Address(input)
        }

        return newOrganization
    }

    fun newOrganizationFromInput(): Organization {
        if (reader != null)
            return newOrganizationFromScript()

        val r = Reader()

        val newOrganization = Organization()

        readValueForField("Имя организации", "String") {
            newOrganization.name = r.readString()
        }

        Messenger.printMessage("Координаты")
        readValueForField("x", "Double") {
            newOrganization.coordinates.x = r.readString().toDoubleOrNull()
                ?: throw NumberFormatException("Введите дробное числовое значение")
        }

        readValueForField("y", "Int") {
            newOrganization.coordinates.y = r.readString().toIntOrNull()
                ?: throw NumberFormatException("Введите целочисленное значение")
        }

        readValueForField("Годовой оборот", "Int") {
            newOrganization.annualTurnover = r.readString().toIntOrNull()
                ?: throw NumberFormatException("Введите целочисленное значение")
        }

        readValueForField("Полное имя", "String или null") {
            newOrganization.fullName = r.readStringOrNull()
        }

        readValueForField("Количество сотрудников", "Long или null") {
            newOrganization.employeesCount = r.readStringOrNull()?.toLong()
        }

        readValueForField(
            "Тип организации " +
                    "(COMMERCIAL, GOVERNMENT, TRUST, PRIVATE_LIMITED_COMPANY или OPEN_JOINT_STOCK_COMPANY)",
            "organization.OrganizationType",
        ) {
            newOrganization.type = OrganizationType.valueOf(r.readString())
        }

        readValueForField("Адрес (ZIP-код)", "String или null") {
            val input = r.readStringOrNull()
            if (input == null)
                newOrganization.postalAddress = null
            else
                newOrganization.postalAddress = Address(input)
        }

        return newOrganization
    }

    private fun readValueForField(message: String, type: String, consumer: Consumer<Unit>) {
        while (true) {
            try {
                Messenger.inputPrompt(message, ": ")
                consumer.accept(Unit)
                break
            } catch (ex: ConstraintViolationException) {
                Messenger.printMessage("Невалидное значение поля, повторите ввод", TextColor.RED)
            } catch (ex: IllegalArgumentException) {
                Messenger.printMessage("Невалидное значение поля. " +
                            "Ожидался аргумент типа $type. Повторите ввод", TextColor.RED)
            } catch (ex: NumberFormatException) {
                Messenger.printMessage(ex.message ?: "Введите числовое значение", TextColor.RED)
            }
        }
    }

    private fun getValueForField(type: String, consumer: Consumer<Unit>) {
        try {
            consumer.accept(Unit)
        } catch (ex: ConstraintViolationException) {
            throw InvalidFieldValueException(
                "Невалидное значение поля."
            )
        } catch (ex: IllegalArgumentException) {
            throw InvalidFieldValueException(
                "Невалидное значение поля. Ожидался аргумент типа $type."
            )
        } catch (ex: NumberFormatException) {
            throw InvalidFieldValueException(ex.message)
        }
    }
}