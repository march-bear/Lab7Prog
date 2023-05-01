package network.receiver

import request.Response
import java.net.Socket
import java.nio.ByteBuffer

class TCPStreamReceiver(private val sock: Socket) : ReceiverInterface {
    override fun receive(): Response? {
        val stream = sock.getInputStream()
        val lenArr = ByteArray(4)
        stream.read(lenArr)

        val len = ByteBuffer.wrap(lenArr).int
        val msgArr = ByteArray(len)
        stream.read(msgArr)

        val response = String(msgArr)

        return ReceiverInterface.deserialize(response)
    }
}