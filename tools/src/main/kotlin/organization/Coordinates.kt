package organization

import kotlinx.serialization.Serializable

/**
 * Класс координат. Объекты класса используются элементами коллекции
 */

@Serializable
class Coordinates(
    var x: Double = 0.0,
    var y: Int = 0,
) {
    override fun toString(): String {
        return "$x $y"
    }
    fun clone(): Coordinates {
        return Coordinates(x, y)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Coordinates

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y
        return result
    }
}