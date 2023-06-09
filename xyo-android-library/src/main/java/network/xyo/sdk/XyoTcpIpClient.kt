package network.xyo.sdk
import android.net.Uri
import java.io.IOException
import java.net.Socket 
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import network.xyo.sdkcorekotlin.boundWitness.XyoBoundWitness
import network.xyo.sdkcorekotlin.network.XyoNetworkHandler
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalog
import network.xyo.sdkcorekotlin.network.tcp.XyoTcpPipe
import network.xyo.sdkcorekotlin.node.XyoNodeListener
import network.xyo.sdkcorekotlin.node.XyoRelayNode
import java.nio.ByteBuffer

@kotlin.ExperimentalUnsignedTypes
class XyoTcpIpClient(
    relayNode: XyoRelayNode,
    procedureCatalog: XyoProcedureCatalog,
    override var autoBridge: Boolean,
    override var acceptBridging: Boolean,
    autoBoundWitness: Boolean
) : XyoClient(relayNode, procedureCatalog, autoBoundWitness) {

    init {
        relayNode.addListener("XyoTcpIpClient", object : XyoNodeListener() {
            override fun onBoundWitnessEndSuccess(boundWitness: XyoBoundWitness) {
                super.onBoundWitnessEndSuccess(boundWitness)
                if (autoBridge && ByteBuffer.wrap((relayNode.stateRepository.getIndex()?.valueCopy)).int % 5 == 0) {
                    GlobalScope.launch {
                        this@XyoTcpIpClient.bridge()
                    }
                }
            }
        })
    }

    private val bridgeMutex = Mutex()

    suspend fun bridge(): String? {
        var errorMessage: String? = null
        var networkErrorMessage: String? = null
        if (bridgeMutex.tryLock()) {
            log.info("bridge - started: [${knownBridges?.size}]")
            knownBridges?.let { knownBridges ->
                if (knownBridges.isEmpty()) {
                    log.info("No known bridges, skipping bridging!")
                    errorMessage = "No Known Bridges"
                } else {
                    var bw: XyoBoundWitness? = null
                    knownBridges.forEach { bridge ->
                        log.info("Trying to bridge: $bridge")
                        boundWitnessStarted(bridge)
                        try {
                            if (bw == null) {
                                val uri = Uri.parse(bridge)

                                log.info("Trying to bridge [info]: ${uri.host}:${uri.port}")

                                val socket = Socket(uri.host, uri.port)
                                val pipe = XyoTcpPipe(socket, null)
                                val handler = XyoNetworkHandler(pipe)

                                log.info("Starting Bridge BoundWitness")
                                relayNode.addListener("XyoTcpIpClient-bridge", object : XyoNodeListener() {
                                    override fun onBoundWitnessEndFailure(error: Exception?) {
                                        errorMessage = error?.message ?: error?.toString() ?: "Unknown Error"
                                    }
                                })
                                bw = relayNode.boundWitness(handler, procedureCatalog)
                                relayNode.removeListener("XyoTcpIpClient-bridge")
                                pipe.close()
                                log.info("Bridge Result: $bw")
                            }
                        } catch (e: IOException) {
                            log.info("Bridging Excepted $e")
                            networkErrorMessage = e.message ?: e.toString()
                        }
                        boundWitnessCompleted(bridge, bw, errorMessage ?: networkErrorMessage)
                    }
                }
            }
            bridgeMutex.unlock()
        }
        return errorMessage ?: networkErrorMessage
    }

    fun startBridge() {
        
    }
    
    override var scan: Boolean
        get() { return false }
        set(_) { }
}
