package xyz.bluspring.beetleorigin.client

import net.fabricmc.api.ClientModInitializer
import xyz.bluspring.beetleorigin.network.BeetleNetwork

class BeetleOriginClient : ClientModInitializer {
    override fun onInitializeClient() {
        BeetleNetwork.initClient()
    }
}