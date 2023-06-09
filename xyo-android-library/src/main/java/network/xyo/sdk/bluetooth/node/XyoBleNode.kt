package network.xyo.sdk.bluetooth.node

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.generic.scanner.XYSmartScanListener
import network.xyo.sdk.bluetooth.client.XyoBluetoothClient
import network.xyo.sdk.bluetooth.server.XyoBluetoothServer
import network.xyo.sdkcorekotlin.hashing.XyoHash
import network.xyo.sdkcorekotlin.network.XyoNetworkHandler
import network.xyo.sdkcorekotlin.network.XyoNetworkPipe
import network.xyo.sdkcorekotlin.network.XyoProcedureCatalog
import network.xyo.sdkcorekotlin.node.XyoRelayNode
import network.xyo.sdkcorekotlin.repositories.XyoBridgeQueueRepository
import network.xyo.sdkcorekotlin.repositories.XyoOriginBlockRepository
import network.xyo.sdkcorekotlin.repositories.XyoOriginChainStateRepository

@kotlin.ExperimentalUnsignedTypes
open class XyoBleNode(private val procedureCatalog: XyoProcedureCatalog,
                      blockRepository: XyoOriginBlockRepository,
                      stateRepository: XyoOriginChainStateRepository,
                      bridgeQueueRepository: XyoBridgeQueueRepository,
                      hashingProvider: XyoHash.XyoHashProvider) :
        XyoRelayNode(blockRepository, stateRepository, bridgeQueueRepository, hashingProvider) {

    private var canBoundWitness = true

    val scanCallback = object : XYSmartScanListener() {
        override fun entered(device: XYBluetoothDevice) {
            super.entered(device)

            if (device is XyoBluetoothClient) {
                GlobalScope.launch {
                    tryBoundWitnessWithDevice(device)
                }
            }
        }
    }

    @InternalCoroutinesApi
    val serverCallback = object : XyoBluetoothServer.Listener {
        override fun onPipe(pipe: XyoNetworkPipe) {
            GlobalScope.launch {
                if (canBoundWitness) {
                    canBoundWitness = false
                    val handler = XyoNetworkHandler(pipe)

                    boundWitness(handler, procedureCatalog)

                    canBoundWitness = true
                    return@launch
                }

                pipe.close()
            }
        }
    }

    suspend fun tryBoundWitnessWithDevice(device: XyoBluetoothClient) {
        if (canBoundWitness) {
            canBoundWitness = false

            device.connection {
                val pipe = device.createPipe()

                if (pipe != null) {
                    val handler = XyoNetworkHandler(pipe)

                    val bw = boundWitness(handler, procedureCatalog)
                    return@connection XYBluetoothResult(bw != null)
                }

                return@connection XYBluetoothResult(false)
            }


            canBoundWitness = true
        }
    }
}