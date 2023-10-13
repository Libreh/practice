package practice.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class MoveWorld {

    public static void invoke() throws IOException {
        var gameDir = FabricLoader.getInstance().getGameDir();

        Files.move(gameDir.resolve("world"), gameDir.resolve("world_" + UUID.randomUUID()));
    }
}