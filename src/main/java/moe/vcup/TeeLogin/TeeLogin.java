package moe.vcup.TeeLogin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import moe.vcup.TeeLogin.utils.LoginManager;
import moe.vcup.TeeLogin.utils.Messenger;
import moe.vcup.TeeLogin.utils.PasswordManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;


public class TeeLogin implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("teelogin");
	public static final Path ConfigDir = Paths.get("./config/TeeLogin");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
			dispatcher.register(literal("login")
				.then(argument("password", string())
					.executes(this::LoginCommand)
					.then(literal("register")
						.executes(this::RegisterCommand)
					)
				)
			);
		}));
		try {
			Files.createDirectories(ConfigDir);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	public int LoginCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var player = context.getSource().getPlayer();
		var password = getString(context, "password");
		if (!PasswordManager.playerIsRegistered(player)){
			Messenger.m(player, true, "????????????/login <password> register?????????");
		} else if (PasswordManager.playerIsNeedCheckPassword(player)){
			Messenger.m(player, true, "????????????/login <password> register??????????????????");
		} else if (PasswordManager.verifyPassword(player, password)){
			if (!LoginManager.LoggedPlayer(player)){
				Messenger.m(player, true, "?????????????????????????????????");
				return 0;
			}
			Messenger.m(player, true, "???????????????");
		} else {
			Messenger.m(player, true, "???????????????");
		}
		return 0;
	}

	public int RegisterCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var player = context.getSource().getPlayer();
		var password = getString(context, "password");
		if (PasswordManager.playerIsRegistered(player)){
			Messenger.m(player, true, "?????????????????????/login <password> ???????????????????????????????????????????????????");
		} else if (PasswordManager.playerIsNeedCheckPassword(player)){
			if (PasswordManager.setPlayerPassword(player, password)){
				Messenger.m(player, true, "?????????????????????/login <password> ???????????????");
			} else {
				Messenger.m(player, true, "?????????????????????????????????????????????");
			}
		} else {
			PasswordManager.setPlayerPassword(player, password);
			Messenger.m(player, true, "????????????/login <password> register ???????????????");
		}
		return 0;
	}
}
