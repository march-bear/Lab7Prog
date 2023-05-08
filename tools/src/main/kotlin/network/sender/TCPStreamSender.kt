package network.sender

import request.Request
import java.net.Socket
import java.nio.ByteBuffer

class TCPStreamSender(private val sock: Socket) : SenderInterface {
    override fun send(msg: String) {
        val stream = sock.getOutputStream()
        val lenMsg = msg.length
        val arrMsg = ByteBuffer.allocate(4).putInt(lenMsg).array() + msg.toByteArray()
        stream.write(arrMsg)
    }
}