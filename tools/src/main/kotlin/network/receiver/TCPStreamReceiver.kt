package network.receiver

import java.net.Socket
import java.nio.ByteBuffer

class TCPStreamReceiver(private val sock: Socket) : ReceiverInterface {
    override fun receive(): String {
        val stream = sock.getInputStream()
        val lenArr = ByteArray(4)
        stream.read(lenArr)

        val len = ByteBuffer.wrap(lenArr).int
        val msgArr = ByteArray(len)
        stream.read(msgArr)

        return String(msgArr)
    }
}