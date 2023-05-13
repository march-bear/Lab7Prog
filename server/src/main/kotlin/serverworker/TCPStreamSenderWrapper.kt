package serverworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.sender.AbstractSenderWrapper
import network.sender.SenderInterface
import network.sender.TCPStreamSender
import request.Response
import java.net.Socket

class TCPStreamSenderWrapper(sock: Socket) : AbstractSenderWrapper<Response>() {
    override val sender: SenderInterface = TCPStreamSender(sock)

    override fun send(msg: Response) {
        val msgString = serialize(msg) ?: throw Exception()
        println(msg)
        sender.send(msgString)
    }

    private fun serialize(msg: Response): String? {
        return try {
            Json.encodeToString(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}