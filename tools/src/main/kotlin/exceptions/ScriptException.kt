package exceptions

class ScriptException(
    override val message: String? = "Обнаружен циклический вызов скрипта"
) : Exception(message)