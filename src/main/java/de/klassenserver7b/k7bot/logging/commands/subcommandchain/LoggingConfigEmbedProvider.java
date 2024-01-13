/**
 * 
 */
package de.klassenserver7b.k7bot.logging.commands.subcommandchain;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.logging.LoggingConfigDBHandler;
import de.klassenserver7b.k7bot.logging.LoggingOptions;
import de.klassenserver7b.k7bot.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

/**
 * 
 */
public class LoggingConfigEmbedProvider extends ListenerAdapter {

	private InteractionHook hook;
	private final long guildId;
	private LoggingOptions category;

	/**
	 * 
	 */
	public LoggingConfigEmbedProvider(InteractionHook hook) {
		this.hook = hook;
		this.guildId = hook.getInteraction().getGuild().getIdLong();

		hook.sendMessageEmbeds(buildCatSelectEmbed()).setComponents(buildCatSelectActionRows()).queue();
	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {

		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			return;
		}

		hook = event.deferEdit().complete();
		String compId = event.getComponentId();
		String matcher = compId.replaceAll("(.*)-(\\d+)?$", "$1");
		int optId = Integer.parseInt(event.getSelectedOptions().get(0).getValue().replace("logging-catid-", ""));

		switch (matcher) {

		case "logging-single-select" -> {
			LoggingConfigDBHandler.toggleOption(LoggingOptions.byId(optId), guildId);
		}

		case "logging-choose-category" -> {
			category = LoggingOptions.byId(optId);
		}

		default -> {
			return;
		}
		}

		sendCatOptionsEmbed();

	}

	@Override
	public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {

		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			return;
		}

		hook = event.deferEdit().complete();
		String compId = event.getComponentId();

		if (compId.equalsIgnoreCase("logging-conf-exit")) {
			exit();
			return;
		}

		if (!compId.startsWith("logging-cat-")) {
			return;
		}

		compId = compId.replace("logging-cat-", "");
		LoggingOptions option = LoggingOptions.byId(Integer.parseInt(compId.split("-")[1]));

		switch (compId.split("-")[0]) {

		case "disableall" -> {
			changeCat(option, false);
		}

		case "enableall" -> {
			changeCat(option, true);
		}

		case "back" -> {
			sendCatSelectEmbed();
			return;
		}

		default -> {
			return;
		}
		}

		sendCatOptionsEmbed();

	}

	protected void exit() {
		hook.deleteOriginal().queue();
		Klassenserver7bbot.getInstance().getShardManager().removeEventListener(this);
	}

	protected void sendCatSelectEmbed() {
		hook.editOriginalEmbeds(buildCatSelectEmbed()).setComponents(buildCatSelectActionRows()).queue();
	}

	protected MessageEmbed buildCatSelectEmbed() {
		EmbedBuilder embbuild = EmbedUtils.getDefault();
		embbuild.setTitle("Logging Config");
		embbuild.setColor(Color.blue);

		StringBuilder strbuild = new StringBuilder();
		strbuild.append("Please select the type of Logging you want to configure");
		strbuild.append("\n\n");
		strbuild.append("Available are:");
		strbuild.append("\n");

		for (LoggingOptions option : LoggingOptions.values()) {
			if (option.getId() % 10 == 0) {
				strbuild.append(option.toString());
				strbuild.append(",");
				strbuild.append("\n");
			}
		}

		embbuild.setDescription(strbuild);

		return embbuild.build();
	}

	protected List<LayoutComponent> buildCatSelectActionRows() {

		List<LayoutComponent> rows = new LinkedList<>();
		List<ItemComponent> strSelect = new LinkedList<>();

		StringSelectMenu.Builder strSelectBuilder = StringSelectMenu.create("logging-choose-category");

		for (LoggingOptions option : LoggingOptions.values()) {
			if (option.getId() % 10 == 0) {
				strSelectBuilder.addOption(option.toString(), "logging-catid-" + option.getId());
			}
		}

		strSelect.add(strSelectBuilder.build());

		rows.add(ActionRow.of(strSelect));
		rows.add(ActionRow.of(Button.danger("logging-conf-exit", "Exit")));

		return rows;
	}

	protected void sendCatOptionsEmbed() {

		int idRange = category.getId();

		List<Integer> catids = Arrays.asList(LoggingOptions.values()).stream()
				.filter(opt -> (opt.getId() > idRange && opt.getId() < idRange + 10)).map(opt -> opt.getId()).toList();

		hook.editOriginalEmbeds(buildCatOptionsEmbed(catids)).setComponents(buildCatOptionsActionRows(catids)).queue();
	}

	protected MessageEmbed buildCatOptionsEmbed(List<Integer> catids) {

		String catname = category.toString();

		EmbedBuilder embbuild = EmbedUtils.getDefault();
		embbuild.setColor(Color.blue);
		embbuild.setTitle("LoggingConfig - " + catname);

		StringBuilder strbuild = new StringBuilder();
		strbuild.append("Option");

		for (int i = 0; i < 30; i++) {
			strbuild.append(" ");
		}

		strbuild.append(" - State");
		strbuild.append("\n\n");

		for (int catid : catids) {

			LoggingOptions opt = LoggingOptions.byId(catid);

			strbuild.append(opt.toString());

			for (int i = 0; i < 30 - opt.toString().toCharArray().length; i++) {
				strbuild.append(" ");
			}

			strbuild.append(" - ");

			boolean enabled = LoggingConfigDBHandler.isOptionEnabled(opt, guildId);
			strbuild.append((enabled ? ":white_check_mark:" : ":x:"));

			strbuild.append("\n");

		}

		embbuild.setDescription(strbuild);

		return embbuild.build();
	}

	protected List<LayoutComponent> buildCatOptionsActionRows(List<Integer> catIds) {

		int idRange = category.getId();

		List<ItemComponent> buttonRow = new LinkedList<>();

		buttonRow.add(Button.primary("logging-cat-enableall-" + idRange, "Enable All"));
		buttonRow.add(Button.primary("logging-cat-disableall-" + idRange, "Disable All"));
		buttonRow.add(Button.danger("logging-cat-back-00", "Back"));

		StringSelectMenu.Builder strSelectBuilder = StringSelectMenu.create("logging-single-select");

		for (int catid : catIds) {
			strSelectBuilder.addOption(LoggingOptions.byId(catid).toString(), "logging-catid-" + catid);
		}

		List<LayoutComponent> rows = new LinkedList<>();
		rows.add(ActionRow.of(buttonRow));
		rows.add(ActionRow.of(strSelectBuilder.build()));

		return rows;
	}

	protected void changeCat(LoggingOptions cat, boolean enable) {

		for (int optId = cat.getId() + 1; optId < cat.getId() + 10; optId++) {

			LoggingOptions option;
			if ((option = LoggingOptions.byId(optId)) != LoggingOptions.UNKNOWN) {

				if (enable) {
					LoggingConfigDBHandler.enableOption(option, guildId);
				} else {
					LoggingConfigDBHandler.disableOption(option, guildId);
				}
			}

		}

	}

}
