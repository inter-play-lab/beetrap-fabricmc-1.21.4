package beetrap.btfmc.handler;

import beetrap.btfmc.openai.OpenAiUtil;
import beetrap.btfmc.tts.TextToSpeechUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.openai.models.responses.Response;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public final class CommandHandler {
    private CommandHandler() {
        throw new AssertionError();
    }

    private static int gameNewCommand(CommandContext<ServerCommandSource> commandContext) {
        Integer aiLevel = commandContext.getArgument("ai_level", Integer.class);

        MinecraftServer server = commandContext.getSource().getServer();
        ServerWorld world = server.getOverworld();

        for(ServerPlayerEntity player : world.getPlayers()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }

        BeetrapGameHandler.createGame(server, aiLevel == null ? 0 : aiLevel);

        return 1;
    }

    private static int gameDestroyCommand(
            CommandContext<ServerCommandSource> commandSourceCommandContext) {
        BeetrapGameHandler.destroyGame();
        return 0;
    }

    private static void registerCommands0(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("game")
                .then(CommandManager.literal("new")
                        .then(CommandManager.argument("ai_level", IntegerArgumentType.integer())
                                .executes(CommandHandler::gameNewCommand)
                        )
                )
                .then(CommandManager.literal("destroy")
                        .executes(CommandHandler::gameDestroyCommand)
                )
        );

        dispatcher.register(
                CommandManager.literal("openai")
                        .then(CommandManager.literal("clear")
                                .executes(CommandHandler::openaiClearCommand)
                        )
                        .then(CommandManager.literal("say")
                                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(CommandHandler::openaiSayCommand)
                                )
                        )
        );
    }

    private static int openaiClearCommand(CommandContext<ServerCommandSource> ctx) {
        OpenAiUtil.clearHistory();
        ctx.getSource().sendFeedback(() -> Text.of("History cleared."), false);
        return 1;
    }

    private static int openaiSayCommand(CommandContext<ServerCommandSource> ctx) {
        String msg = StringArgumentType.getString(ctx, "message");
        CompletableFuture<Response> response = OpenAiUtil.getGpt4oLatestResponseAsync(msg);

        response.whenComplete((response1, throwable) -> {
            String s = response1.output().getFirst().asMessage().content().getFirst().asOutputText().text();
            TextToSpeechUtil.say(s);
            Text t = Text.of(s);
            for(ServerPlayerEntity player : ctx.getSource().getServer().getOverworld().getPlayers()) {
                player.sendMessage(t);
            }
        });

        return 1;
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(CommandHandler::registerCommands0);
    }
}
