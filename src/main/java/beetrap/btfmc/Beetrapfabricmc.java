package beetrap.btfmc;

import beetrap.btfmc.networking.PlayerPollinateC2SPayload;
import beetrap.btfmc.networking.PlayerTargetNewEntityC2SPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.StartWorldTick;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Beetrapfabricmc implements ModInitializer {
	public static final String MOD_ID = "beetrap-fabricmc";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private BeetrapGameServer bgs;

	private boolean newBeetrapGame(MinecraftServer server) {
		if(bgs == null) {
			this.bgs = new BeetrapGameServer(server, new Vector3i(-10, 0, -10), new Vector3i(10, 0, 10));

			return true;
		}

		return false;
	}

	private boolean destroyBeetrapGame() {
		if(this.bgs == null) {
			return true;
		}

		this.bgs.dispose();
		this.bgs = null;
		return true;
	}

	private int gameCommand(CommandContext<ServerCommandSource> commandContext) {
		String arg1 = commandContext.getArgument("option", String.class);

		if(arg1.equalsIgnoreCase("new")) {
			return this.newBeetrapGame(commandContext.getSource().getServer()) ? 1 : 0;
		} else if(arg1.equalsIgnoreCase("destroy")) {
			return this.destroyBeetrapGame() ? 1 : 0;
		}

		return 0;
	}

	private CompletableFuture<Suggestions> getGameCommandSuggestions(
			CommandContext<ServerCommandSource> commandContext,
			SuggestionsBuilder builder) {
		builder.suggest("new");
		builder.suggest("destroy");
		return builder.buildFuture();
	}

	private void registerGameCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, RegistrationEnvironment registrationEnvironment) {
		dispatcher.register(CommandManager.literal("game")
				.then(CommandManager.argument("option", StringArgumentType.greedyString())
						.suggests(this::getGameCommandSuggestions)
						.executes(Beetrapfabricmc.this::gameCommand)));
	}

	private void onPlayerTargetEntity(PlayerTargetNewEntityC2SPayload payload, Context context) {
		if(this.bgs != null) {
			this.bgs.onPlayerTargetNewEntity(context.player(), payload.exists(), payload.entityId());
		}
	}

	private void onPlayerPollinate(PlayerPollinateC2SPayload payload,
			Context context) {
		if(this.bgs != null) {
			this.bgs.onPlayerPollinate(context.player(), payload.exists(), payload.entityId());
		}
	}

	private void onWorldTick(ServerWorld world) {
		if(this.bgs != null) {
			this.bgs.onWorldTick();
		}
	}

	@Override
	public void onInitialize() {
		ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
		CommandRegistrationCallback.EVENT.register(this::registerGameCommand);
		PayloadTypeRegistry.playC2S().register(PlayerTargetNewEntityC2SPayload.ID, PlayerTargetNewEntityC2SPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PlayerTargetNewEntityC2SPayload.ID, this::onPlayerTargetEntity);
		PayloadTypeRegistry.playC2S().register(PlayerPollinateC2SPayload.ID, PlayerPollinateC2SPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PlayerPollinateC2SPayload.ID, this::onPlayerPollinate);
	}
}