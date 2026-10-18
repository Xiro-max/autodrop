
package dev.autodrop;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AutoDropItemClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static KeyBinding dropOnceKey;

    private static boolean enabled = false;
    private static boolean dropWholeStack = true;
    private static int tickDelay = 5;
    private static int tickCounter = 0;
    private static Path configPath;

    @Override
    public void onInitializeClient() {
        configPath = Path.of("config", "auto-drop-item.properties");
        loadConfig();

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.auto_drop_item.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyBinding.Category.MISC
            )
        );

        dropOnceKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.auto_drop_item.drop_once",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KeyBinding.Category.MISC
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;

                if (client.player != null) {
                    client.player.sendMessage(
                        Text.literal("Auto Drop All Items: "
                            + (enabled ? "ON" : "OFF")),
                        true
                    );
                }
            }

            while (dropOnceKey.wasPressed()) {
                dropNextItem(client);
            }

            if (!enabled
                    || client.player == null
                    || client.interactionManager == null) {
                return;
            }

            if (++tickCounter < Math.max(1, tickDelay)) {
                return;
            }

            tickCounter = 0;
            dropNextItem(client);
        });
    }

    private static void dropNextItem(
            net.minecraft.client.MinecraftClient client) {

        if (client.player == null
                || client.interactionManager == null) {
            return;
        }

        // Не выбрасывать предметы, когда открыт сундук
        // или другое внешнее окно.
        if (client.player.currentScreenHandler
                != client.player.playerScreenHandler) {
            return;
        }

        var handler = client.player.playerScreenHandler;

        for (Slot slot : handler.slots) {
            // Работаем только с инвентарём игрока.
            if (slot.inventory != client.player.getInventory()) {
                continue;
            }

            ItemStack stack = slot.getStack();

            if (stack.isEmpty()) {
                continue;
            }

            // 1 — выбросить весь стак.
            // 0 — выбросить один предмет.
            int button = dropWholeStack ? 1 : 0;

            client.interactionManager.clickSlot(
                handler.syncId,
                slot.id,
                button,
                SlotActionType.THROW,
                client.player
            );

            // За один цикл обрабатываем только один слот.
            return;
        }
    }

    private static void loadConfig() {
        try {
            Files.createDirectories(configPath.getParent());

            if (!Files.exists(configPath)) {
                Properties defaults = new Properties();

                defaults.setProperty(
                    "drop-whole-stack",
                    Boolean.toString(dropWholeStack)
                );

                defaults.setProperty(
                    "tick-delay",
                    Integer.toString(tickDelay)
                );

                try (var out =
                        Files.newOutputStream(configPath)) {
                    defaults.store(
                        out,
                        "Auto Drop Item configuration"
                    );
                }
            }

            Properties properties = new Properties();

            try (var in = Files.newInputStream(configPath)) {
                properties.load(in);
            }

            dropWholeStack = Boolean.parseBoolean(
                properties.getProperty(
                    "drop-whole-stack", "true"
                )
            );

            try {
                tickDelay = Integer.parseInt(
                    properties.getProperty("tick-delay", "5")
                );
            } catch (NumberFormatException ignored) {
                tickDelay = 5;
            }

            tickDelay = Math.max(1, Math.min(200, tickDelay));

        } catch (IOException e) {
            System.err.println(
                "[Auto Drop Item] Config error: "
                    + e.getMessage()
            );
        }
    }
}
