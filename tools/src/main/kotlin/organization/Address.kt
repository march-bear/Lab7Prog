package organization

import kotlinx.serialization.Serializable

/**
 * Класс адресов. Объекты класса используются элементами коллекции
 */

@Serializable
class Address(var zipCode: String) {
    override fun toString(): String {
        return zipCode
    }

    fun clone(): Address {
        return Address(zipCode)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Address

        if (zipCode != other.zipCode) return false

        return true
    }

    override fun hashCode(): Int {
        return zipCode.hashCode()
    }
}