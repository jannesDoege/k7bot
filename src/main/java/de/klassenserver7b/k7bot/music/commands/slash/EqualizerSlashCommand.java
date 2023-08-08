/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.slash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.commands.types.TopLevelSlashCommand;
import de.klassenserver7b.k7bot.music.utilities.BotAudioEffectsManager;
import de.klassenserver7b.k7bot.music.utilities.EqualizerPreset;
import de.klassenserver7b.k7bot.music.utilities.MusicUtil;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import de.klassenserver7b.k7bot.util.GenericMessageSendHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * @author Klassenserver7b
 *
 */
public class EqualizerSlashCommand implements TopLevelSlashCommand {

	@Override
	public void performSlashCommand(SlashCommandInteraction event) {

		String preset = event.getOption("preset").getAsString();
		InteractionHook hook = event.deferReply(true).complete();

		if (!MusicUtil.checkConditions(new GenericMessageSendHandler(hook), event.getMember())) {
			return;
		}

		BotAudioEffectsManager eq = BotAudioEffectsManager.getAudioEffectsManager(Klassenserver7bbot.getInstance()
				.getPlayerUtil().getController(event.getGuild().getIdLong()).getPlayer());

		EqualizerPreset[] vals = EqualizerPreset.values();

		for (EqualizerPreset val : vals) {
			if (val.toString().equalsIgnoreCase(preset)) {
				eq.setEQMode(val);
				hook.sendMessageEmbeds(EmbedUtils.getBuilderOf(Color.green,
						"Equalizer was sucessful set to preset '" + preset + "'", event.getGuild().getIdLong()).build())
						.queue();
				return;
			}
		}

		hook.sendMessageEmbeds(EmbedUtils.getBuilderOf(Color.red,
				"Something went wrong! Please try again and contact the bot support if this keeps happening.",
				event.getGuild().getIdLong()).build()).queue();

	}

	@Override
	public @NotNull SlashCommandData getCommandData() {

		List<Choice> choices = new ArrayList<>();

		EqualizerPreset[] vals = EqualizerPreset.values();

		for (EqualizerPreset val : vals) {

			if (val != EqualizerPreset.UNKNOWN) {
				choices.add(new Choice(val.toString(), val.toString()));
			}
		}

		return Commands.slash("equalizer", "used to set up the equalizer of the bot.")
				.addOptions(new OptionData(OptionType.STRING, "preset", "please select an equalizer preset.")
						.addChoices(choices).setRequired(true))
				.setGuildOnly(true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT));
	}

}
