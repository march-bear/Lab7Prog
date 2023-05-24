package command

/**
 * Перечисление типов аргументов команды
 */

enum class ArgumentType {
    TOKEN, // уникальный токен клиента
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    ORGANIZATION, // объект класса Organization
}

