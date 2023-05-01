package commandcallgraph

import command.Command
import command.CommandArgument
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RequestGraph {
    private val countNumbers: HashMap<String, Long> = HashMap()
    private val leafs: ArrayList<Leaf> = ArrayList()
    private var currLeaf: Leaf

    init {
        leafs.add(Leaf(null, null, ROOT_NAME, null))
        currLeaf = leafs.first()
    }

    fun getCurrLeafId(): String {
        return currLeaf.id
    }

    fun addLeaf(command: Command, args: CommandArgument, name: String = command::class.simpleName ?: "COMMAND"): String {
        val requestClassName = command::class.simpleName.toString().uppercase()
        val id = "${name.uppercase()}_${getNumber(requestClassName)}"

        val newLeaf = Leaf(command, args, id, currLeaf)
        currLeaf = newLeaf

        leafs.add(newLeaf)

        return id
    }

    fun rollback(leafId: String): Boolean {
        val targetLeaf = leafs.find { it.id == leafId}

        if (targetLeaf == null) {
            return false
        } else {
            val route = getRoute(currLeaf, targetLeaf)
            while (route.isNotEmpty()) {
                val (action, commandPair) = route.poll()
                val (command, args) = commandPair
                when (action) {
                    CommandAction.EXECUTING -> command.execute(args)
                    CommandAction.CANCELLATION -> command.cancel()
                }
            }
        }

        currLeaf = targetLeaf
        return true
    }

    private fun getRoute(currLeaf: Leaf, targetLeaf: Leaf): Queue<Pair<CommandAction, Pair<Command, CommandArgument>>> {
        val result: Queue<Pair<CommandAction, Pair<Command, CommandArgument>>> = LinkedList()
        if (currLeaf == targetLeaf)
            return LinkedList()

        val routeFromRootToCurr = LinkedList<Leaf>()
        var previousToCurrLeaf = currLeaf

        while (previousToCurrLeaf.id != ROOT_NAME) {
            routeFromRootToCurr.add(previousToCurrLeaf)
            previousToCurrLeaf = previousToCurrLeaf.previousLeaf!!

            if (previousToCurrLeaf == targetLeaf) {
                for (leaf in routeFromRootToCurr)
                    result.add(Pair(CommandAction.CANCELLATION, Pair(leaf.command!!, leaf.args!!)))
                return result
            }
        }

        val routeFromCurrBranchToTarget = LinkedList<Leaf>()
        var previousToTargetLeaf = targetLeaf

        while (previousToTargetLeaf.id != ROOT_NAME) {
            if (previousToTargetLeaf in routeFromRootToCurr) {
                var currInRouteRootToCurr: Leaf = routeFromRootToCurr[0]
                while (currInRouteRootToCurr != previousToTargetLeaf) {
                    result.add(Pair(CommandAction.CANCELLATION, Pair(currInRouteRootToCurr.command!!, currInRouteRootToCurr.args!!)))
                    currInRouteRootToCurr = currInRouteRootToCurr.previousLeaf!!
                }

                for (leaf in routeFromCurrBranchToTarget.descendingIterator())
                    result.add(Pair(CommandAction.EXECUTING, Pair(leaf.command!!, leaf.args!!)))

                return result
            }

            routeFromCurrBranchToTarget.add(previousToTargetLeaf)
            previousToTargetLeaf = previousToTargetLeaf.previousLeaf!!
        }
        for (leaf in routeFromRootToCurr)
            result.add(Pair(CommandAction.CANCELLATION, Pair(leaf.command!!, leaf.args!!)))

        for (leaf in routeFromCurrBranchToTarget.descendingIterator())
            result.add(Pair(CommandAction.EXECUTING, Pair(leaf.command!!, leaf.args!!)))

        return result
    }

    private fun getNumber(commandName: String): Long {
        if (countNumbers[commandName] == null)
            countNumbers[commandName] = 1L
        else
            countNumbers[commandName] = countNumbers[commandName]!! + 1L

        return countNumbers[commandName]!!
    }

    companion object {
        const val ROOT_NAME = "START"
    }
}

enum class CommandAction {
    CANCELLATION,
    EXECUTING,
}