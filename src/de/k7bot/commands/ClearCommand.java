
package de.k7bot.commands;

import de.k7bot.commands.types.ServerCommand;
import de.k7bot.util.PermissionError;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class ClearCommand implements ServerCommand {
	public void performCommand(Member m, TextChannel channel, Message message) {

		if (m.hasPermission(channel, Permission.MESSAGE_MANAGE)) {

			String[] args = message.getContentStripped().split(" ");

			if (args.length == 2) {

				int amount = Integer.parseInt(args[1]);

				onclear(amount, channel, m);

				TextChannel system = channel.getGuild().getSystemChannel();
				if (channel != system) {

					channel.sendMessage(amount + " messages deleted.").complete().delete()
							.queueAfter(3L, TimeUnit.SECONDS);

				}

				EmbedBuilder builder = new EmbedBuilder();
				builder.setColor(16345358);
				builder.setFooter("requested by @" + m.getEffectiveName());
				builder.setTimestamp(OffsetDateTime.now());
				builder.setDescription(amount + " messages deleted!\n\n" + "**Channel: **\n" + "#"
						+ channel.getName());
				system.sendMessageEmbeds(builder.build()).queue();

			}

		}

		else {

			PermissionError.onPermissionError(m, channel);

		}

	}

	public static void onclear(int amount, TextChannel chan, Member m) {
		try {

			chan.purgeMessages(get(chan, amount));

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public static List<Message> get(MessageChannel channel, int amount) {
		List<Message> messages = new ArrayList<>();
		int i = 0;

		for (Message message : channel.getIterableHistory().cache(false)) {
			if (!message.isPinned()) {
				messages.add(message);
			}

			if (i++ >= amount) {
				break;
			}
		}
		return messages;
	}
}
