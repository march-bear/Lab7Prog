package command.implementations

import collection.CollectionWrapper
import command.*
import organization.Organization
import request.Request
import request.Response

class AddIfMaxCommand(
    private val collection: CollectionWrapper<Organization>,
) : Command {
    private var newElem: Organization? = null

    override val info: String
        get() = "добавить новый элемент в коллекцию, если его значение " +
                "превышает значение наибольшего элемента этой коллекции"

    override val argumentValidator: ArgumentValidator = ArgumentValidator(listOf(ArgumentType.ORGANIZATION))

    override fun execute(req: Request): Response {
        argumentValidator.check(req.args)

        val elem = req.args.organization!!

        newElem = elem.clone()

        if (collection.isEmpty() || newElem!! > collection.max()) {
            collection.add(newElem!!)
            return Response(true,"Элемент добавлен в коллекцию", req.key)
        }

        return Response(true, "Элемент не является максимальным", req.key)
    }
}