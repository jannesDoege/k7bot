package de.k7bot.manage;

import de.k7bot.Klassenserver7bbot;
import de.k7bot.commands.slash.HA3MembersCommand;
import de.k7bot.commands.slash.HelpSlashCommand;
import de.k7bot.commands.slash.PingSlashCommand;
import de.k7bot.commands.slash.Shutdownslashcommand;
import de.k7bot.commands.slash.WhitelistSlashCommand;
import de.k7bot.commands.types.SlashCommand;
import de.k7bot.music.commands.slash.ChartsSlashCommand;
import de.k7bot.music.commands.slash.EqualizerSlashCommand;
import de.k7bot.music.commands.slash.PlaySlashCommand;
import de.k7bot.subscriptions.commands.SubscribeSlashCommand;
import de.k7bot.subscriptions.commands.UnSubscribeSlashCommand;
import de.k7bot.util.commands.slash.ClearSlashCommand;
import de.k7bot.util.commands.slash.ReactRolesSlashCommand;
import de.k7bot.util.commands.slash.ToEmbedSlashCommand;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class SlashCommandManager {

	public ConcurrentHashMap<String, SlashCommand> commands;
	public final Logger commandlog = LoggerFactory.getLogger("Commandlog");

	public SlashCommandManager() {

		this.commands = new ConcurrentHashMap<>();

		this.commands.put("help", new HelpSlashCommand());
		this.commands.put("clear", new ClearSlashCommand());
		this.commands.put("shutdown", new Shutdownslashcommand());
		this.commands.put("ping", new PingSlashCommand());
		this.commands.put("toembed", new ToEmbedSlashCommand());
		this.commands.put("reactrole", new ReactRolesSlashCommand());
		this.commands.put("play", new PlaySlashCommand());
		this.commands.put("charts", new ChartsSlashCommand());
		this.commands.put("subscribe", new SubscribeSlashCommand());
		this.commands.put("unsubscribe", new UnSubscribeSlashCommand());
		this.commands.put("equalizer", new EqualizerSlashCommand());
		this.commands.put("whitelistadd", new WhitelistSlashCommand());
		this.commands.put("ha3members", new HA3MembersCommand());

		Klassenserver7bbot.getInstance().getShardManager().getShards().forEach(shard -> {
			CommandListUpdateAction commup = shard.updateCommands();

			commands.values().forEach(command -> {

				commup.addCommands(command.getCommandData());

			});

			commup.complete();

		});
	}

	public boolean perform(SlashCommandInteraction event) {
		SlashCommand cmd;
		if ((cmd = this.commands.get(event.getName().toLowerCase())) != null) {

			String guild = "PRIVATE";
			if (event.getGuild() != null) {
				guild = event.getGuild().getName();
			}

			commandlog.info("SlashCommand - see next lines:\n\nUser: " + event.getUser().getName() + " | \nGuild: "
					+ guild + " | \nChannel: " + event.getChannel().getName() + " | \nMessage: "
					+ event.getCommandString() + "\n");

			cmd.performSlashCommand(event);

			return true;
		}
		return false;
	}
}
