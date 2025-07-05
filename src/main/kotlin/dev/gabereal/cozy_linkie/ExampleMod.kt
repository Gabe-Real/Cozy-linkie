package dev.gabereal.cozy_linkie

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

object ExampleMod : ModInitializer {
    val LOGGER: Logger = LogManager.getLogger("cozy_linkie")

    override fun onInitialize(mod: ModContainer) {
        LOGGER.info("Hello Quilt world from ${mod.metadata().name()}!")
    }
}
