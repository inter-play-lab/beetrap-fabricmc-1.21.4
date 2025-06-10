package beetrap.btfmc;

import beetrap.btfmc.handler.BeetrapGameHandler;
import beetrap.btfmc.handler.CommandHandler;
import beetrap.btfmc.handler.EntityHandler;
import beetrap.btfmc.handler.NetworkHandler;
import beetrap.btfmc.openai.OpenAiUtil;
import net.fabricmc.api.ModInitializer;

public class Beetrapfabricmc implements ModInitializer {
	public static final String MOD_ID = "beetrap-fabricmc";

	@Override
	public void onInitialize() {
		OpenAiUtil.load();
		BeetrapGameHandler.registerEvents();
		CommandHandler.registerCommands();
		NetworkHandler.registerCustomPayloads();
		EntityHandler.registerEntities();
	}

	public static String id(String name) {
		return MOD_ID + ":" + name;
	}
}
