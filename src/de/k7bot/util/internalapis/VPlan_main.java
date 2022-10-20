package de.k7bot.util.internalapis;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.awt.Color;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.k7bot.Klassenserver7bbot;
import de.k7bot.sql.LiteSQL;
import de.k7bot.subscriptions.types.SubscriptionTarget;
import de.k7bot.util.Cell;
import de.k7bot.util.TableMessage;
import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * 
 * @author felix
 * @Deprecated use {@link de.k7bot.util.internalapis.VplanNEW_XML VplanNEW_XML
 *             instead}
 */
@DeprecatedSince(value = "1.14.0")
public class VPlan_main {

	private final Logger log = Klassenserver7bbot.getInstance().getMainLogger();
	private String vplanpw;

	public VPlan_main(String pw) {
		vplanpw = pw;
	}

	/**
	 * 
	 * @param force
	 * @param klasse
	 * @param channel
	 * 
	 * @since 1.15.0
	 */
	public void sendVplanToChannel(boolean force, String klasse, TextChannel channel) {

		MessageCreateData d = getVplanMessage();

		if (d != null) {
			channel.sendMessage(d).queue();
		}

	}

	/**
	 * 
	 * @param klasse
	 * 
	 * @since 1.15.0
	 */
	public void VplanNotify(String klasse) {

		MessageCreateData d = getVplanMessage();

		if (d != null) {
			Klassenserver7bbot.getInstance().getSubscriptionManager()
					.provideSubscriptionNotification(SubscriptionTarget.VPLAN, d);
		}
	}

	/**
	 * See {@link de.k7bot.timed.Vplan_main Vplan_main}
	 */
	@DeprecatedSince(value = "1.14.0")
	private MessageCreateData getVplanMessage() {

		JsonObject plan = getPlan();

		List<JsonObject> fien = finalplancheck(plan);

		if (fien != null) {

			String info = plan.get("info").toString();

			info = info.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").trim();

			if (log != null) {
				log.debug("sending Vplanmessage with following hash: " + fien.hashCode() + " and devmode = "
						+ Klassenserver7bbot.getInstance().isDevMode());
			}

			EmbedBuilder embbuild = new EmbedBuilder();

			embbuild.setTitle("Es gibt einen neuen Vertretungsplan für "
					+ plan.get("head").getAsJsonObject().get("title").getAsString() + "\n");

			if (fien.isEmpty()) {

				embbuild.setTitle("**KEINE ÄNDERUNGEN :sob:**");

				if (!(info.equalsIgnoreCase(""))) {

					embbuild.addField("Sonstige Infos", info, false);

				}

			} else {

				TableMessage tablemess = new TableMessage();
				tablemess.addHeadline("Stunde", "Fach", "Lehrer", "Raum", "Info");

				fien.forEach(entry -> {

					JsonArray changes = entry.get("changed").getAsJsonArray();
					boolean subjectchange = false;
					boolean teacherchange = false;
					boolean roomchange = false;

					if (changes.size() != 0) {

						if (changes.toString().contains("subject")) {
							subjectchange = true;
						}
						if (changes.toString().contains("teacher")) {
							teacherchange = true;
						}
						if (changes.toString().contains("room")) {
							roomchange = true;
						}

					}

					tablemess.addCell(entry.get("lesson").getAsString().replaceAll("\"", ""));

					if (!entry.get("subject").toString().equalsIgnoreCase("\"---\"")) {

						Cell subject = Cell.of(entry.get("subject").getAsString().replaceAll("\"", ""),
								(subjectchange ? Cell.STYLE_BOLD : Cell.STYLE_NONE));
						Cell teacher = Cell.of(entry.get("teacher").getAsString().replaceAll("\"", ""),
								(teacherchange ? Cell.STYLE_BOLD : Cell.STYLE_NONE));

						StringBuilder strbuild = new StringBuilder();
						JsonElement elem = entry.get("teacher");

						if (elem != null) {
							JsonElement teachelem = Klassenserver7bbot.getInstance().getTeacherList().get(elem.getAsString()
									.replaceAll("\"", "").replaceAll("\\(", "").replaceAll("\\)", ""));

							if (teachelem != null) {

								JsonObject teach = teachelem.getAsJsonObject();

								String gender = teach.get("gender").getAsString();
								if (gender.equalsIgnoreCase("female")) {
									strbuild.append("Frau ");
								} else if (gender.equalsIgnoreCase("male")) {
									strbuild.append("Herr ");
								}

								if (teach.get("is_doctor").getAsBoolean()) {

									strbuild.append("Dr. ");

								}

								strbuild.append(teach.get("full_name").getAsString().replaceAll("\"", ""));

							}
						}

						teacher.setLinkTitle(strbuild.toString().trim());
						teacher.setLinkURL("https://manos-dresden.de/lehrer");

						Cell room = Cell.of(entry.get("room").getAsString().replaceAll("\"", ""),
								(roomchange ? Cell.STYLE_BOLD : Cell.STYLE_NONE));

						tablemess.addRow(subject, teacher, room);

					} else {

						tablemess.addRow(Cell.of("AUSFALL", Cell.STYLE_BOLD), "---", "---");

					}

					if (!entry.get("info").toString().equalsIgnoreCase("\"\"")) {

						tablemess.addCell(entry.get("info").getAsString().replaceAll("\"", ""));

					} else {
						tablemess.addCell("   ");
					}

				});

				tablemess.automaticLineBreaks(4);
				embbuild.setDescription("**Änderungen**\n" + tablemess.build());

				if (!(info.equalsIgnoreCase(""))) {

					embbuild.addField("Sonstige Infos", info, false);

				}

			}

			embbuild.setColor(Color.decode("#038aff"));
			embbuild.setFooter("Stand vom " + OffsetDateTime.now());

			LiteSQL.onUpdate("UPDATE vplannext SET classeintraege = ?;", fien.hashCode());

			return new MessageCreateBuilder().setEmbeds(embbuild.build()).build();

		}

		return null;
	}

	/**
	 * See {@link de.k7bot.timed.Vplan_main Vplan_main}
	 */
	@DeprecatedSince(value = "1.14.0")
	private List<JsonObject> finalplancheck(JsonObject plan) {

		Integer dbh = null;
		List<JsonObject> finalentries = new ArrayList<>();

		if (plan != null) {
			boolean synced;

			synced = synchronizePlanDB(plan);

			List<JsonObject> getC = getyourC(plan);
			if (getC != null) {
				int h = getC.hashCode();

				ResultSet set = LiteSQL.onQuery("SELECT classeintraege FROM vplannext;");
				try {
					if (set.next()) {

						dbh = set.getInt("classeintraege");

					}
					if (dbh != null) {
						if (dbh != h || synced) {

							finalentries = getC;

							String date = plan.get("head").getAsJsonObject().get("title").getAsString()
									.replaceAll(" ", "").replaceAll("\\(B-Woche\\)", "").replaceAll("\\(A-Woche\\)", "")
									.replaceAll(",", "").replaceAll("Montag", "").replaceAll("Dienstag", "")
									.replaceAll("Mittwoch", "").replaceAll("Donnerstag", "").replaceAll("Freitag", "")
									.toLowerCase();
							LiteSQL.onUpdate("UPDATE vplannext SET zieldatum = ?;", date);

						} else {
							return null;

						}
					} else {
						finalentries = getC;
						String date = plan.get("head").getAsJsonObject().get("title").getAsString().replaceAll(" ", "")
								.replaceAll("\\(B-Woche\\)", "").replaceAll("\\(A-Woche\\)", "").replaceAll(",", "")
								.replaceAll("Montag", "").replaceAll("Dienstag", "").replaceAll("Mittwoch", "")
								.replaceAll("Donnerstag", "").replaceAll("Freitag", "").toLowerCase();
						LiteSQL.onUpdate("INSERT INTO vplannext(zieldatum, classeintraege) VALUES(?, ?);", date,
								finalentries.hashCode());
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
				return finalentries;

			} else {
				return null;

			}
		} else {
			return null;
		}
	}

	/**
	 * See {@link de.k7bot.timed.Vplan_main Vplan_main}
	 */
	@DeprecatedSince(value = "1.14.0")
	private boolean synchronizePlanDB(JsonObject plan) {
		if (plan != null) {
			String dbdate = "";

			String onlinedate = plan.get("head").getAsJsonObject().get("title").getAsString().replaceAll(" ", "")
					.replaceAll("\\(B-Woche\\)", "").replaceAll("\\(A-Woche\\)", "").replaceAll(",", "")
					.replaceAll("Montag", "").replaceAll("Dienstag", "").replaceAll("Mittwoch", "")
					.replaceAll("Donnerstag", "").replaceAll("Freitag", "").toLowerCase();
			OffsetDateTime time = OffsetDateTime.now();
			String realdate = "" + time.getDayOfMonth() + "."
					+ time.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN).toLowerCase() + time.getYear();

			try {
				ResultSet next = LiteSQL.onQuery("SELECT zieldatum FROM vplannext;");
				if (next.next()) {

					dbdate = next.getString("zieldatum");
				}

				if (!(dbdate.equalsIgnoreCase(onlinedate)) && dbdate.equalsIgnoreCase(realdate)) {

					LiteSQL.getdblog().info("Plan-DB-Sync");

					ResultSet old = LiteSQL.onQuery("SELECT * FROM vplannext;");

					if (old.next()) {
						LiteSQL.onUpdate("UPDATE vplancurrent SET zieldatum = ?, classeintraege = ?;",
								old.getString("zieldatum"), old.getInt("classeintraege"));
						LiteSQL.onUpdate("UPDATE vplannext SET zieldatum = '', classeintraege = '';");
					}
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;

	}

	/**
	 * See {@link de.k7bot.timed.Vplan_main Vplan_main}
	 */
	@DeprecatedSince(value = "1.14.0")
	private List<JsonObject> getyourC(JsonObject obj) {
		List<JsonObject> classentries = new ArrayList<>();
		if (obj != null) {
			JsonArray arr = obj.get("body").getAsJsonArray();
			arr.forEach(element -> {
				String elem = element.getAsJsonObject().get("class").toString().replaceAll("\"", "");
				if (elem.contains("9b") || elem.equalsIgnoreCase("9a-9c/ Spw") || elem.equalsIgnoreCase("9a-9c")) {

					classentries.add(element.getAsJsonObject());

				}

			});
			return classentries;
		} else {
			return null;

		}

	}

	/**
	 * See {@link de.k7bot.timed.Vplan_main Vplan_main}
	 */
	@DeprecatedSince(value = "1.14.0")
	private JsonObject getPlan() {

		final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope("manos-dresden.de", 443),
				new UsernamePasswordCredentials("manos", vplanpw));
		try (final CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider)
				.build()) {
			final HttpGet httpget = new HttpGet("https://manos-dresden.de/vplan/upload/next/students.json");
			final CloseableHttpResponse response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == 200) {
				JsonElement elem = JsonParser.parseString(EntityUtils.toString(response.getEntity()));
				return elem.getAsJsonObject();
			} else {
				return null;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

}