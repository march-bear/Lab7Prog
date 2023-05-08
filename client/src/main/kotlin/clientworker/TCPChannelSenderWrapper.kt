package clientworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import network.sender.AbstractSenderWrapper
import network.sender.SenderInterface
import network.sender.TCPChannelSender
import request.Request
import request.Response
import java.nio.channels.SocketChannel

class TCPChannelSenderWrapper(sock: SocketChannel): AbstractSenderWrapper<Request>() {
    override val sender: SenderInterface = TCPChannelSender(sock)

    override fun send(msg: Request) {
        val msgString = serialize(msg) ?: throw Exception()
        sender.send(msgString)
    }

    private fun serialize(msg: Request): String? {
        return try {
            Json.encodeToString(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}