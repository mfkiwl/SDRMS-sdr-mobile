package space.sdrmaker.sdrmobile.dsp.sdr

import com.mantz_it.hackrf_android.Hackrf
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

const val PACKET_QUEUE_SIZE = 10

/**
 * Signal source working with HackRF SDR device.
 *
 * @property hackrf Initialized instance of HackRF driver.
 */
class HackRFSignalSource(
    private val hackrf: Hackrf
) :
    Iterator<FloatArray> {

    private var queue: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(2)
    private var packets: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(PACKET_QUEUE_SIZE)
    private var active = false

    override fun hasNext() = active

    /**
     * Provides FloatArray representing interleaved IQ samples from HackRF device.
     *
     * @return FloatArray representing interleaved IQ samples from HackRF device.
     */
    override fun next(): FloatArray {
        if (!active) start()
        var packet = packets.poll(1000, TimeUnit.MILLISECONDS)
        while (packet == null) {
            println("HackTF: compute thread buffer underrun\n")
            packet = packets.poll(1000, TimeUnit.MILLISECONDS)
        }

        val result = FloatArray(packet.size) { i -> packet[i].toFloat() / 128}
        hackrf.returnBufferToBufferPool(packet)
        return result
    }

    private fun start() {
        println("HackRF receive thread start\n")
        active = true
        queue = hackrf.startRX()
        thread {
            while (active) {
                val packet =
                    queue.poll(1000, TimeUnit.MILLISECONDS)
                if (packet == null ) {
                    println("HackRF: receive thread buffer underrun\n")
                    continue
                }
                if (!packets.offer(packet)) {
                    println("HackRF: receive thread buffer overrun\n")
                }
            }
        }
    }

    /**
     * Stops streaming IQ data from HackRF driver.
     */
    fun stop() {
        println("HackRF stop\n")
        active = false
        hackrf.stop()
    }
}
