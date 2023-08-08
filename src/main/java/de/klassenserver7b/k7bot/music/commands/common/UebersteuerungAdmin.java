package de.klassenserver7b.k7bot.music.commands.common;

import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import de.klassenserver7b.k7bot.HelpCategories;
import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.commands.types.ServerCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import de.klassenserver7b.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class UebersteuerungAdmin implements ServerCommand {

	private boolean isEnabled;

	@Override
	public String gethelp() {
		return null;
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "uvolume" };
	}

	@Override
	public HelpCategories getcategory() {
		return HelpCategories.UNKNOWN;
	}

	@Override
	public void performCommand(Member m, GuildMessageChannel channel, Message message) {
		if (m.hasPermission(Permission.ADMINISTRATOR)) {

			String[] args = message.getContentDisplay().split(" ");
			try {
				int volume = Integer.parseInt(args[1]);
				MusicUtil.updateChannel(channel);
				if (args.length > 1) {
					Guild guild = channel.getGuild();
					MusicController controller = Klassenserver7bbot.getInstance().getPlayerUtil()
							.getController(guild.getIdLong());
					AudioPlayer player = controller.getPlayer();
					player.setVolume(volume);
					EmbedBuilder builder = EmbedUtils.getDefault(channel.getGuild().getIdLong());
					builder.setFooter("Requested by @" + m.getEffectiveName());
					builder.setTitle("Volume was set to " + volume);
					channel.sendMessageEmbeds(builder.build()).complete().delete().queueAfter(10L, TimeUnit.SECONDS);

				} else {
					SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "volume [int]", m);
				}
			} catch (NumberFormatException e) {
				SyntaxError.oncmdSyntaxError(new GenericMessageSendHandler(channel), "volume [int]", m);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void disableCommand() {
		isEnabled = false;
	}

	@Override
	public void enableCommand() {
		isEnabled = true;
	}

}
