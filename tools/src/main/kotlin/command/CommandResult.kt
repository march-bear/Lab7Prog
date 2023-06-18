package command

import message.DataBaseChanges
import message.Response

data class CommandResult(
    val resp: Response,
    val inf: DataBaseChanges? = null,
)