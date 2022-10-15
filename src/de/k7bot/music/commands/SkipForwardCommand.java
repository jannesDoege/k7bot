package de.k7bot.music.commands;

import de.k7bot.Klassenserver7bbot;
import de.k7bot.commands.types.ServerCommand;
import de.k7bot.music.MusicController;
import de.k7bot.music.utilities.MusicUtil;
import de.k7bot.util.errorhandler.SyntaxError;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class SkipForwardCommand implements ServerCommand {

	@Override
	public String gethelp() {
		return "Spult zur um die gewählte Anzahl an Sekunden vor.\n - z.B. [prefix]forward [time in seconds]";
	}

	@Override
	public String getcategory() {
		return "Musik";
	}

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		if(!MusicUtil.checkConditions(channel, m)) {
			return;
		}
		
		String[] args = message.getContentDisplay().split(" ");

		if (args.length < 2) {
			SyntaxError.oncmdSyntaxError(channel, "seek [position in seconds]", m);
			return;
		}

		MusicController controller = Klassenserver7bbot.INSTANCE.getPlayerUtil().getController(m.getGuild().getIdLong());
		int pos = Integer.valueOf(args[1]);
		controller.forward(pos * 1000);

	}


}
