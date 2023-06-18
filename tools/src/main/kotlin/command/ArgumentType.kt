package command

/**
 * Перечисление типов аргументов команды
 */

enum class ArgumentType {
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    ORGANIZATION, // объект класса Organization
    TOKEN, // уникальный токен клиента
}

