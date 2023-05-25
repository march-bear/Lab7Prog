import command.Command

abstract class AbstractCommandManager {
    abstract fun getCommand(name: String): Command?
    abstract fun getCommandForUser(name: String, username: String): Command?
}