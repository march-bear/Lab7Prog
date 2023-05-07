package command

import exceptions.InvalidArgumentsForCommandException
import exceptions.ScriptException
import request.CommandInfo
import kotlin.NullPointerException

class ArgumentValidator(
    private val argumentTypes: List<ArgumentType>,
    ) {
    init {
        if (argumentTypes != argumentTypes.sorted())
            throw IllegalArgumentException("Описание аргументов команды должно идти в порядке: INT -> LONG -> " +
                    "FLOAT -> DOUBLE -> STRING -> ORGANIZATION.\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")

        else if (argumentTypes.count { it == ArgumentType.ORGANIZATION } > 1)
            throw IllegalArgumentException("Команда не может содержать более одного аргумента ORGANIZATION\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")
    }

    fun check(args: CommandArgument) {
        var counter = 0
        for (type in argumentTypes) {
            try {
                when (type) {
                    ArgumentType.INT -> args.primArgs[counter].toInt()
                    ArgumentType.LONG -> args.primArgs[counter].toLong()
                    ArgumentType.FLOAT -> args.primArgs[counter].toFloat()
                    ArgumentType.DOUBLE -> args.primArgs[counter].toDouble()
                    ArgumentType.STRING -> args.primArgs[counter]
                    ArgumentType.ORGANIZATION -> args.organization ?: throw NullPointerException()
                }
            } catch (ex: NumberFormatException) {
                throw InvalidArgumentsForCommandException("${args.primArgs[counter]}: " +
                        "аргумент не удовлетворяет условию type=$type")
            } catch (ex: NullPointerException) {
                throw InvalidArgumentsForCommandException(("Аргумент $type - не найден"))
            } catch (ex: ScriptException) {
                throw InvalidArgumentsForCommandException("Ошибка во время проверки скрипта:\n${ex.message}")
            }
            counter++
        }

        if (counter != (args.primArgs.size))
            throw InvalidArgumentsForCommandException("${args.primArgs[counter]}: неизвестный аргумент")
    }

    fun checkNeedOrganization(): Boolean = ArgumentType.ORGANIZATION in argumentTypes
}

class ArgumentValidatorFactory(private val commandsInfo: List<CommandInfo>) {
    fun getByCommandName(name: String): ArgumentValidator? {
        return try {
            val info = commandsInfo.stream().filter { it.name == name} .findFirst().get()
            ArgumentValidator(info.args)
        } catch (ex: NullPointerException) {
            null
        } catch (ex: IllegalArgumentException) {
            throw ex
        }
    }
}