package de.klassenserver7b.k7bot.listener;

import de.klassenserver7b.k7bot.sql.LiteSQL;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class BotLeaveGuildListener extends ListenerAdapter {

	@Override
	public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
		long guildid = event.getGuild().getIdLong();

		LiteSQL.onUpdate("DELETE FROM musicutil WHERE guildId = ?;", guildid);
		LiteSQL.onUpdate("DELETE FROM statschannels WHERE guildId = ?;", guildid);
		LiteSQL.onUpdate("DELETE FROM botutil WHERE guildId = ?;", guildid);
		LiteSQL.onUpdate("DELETE FROM reactroles WHERE guildId = ?;", guildid);

	}

	@Override
	public void onUnavailableGuildLeave(@Nonnull UnavailableGuildLeaveEvent event) {
		long guildid = event.getGuildIdLong();

		LiteSQL.onUpdate("DELETE FROM musicutil WHERE guildId = ?;", guildid);
		LiteSQL.onUpdate("DELETE FROM statschannels WHERE guildId = ?;", guildid);
		LiteSQL.onUpdate("DELETE FROM botutil WHERE guildId = ?;", guildid);
		LiteSQL.onUpdate("DELETE FROM reactroles WHERE guildId = ?;", guildid);
	}
}