package de.k7bot.commands.slash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import de.k7bot.HelpCategories;
import de.k7bot.commands.common.HelpCommand;
import de.k7bot.commands.types.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class HelpSlashCommand implements SlashCommand {

	public void performSlashCommand(SlashCommandInteraction event) {

		HelpCommand help = new HelpCommand();

		InteractionHook hook = event.deferReply(true).complete();

		hook.sendMessage("** look into your DM's **" + event.getUser().getAsMention()
				+ "\n (Only available if you have the option `get DM's from server members` in the `Privacy & Safety` settings enabled!)")
				.queue();

		OptionMapping catopt = event.getOption("category");

		MessageEmbed embed;

		if (catopt == null || catopt.getAsString().equalsIgnoreCase(HelpCategories.OVERVIEW.toString())) {
			embed = help.generateHelpOverview(event.getGuild());
		} else {
			embed = help.generateHelpforCategory(catopt.getAsString(), event.getGuild());
		}

		PrivateChannel ch = event.getUser().openPrivateChannel().complete();

		if (ch == null) {

			MessageEmbed errorembed = new EmbedBuilder().setColor(Color.decode("#ff0000")).setDescription(
					"Couldn't send you a DM - please check if you have the option `get DM's from server members` in the `Privacy & Safety` settings enabled!")
					.build();

			hook.sendMessageEmbeds(errorembed).complete().delete().queueAfter(20, TimeUnit.SECONDS);

		}

		ch.sendMessageEmbeds(embed).queue();

	}

	@Override
	public @NotNull SlashCommandData getCommandData() {

		ArrayList<Choice> choices = new ArrayList<>();

		for (HelpCategories c : HelpCategories.values()) {

			if (c == HelpCategories.UNKNOWN) {
				continue;
			}

			choices.add(new Choice(c.toString(), c.toString()));

		}

		return Commands.slash("help", "Gibt dir die Hilfe-Liste aus.")
				.addOptions(new OptionData(OptionType.STRING, "category",
						"Wähle die Kategorie aus -> Overview für die Kategorieübersicht").setRequired(false)
						.addChoices(choices));
	}
}