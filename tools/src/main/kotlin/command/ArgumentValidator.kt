package command

import exceptions.InvalidArgumentsForCommandException
import java.lang.NullPointerException

class ArgumentValidator(private val argumentTypes: List<ArgumentType>) {
    init {
        if (ArgumentType.SCRIPT in argumentTypes && argumentTypes.size != 1)
            throw IllegalArgumentException("Команда, принимающая аргумент SCRIPT, не может принимать другие аргументы\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")

        else if (argumentTypes != argumentTypes.sorted())
            throw IllegalArgumentException("Описание аргументов команды должно идти в порядке: INT -> LONG -> " +
                    "FLOAT -> DOUBLE -> STRING -> ORGANIZATION.\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")

        else if (argumentTypes.count { it == ArgumentType.ORGANIZATION } > 1) {
            throw IllegalArgumentException("Команда не может содержать более одного аргумента ORGANIZATION\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")
        }
    }

    fun check(args: CommandArgument) {
        var thereIsOrganization = false
        var counter = 0
        for (type in argumentTypes) {
            try {
                when (type) {
                    ArgumentType.INT -> args.primitiveTypeArguments?.get(counter)!!.toInt()
                    ArgumentType.LONG -> args.primitiveTypeArguments?.get(counter)!!.toLong()
                    ArgumentType.FLOAT -> args.primitiveTypeArguments?.get(counter)!!.toFloat()
                    ArgumentType.DOUBLE -> args.primitiveTypeArguments?.get(counter)!!.toDouble()
                    ArgumentType.STRING -> args.primitiveTypeArguments?.get(counter)!!.toString()
                    ArgumentType.ORGANIZATION -> { thereIsOrganization = true; args.organization ?: NullPointerException()}
                    ArgumentType.SCRIPT -> checkScript(args.primitiveTypeArguments?.get(counter)!!)
                }
            } catch (ex: NumberFormatException) {
                throw InvalidArgumentsForCommandException("${args.primitiveTypeArguments?.get(counter) ?: ""}: " +
                        "аргумент не удовлетворяет условию type=$type")
            } catch (ex: NullPointerException) {
                throw InvalidArgumentsForCommandException(("Аргумент $type - не найден"))
            }
            counter++
        }

        if (counter != (args.primitiveTypeArguments?.size ?: 0))
            throw InvalidArgumentsForCommandException(
                if (thereIsOrganization && !args.needAnOrganization)
                    "аргумент - объект класса organization.Organization - вводится на следующих строках " +
                            "(для ввода объекта в конце строки поставьте \\"
                else
                    "${args.primitiveTypeArguments?.get(counter)}: неизвестный аргумент")
    }

    fun checkScript(filePath: String): Boolean {
        return true
    }
}