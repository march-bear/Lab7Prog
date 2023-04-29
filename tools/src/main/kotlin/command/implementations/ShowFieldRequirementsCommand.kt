package command.implementations

import command.Command
import command.CommandArgument
import command.CommandResult
import exceptions.CancellationException
import organization.Organization

class ShowFieldRequirementsCommand : Command {
    override val info: String
        get() = "вывести требования к полям класса organization.Organization"

    override fun execute(args: CommandArgument): CommandResult {
        return CommandResult(true, Organization.fieldRequirements, false)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}