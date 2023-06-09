package network.xyo.sdk
import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.xyo.sdk.bluetooth.XyoBleSdk
import network.xyo.sdk.bluetooth.advertiser.XyoBluetoothAdvertiser
import network.xyo.sdk.bluetooth.server.XyoBluetoothServer
import network.xyo.sdkcorekotlin.network.XyoNetworkHandler
import network.xyo.sdkcorekotlin.network.XyoNetworkPipe
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalog
import network.xyo.sdkcorekotlin.node.XyoNodeListener
import network.xyo.sdkcorekotlin.node.XyoRelayNode

@InternalCoroutinesApi
@kotlin.ExperimentalUnsignedTypes
class XyoBleServer(
    context: Context,
    relayNode: XyoRelayNode,
    procedureCatalog: XyoProcedureCatalog,
    autoBridge: Boolean,
    acceptBridging: Boolean,
    listen: Boolean
) : XyoServer(relayNode, procedureCatalog) {

    override var autoBridge: Boolean = false
    override var acceptBridging: Boolean = false

    var advertiser: XyoBluetoothAdvertiser? = null
    lateinit var server: XyoBluetoothServer

    var listen: Boolean
        get() { return advertiser?.started ?: false }
        set(value) {
            runBlocking {
                advertiser?.let { advertiser ->
                    if (value) {
                        log.info("Starting Advertiser")
                        advertiser.startAdvertiser()
                    } else {
                        log.info("Stopping Advertiser")
                        advertiser.stopAdvertiser()
                    }
                }
            }
        }

    init {
        this.autoBridge = autoBridge
        this.acceptBridging = acceptBridging
        GlobalScope.launch {
            initServer(context)
            this@XyoBleServer.listen = listen
        }
    }

    private suspend fun initServer(context: Context): Boolean {
        advertiser = XyoBleSdk.advertiser(context)
        server = XyoBleSdk.server(context)
        var errorMessage: String? = null
        server.listener = object : XyoBluetoothServer.Listener {
            override fun onPipe(pipe: XyoNetworkPipe) {
                log.info("onPipe")
                GlobalScope.launch {
                    boundWitnessStarted(null)
                    val handler = XyoNetworkHandler(pipe)
                    relayNode.addListener("XyoBleServer", object : XyoNodeListener() {
                        override fun onBoundWitnessEndFailure(error: Exception?) {
                            errorMessage = error?.message ?: error?.toString() ?: "Unknown Error"
                        }
                    })
                    val bw = relayNode.boundWitness(handler, procedureCatalog)
                    relayNode.removeListener("XyoBleServer")
                    boundWitnessCompleted(null, bw, errorMessage)
                    pipe.close()
                    return@launch
                }
            }
        }
        log.info("Initialized Server")
        return true
    }
}
