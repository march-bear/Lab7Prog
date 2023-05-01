package command

import exceptions.InvalidArgumentsForCommandException
import exceptions.ScriptException
import request.CommandInfo
import kotlin.NullPointerException

class ArgumentValidator(
    private val argumentTypes: List<ArgumentType>,
    private val scriptValidator: ScriptValidator? = null,
    ) {
    init {
        if (argumentTypes != argumentTypes.sorted())
            throw IllegalArgumentException("Описание аргументов команды должно идти в порядке: INT -> LONG -> " +
                    "FLOAT -> DOUBLE -> STRING -> ORGANIZATION.\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")

        else if (argumentTypes.count { it == ArgumentType.ORGANIZATION } > 1)
            throw IllegalArgumentException("Команда не может содержать более одного аргумента ORGANIZATION\n" +
                    "Обратитесь к разработчику для разъяснения ситуации: dakako@go4rta.com")


        else if (argumentTypes.count { it == ArgumentType.SCRIPT } > 1)
            throw IllegalArgumentException("Команда не может содержать более одного аргумента SCRIPT\n" +
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
                    ArgumentType.SCRIPT -> checkScript(args.script)
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

    private fun checkScript(script: List<Pair<String, CommandArgument>>) {
        if (scriptValidator == null)
            throw ScriptException("Валидатор скрипта не найден")

        scriptValidator.check(script)
    }

    fun checkNeedScript(): Boolean = ArgumentType.SCRIPT in argumentTypes

    fun checkNeedOrganization(): Boolean = ArgumentType.ORGANIZATION in argumentTypes
}

class ScriptValidator(commandsInfo: List<CommandInfo>) {
    private val factory = ArgumentValidatorFactory(commandsInfo)
    fun check(script: List<Pair<String, CommandArgument>>) {
        for ((name, args) in script) {
            try {
                val validator = factory.getByCommandName(name) ?: throw ScriptException("$name: команда не найдена")
                validator.check(args)
            } catch (ex: IllegalArgumentException) {
                throw ScriptException(ex.message)
            } catch (ex: InvalidArgumentsForCommandException) {
                throw ScriptException(ex.message)
            }
        }
    }
}

class ArgumentValidatorFactory(private val commandsInfo: List<CommandInfo>) {
    fun getByCommandName(name: String): ArgumentValidator? {
        return try {
            val info = commandsInfo.stream().filter { it.name == name} .findFirst().get()
            ArgumentValidator(info.args, ScriptValidator(commandsInfo))
        } catch (ex: NullPointerException) {
            null
        } catch (ex: IllegalArgumentException) {
            throw ex
        }
    }
}