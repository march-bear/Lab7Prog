package clientworker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import message.Message
import network.sender.AbstractSenderWrapper
import network.sender.SenderInterface
import network.sender.TCPChannelSender
import message.Request
import java.nio.channels.SocketChannel

class TCPChannelSenderWrapper(sock: SocketChannel): AbstractSenderWrapper<Message>() {
    override val sender: SenderInterface = TCPChannelSender(sock)

    override fun send(msg: Message) {
        val msgString = serialize(msg) ?: throw Exception()
        sender.send(msgString)
    }

    private fun serialize(msg: Message): String? {
        return try {
            Json.encodeToString(msg)
        } catch (ex: SerializationException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }
    }
}