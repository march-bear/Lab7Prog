package command.implementations

import command.Command
import command.CommandResult
import exceptions.CancellationException
import organization.Organization
import request.Request
import request.Response

class ShowFieldRequirementsCommand : Command {
    override val info: String
        get() = "вывести требования к полям класса organization.Organization"

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)
        return Response(true, Organization.fieldRequirements, req.key)
    }

    override fun cancel(): String {
        throw CancellationException("Отмена выполнения команды невозможна")
    }
}