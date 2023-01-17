package de.k7bot.util.customapis;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.k7bot.Klassenserver7bbot;
import de.k7bot.manage.PropertiesManager;
import de.k7bot.sql.LiteSQL;
import de.k7bot.subscriptions.types.SubscriptionTarget;
import de.k7bot.util.customapis.types.InternalAPI;
import de.konsl.webweaverapi.WebWeaverClient;
import de.konsl.webweaverapi.WebWeaverException;
import de.konsl.webweaverapi.model.auth.Credentials;
import de.konsl.webweaverapi.model.messages.Message;
import de.konsl.webweaverapi.model.messages.MessageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * Used to interact with the LernsaxAPI <br>
 * Provides methods to:<br>
 * - {@link LernsaxInteractions#connect(String, String, String) login}<br>
 * - {@link LernsaxInteractions#checkForLernplanMessages() check for new
 * "Learning Plans"} <br>
 * - {@link LernsaxInteractions#disconnect() disconnect}
 *
 * @author Klassenserver7b
 *
 */
public class LernsaxInteractions implements InternalAPI {
	WebWeaverClient client;
	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private int ecount = 0;

	/**
	 * Used to connect to the LernsaxAPI via the credentials given in the configfile
	 */
	public void connect() {

		PropertiesManager propMgr = Klassenserver7bbot.getInstance().getPropertiesManager();

		this.client = new WebWeaverClient();
		Credentials cred = new Credentials(propMgr.getProperty("lsaxemail"), propMgr.getProperty("lsaxtoken"),
				propMgr.getProperty("lsaxappid"));

		try {
			client.login(cred);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	/**
	 * Used to connect to the LernsaxAPI via.
	 *
	 * @param lsaxemail     The email of your Lernsax account
	 * @param token         The token for this application
	 * @param applicationID The Id of this application
	 */
	public void connect(String lsaxemail, String token, String applicationID) {

		this.client = new WebWeaverClient();
		Credentials cred = new Credentials(lsaxemail, token, applicationID);

		client.login(cred);

	}

	/**
	 * Disconnects this client from the API
	 */
	public void disconnect() {
		client.logout();
		client = null;
	}

	/**
	 *
	 */
	@Override
	public void checkforUpdates() {

		if (client == null || ecount >= 15) {
			ecount = 0;
			connect();
		}

		List<Message> messages = checkForLernplanMessages();

		if (!messages.isEmpty()) {
			sendLernsaxEmbeds(messages);
		}

	}

	/**
	 * Checks if there are any new "Lernplaene" -> "LearningPlans"<br>
	 * Should only be used in connection with
	 * {@link LernsaxInteractions#sendLernsaxEmbeds(List)}
	 *
	 * @return A List of all new LearningPlans (empty if there are none)
	 * @throws SQLException
	 */
	private List<Message> checkForLernplanMessages() {

		List<Message> messages = new ArrayList<>();

		ResultSet set = LiteSQL.onQuery("Select LernplanId from lernsaxinteractions;");

		String currentMessageID = null;

		try {
			if (set.next()) {
				currentMessageID = set.getString("LernplanId");
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		if (currentMessageID == null) {

			try {
				messages = client.getMessagesScope().getMessages();

				LiteSQL.onUpdate("INSERT INTO lernsaxinteractions(LernplanId) VALUES(?);",
						messages.get(messages.size() - 1).getId());
			} catch (WebWeaverException e) {
				log.error(e.getMessage(), e);
			}

		} else {
			try {
				messages = client.getMessagesScope().getMessages(Integer.parseInt(currentMessageID));

				if (messages.size() > 0)
					messages = messages.stream().skip(1).toList();

				if (messages.size() > 0) {
					LiteSQL.onUpdate("UPDATE lernsaxinteractions SET LernplanId=?;",
							messages.get(messages.size() - 1).getId());
				}
			} catch (NumberFormatException | WebWeaverException e) {
				if (e.getMessage().equalsIgnoreCase("null")) {
					log.warn("Lernsax API request failed");
					ecount++;
				} else {
					log.error(e.getMessage(), e);
					ecount++;
				}
			} catch (NullPointerException e) {
				log.warn("Lernsax API request failed");
				ecount++;
			}
		}

		if (messages.size() <= 0) {
			return messages;
		}

		messages = messages.stream().filter(msg -> msg.getType() == MessageType.NEW_LEARNING_PLAN).toList();

		return messages;
	}

	/**
	 * Sends the given List of "lerplanmessages" obtained by
	 * {@link LernsaxInteractions#checkForLernplanMessages()} to every "Lernsax"
	 * subscribing channel
	 *
	 * @param lernplanmessages The List obtained by
	 *                         {@link LernsaxInteractions#checkForLernplanMessages()}
	 */
	public void sendLernsaxEmbeds(List<Message> lernplanmessages) {

		if (lernplanmessages == null) {
			log.warn("Illegal lernplanmessages list provided - List is null");
			return;
		}

		if (lernplanmessages.size() > 0) {

			log.info("Sending " + lernplanmessages.size() + " new Lernplan-Embeds");
			ArrayList<MessageEmbed> embeds = new ArrayList<>();

			lernplanmessages.forEach(msg -> {

				String linkURLQuery = null;
				try {
					linkURLQuery = URLEncoder.encode(
							"learning_plan|" + msg.getFromGroup().getLogin() + "|" + msg.getData() + "|/", "UTF-8");
				} catch (UnsupportedEncodingException ignored) {
				}

				embeds.add(new EmbedBuilder()
						.setTitle("Ein neuer Lernplan wurde bereitgestellt!",
								"https://" + client.getRemoteHost() + "/l.php?" + linkURLQuery)
						.addField("Name", msg.getData(), false)
						.addField("Klasse / Gruppe", msg.getFromGroup().getName(), false)
						.addField("Autor", msg.getFromUser().getName(), false).build());

			});

			for (MessageEmbed e : embeds) {

				MessageCreateData data = new MessageCreateBuilder().setEmbeds(e).build();

				Klassenserver7bbot.getInstance().getSubscriptionManager()
						.provideSubscriptionNotification(SubscriptionTarget.LERNPLAN, data);
			}

		}

	}

	/**
	 * Used to shutdown the lernsaxAPI
	 */
	@Override
	public void shutdown() {
		disconnect();

	}

}
