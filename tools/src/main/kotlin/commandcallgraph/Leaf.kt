package commandcallgraph

import command.Command
import command.CommandArgument

data class Leaf(
    val command: Command?,
    val args: CommandArgument?,
    val id: String,
    val previousLeaf: Leaf?,
)