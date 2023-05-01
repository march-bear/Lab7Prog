package command.implementations

import collection.CollectionWrapper
import command.*
import exceptions.CancellationException
import jdk.javadoc.doclet.Reporter
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

    override fun cancel(): String {
        if (newElem == null)
            throw CancellationException("Отмена выполнения невозможна, так как команда ещё не была выполнен или уже была отменен")

        val res = collection.remove(newElem!!)

        if (res)
            return "Команда на добавление элемента отменена"
        else
            throw CancellationException("Отмена выполнения невозможна - добавленный элемент уже был подвергнут изменениям")
    }
}