package dev.gabereal.cozy_linkie

import DiscordLinkWebSocketClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedChatMessage
import java.net.URI
import kotlin.concurrent.thread
import net.minecraft.server.MinecraftServer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

object ExampleMod : ModInitializer {
    val LOGGER: Logger = LogManager.getLogger("cozy_linkie")
    lateinit var wsClient: DiscordLinkWebSocketClient

    override fun onInitialize(mod: ModContainer) {
        LOGGER.info("Hello Quilt world from ${mod.metadata().name()}!")

        ServerLifecycleEvents.SERVER_STARTED.register { server: MinecraftServer ->
            val wsUri = URI("ws://localhost:8080") // Replace with your bot WS URL
            wsClient = DiscordLinkWebSocketClient(wsUri, server)

            thread(start = true) {
                try {
                    wsClient.connectBlocking()
                    LOGGER.info("WebSocket connection established!")
                } catch (e: Exception) {
                    LOGGER.error("Failed to connect WebSocket", e)
                }
            }

            // Register chat listener here to ensure server is ready
            ServerMessageEvents.CHAT_MESSAGE.register { message: SignedChatMessage, sender: ServerPlayerEntity, params: MessageType.Parameters ->
                val chatContent = message.content.toString()
                val playerName = sender.name.string

                val jsonMessage = """{"player": "$playerName", "message": "$chatContent"}"""

                if (::wsClient.isInitialized && wsClient.isOpen) {
                    wsClient.sendMessageToBot(jsonMessage)
                }
            }
        }
    }
}

