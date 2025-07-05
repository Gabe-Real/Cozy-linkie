import net.minecraft.network.message.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.util.*

class DiscordLinkWebSocketClient(serverUri: URI, private val server: MinecraftServer) : WebSocketClient(serverUri) {
    private val logger = LogManager.getLogger("DiscordLinkWS")

    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.info("Connected to Discord bot WebSocket")
    }

    override fun onMessage(message: String?) {
        logger.info("Received message from Discord bot: $message")
        if (message == null) return

        try {
            val json = JSONObject(message)
            val playerName = json.getString("player")
            val colorHex = json.getString("color")
            val msg = json.getString("message")

            val colourPlayer = Text.literal("[$playerName]").styled {
                it.withColor(parseColor(colorHex))
            }

            val colourDiscord = Text.literal("[Discord]").styled {
                it.withColor(TextColor.fromRgb(0x5865F2)) // Discord blurple
            }

            val messageText = Text.literal(" $msg").styled {
                it.withColor(TextColor.fromRgb(0xFFFFFF)) // Set message color explicitly
            }

            val fullMessage = colourDiscord.copy()
                .append(" ")
                .append(colourPlayer)
                .append(messageText)

            server.execute {
                server.playerManager.playerList.forEach { player ->
                    player.sendMessage(fullMessage, false)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse message from Discord bot", e)
        }
    }

    // Utility: Convert hex like "#9b59b6" to TextColor
    fun parseColor(hex: String): TextColor {
        return try {
            val colorInt = Integer.parseInt(hex.removePrefix("#"), 16)
            TextColor.fromRgb(colorInt)
        } catch (e: Exception) {
            TextColor.fromRgb(0xFFFFFF) // default to white
        }
    }


    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        logger.info("Disconnected from Discord bot WS: $code / $reason")
    }

    override fun onError(ex: Exception?) {
        logger.error("WebSocket error", ex)
    }

    // Add this helper method to send messages to Discord bot
    fun sendMessageToBot(message: String) {
        if (this.isOpen) {
            this.send(message)
            logger.info("Sent message to Discord bot: $message")
        } else {
            logger.warn("WebSocket not open, can't send message")
        }
    }
}

// Alternative approach using a companion object to store server reference
class DiscordLinkWebSocketClientAlt(serverUri: URI) : WebSocketClient(serverUri) {
    private val logger = LogManager.getLogger("DiscordLinkWS")

    companion object {
        private var minecraftServer: MinecraftServer? = null

        fun setServer(server: MinecraftServer) {
            minecraftServer = server
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.info("Connected to Discord bot WebSocket")
    }

    override fun onMessage(message: String?) {
        logger.info("Received message from Discord bot: $message")
        if (message == null) return

        val server = minecraftServer ?: run {
            logger.error("MinecraftServer not set!")
            return
        }

        server.execute {
            val chatText = Text.literal(message)
            server.playerManager.playerList.forEach { player ->
                player.sendMessage(chatText, false)
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        logger.info("Disconnected from Discord bot WS: $code / $reason")
    }

    override fun onError(ex: Exception?) {
        logger.error("WebSocket error", ex)
    }

    fun sendMessageToBot(message: String) {
        if (this.isOpen) {
            this.send(message)
            logger.info("Sent message to Discord bot: $message")
        } else {
            logger.warn("WebSocket not open, can't send message")
        }
    }
}
