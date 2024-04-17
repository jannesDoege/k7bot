/**
 *
 */
package de.klassenserver7b.k7bot.util.customapis;

import de.klassenserver7b.k7bot.Klassenserver7bbot;
import de.klassenserver7b.k7bot.sql.LiteSQL;
import de.klassenserver7b.k7bot.subscriptions.types.SubscriptionTarget;
import de.klassenserver7b.k7bot.util.*;
import de.klassenserver7b.k7bot.util.customapis.types.LoopedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.io.CloseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author K7
 * @since 1.14.0
 */
public class Stundenplan24Vplan implements LoopedEvent {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final List<String> classes;

	public Stundenplan24Vplan() {
		classes = new ArrayList<>();
	}

	public Stundenplan24Vplan(String... klassen) {
		this.classes = new ArrayList<>();
        Collections.addAll(this.classes, klassen);
	}

	public void registerKlassen(String... klassen) {
        this.classes.addAll(Arrays.asList(klassen));
	}

	/**
	 *
	 * @param force
	 * @param klasse
	 * @param channel
	 *
	 * @since 1.15.0
	 */
	public void sendVplanToChannel(boolean force, String klasse, GuildMessageChannel channel) {

		try (MessageCreateData d = getVplanMessage(force, klasse)) {
			if (d != null) {
				channel.sendMessage(d).queue();
			}
		}

	}

	/**
	 *
	 */
	@Override
	public int checkforUpdates() {
		for (String klasse : classes) {
			vplanNotify(klasse);
		}
		return InternalStatusCodes.SUCCESS;
	}

	/**
	 *
	 * @param klasse
	 *
	 * @since 1.15.0
	 */
	public boolean vplanNotify(String klasse) {

		try (MessageCreateData d = getVplanMessage(false, klasse)) {

			if (d == null) {
				return false;
			}

			Klassenserver7bbot.getInstance().getSubscriptionManager()
					.provideSubscriptionNotification(SubscriptionTarget.VPLAN, d);
		}
		return true;
	}

	/**
	 * 
	 * @param force
	 * @param klasse
	 * @since 1.14.0
	 * @return
	 */
	private MessageCreateData getVplanMessage(boolean force, String klasse) {

		OffsetDateTime d = checkDate();
		Document doc = read(d);

		Element classPlan = getYourClass(doc, klasse);
		boolean sendApproved = force || (doc != null && checkPlanChanges(doc, classPlan));

		if (!sendApproved || doc == null) return null;

		putInDB(doc);
		String info = "";

		if (doc.getElementsByTagName("ZiZeile").getLength() != 0) {
			info = doc.getElementsByTagName("ZiZeile").item(0).getTextContent();
		}

		log.info("sending Vplanmessage with following hash: " + classPlan.hashCode() + " and devmode = "
				+ Klassenserver7bbot.getInstance().isDevMode());

		EmbedBuilder embed = EmbedUtils.getBuilderOf(Color.decode("#038aff"));

		embed.setTitle("Es gibt einen neuen Stundenplan für "
				+ doc.getElementsByTagName("DatumPlan").item(0).getTextContent() + " (" + klasse + ")");
		embed.setFooter("Stand vom " + doc.getElementsByTagName("zeitstempel").item(0).getTextContent());

		/*
		 * TableMessage tablemess = new TableMessage(); tablemess.addHeadline("Stunde",
		 * "Fach", "Lehrer", "Raum", "Info");
		 *
		 * NodeList lessons = classPlan.getElementsByTagName("Std");
		 *
		 * int limit = 6; int ges = lessons.getLength();
		 *
		 * if (lessons.getLength() < limit) { limit = lessons.getLength(); }
		 *
		 * for (int i = 0; i < limit; i++) { Element e = (Element) lessons.item(i);
		 * appendLesson(e, tablemess);
		 *
		 * } TableMessage additionalmess = new TableMessage();
		 * additionalmess.setColums(5);
		 *
		 * for (int i = limit; i < ges; i++) { Element e = (Element) lessons.item(i);
		 * appendLesson(e, additionalmess);
		 *
		 * }
		 *
		 * tablemess.automaticLineBreaks(4); embed.setDescription("**Änderungen**\n"
		 * + tablemess.build());
		 *
		 * boolean isextraembed = false;
		 *
		 * if (additionalmess.hasData()) { additionalmess.automaticLineBreaks(4);
		 *
		 * if (additionalmess.build().length() <= 1020) { embed.addField("",
		 * additionalmess.build(), false); } else { isextraembed = true; } }
		 */

		if (!(info.equalsIgnoreCase(""))) {
			embed.addField("Sonstige Infos", info, false);
		}

		LiteSQL.onUpdate("UPDATE vplannext SET classEntrys = ?;", classPlan.getTextContent().hashCode());

		MessageCreateBuilder builder = new MessageCreateBuilder();
		builder.setEmbeds(embed.build());

		/*
		 * if (isextraembed) {
		 *
		 * embed.clearFields(); embed.setFooter(null);
		 *
		 *
		 * EmbedBuilder addbuild = EmbedUtils.getBuilderOf(Color.decode("#038aff"),
		 * additionalmess.build());
		 *
		 * addbuild.setFooter("Stand vom " +
		 * doc.getElementsByTagName("zeitstempel").item(0).getTextContent());
		 *
		 * if (!(info.equalsIgnoreCase(""))) {
		 *
		 * addbuild.addField("Sonstige Infos", info, false);
		 *
		 * }
		 *
		 * builder.addEmbeds(addbuild.build());
		 *
		 * } else { builder.setEmbeds(embed.build()); }
		 */

		return builder.build();
	}

	private void putInDB(Document doc) {

		NodeList stdList = doc.getElementsByTagName("Std");

		LiteSQL.onUpdate("DELETE FROM vplandata");

		for (int i = 0; i < stdList.getLength(); i++) {

			Element n = (Element) stdList.item(i);

			int lesson = Integer.parseInt(n.getElementsByTagName("St").item(0).getTextContent());
			String room = n.getElementsByTagName("Ra").item(0).getTextContent();
			String teacher = n.getElementsByTagName("Le").item(0).getTextContent();

			if (room.contains("&amp;nbsp;") || room.contains("nbsp;") || room.isBlank()) {
				continue;
			}
			LiteSQL.onUpdate("INSERT INTO vplandata(lesson, room, teacher) VALUES(?, ?, ?)", lesson, room, teacher);

		}
	}

	/**
	 *
	 * @param e
	 * @param tablemess
	 * @return
	 */
	@SuppressWarnings("unused")
	private TableMessage appendLesson(Element e, TableMessage tablemess) {
		TableMessage ret;

		boolean subjectChanged = e.getElementsByTagName("Fa").item(0).hasAttributes();
		boolean teacherChanged = e.getElementsByTagName("Le").item(0).hasAttributes();
		boolean roomChanged = e.getElementsByTagName("Ra").item(0).hasAttributes();
		String lesson = e.getElementsByTagName("St").item(0).getTextContent();

		if (!e.getElementsByTagName("Fa").item(0).getTextContent().equalsIgnoreCase("---")) {

			Cell subjectcell = Cell.of(e.getElementsByTagName("Fa").item(0).getTextContent(),
					(subjectChanged ? Cell.STYLE_BOLD : Cell.STYLE_NONE));
			Cell teachercell = Cell.of(e.getElementsByTagName("Le").item(0).getTextContent(),
					(teacherChanged ? Cell.STYLE_BOLD : Cell.STYLE_NONE));

			String teacherId = e.getElementsByTagName("Le").item(0).getTextContent();
			TeacherDB.Teacher teacher = Klassenserver7bbot.getInstance().getTeacherDB().getTeacher(teacherId);

			if (teacher != null) {
				teachercell.setLinkTitle(teacher.getDecoratedName());
				teachercell.setLinkURL("https://manos-dresden.de/lehrer");
			}

			Cell room = Cell.of(e.getElementsByTagName("Ra").item(0).getTextContent(),
					(roomChanged ? Cell.STYLE_BOLD : Cell.STYLE_NONE));

			tablemess.addRow(lesson, subjectcell, teachercell, room);

		} else {

			tablemess.addRow(lesson, Cell.of("AUSFALL", Cell.STYLE_BOLD), "---", "---");

		}

		if (!e.getElementsByTagName("If").item(0).getTextContent().equalsIgnoreCase("")) {

			tablemess.addCell(e.getElementsByTagName("If").item(0).getTextContent());

		} else {
			tablemess.addCell("   ");
		}

		ret = tablemess;

		return ret;
	}

	/**
	 *
	 * @param plan
	 * @param classPlan
	 * @return
	 * @since 1.14.0
	 */
	private boolean checkPlanChanges(Document plan, Element classPlan) {

		log.debug("PLAN DB CHECK");

		Integer dbhash = null;

		if (plan == null || classPlan == null) {
			return false;
		}

		boolean synced = synchronizePlanDB(plan);

		int planHash = classPlan.getTextContent().hashCode();

		try (ResultSet set = LiteSQL.onQuery("SELECT classEntrys FROM vplannext;")) {

			if (set.next()) {

				dbhash = set.getInt("classEntrys");

			}

			String onlinedate = plan.getElementsByTagName("datei").item(0).getTextContent();
			onlinedate = onlinedate.replaceAll("WPlanKl_", "").replaceAll(".xml", "");

			if (dbhash != null) {

				if (dbhash != planHash || synced) {

					LiteSQL.onUpdate("UPDATE vplannext SET targetDate = ?;", onlinedate);
					return true;

				}
				return false;
			}
			LiteSQL.onUpdate("INSERT INTO vplannext(targetDate, classEntrys) VALUES(?, ?);", onlinedate, planHash);

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return true;

	}

	/**
	 *
	 * @param obj
	 * @param clazz
	 * @return
	 * @since 1.14.0
	 */
	private Element getYourClass(Document obj, String clazz) {

		if (obj == null) {
			return null;
		}
		NodeList nList = obj.getElementsByTagName("Kl");

		Element yourclass = null;
		for (int i = 0; i < nList.getLength(); i++) {

			Node n = nList.item(i);

			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;

				if (e.getElementsByTagName("Kurz").item(0).getTextContent().equalsIgnoreCase(clazz)) {
					yourclass = e;
					break;
				}

			}

		}

		return yourclass;
	}

	/**
	 *
	 * @param plan
	 * @return
	 * @since 1.14.0
	 */
	private boolean synchronizePlanDB(Document plan) {
		if (plan == null) {
			return false;
		}
		String dbdate = "";

		String onlinedate = plan.getElementsByTagName("datei").item(0).getTextContent();
		onlinedate = onlinedate.replaceAll("WPlanKl_", "").replaceAll(".xml", "");

		try (ResultSet next = LiteSQL.onQuery("SELECT targetDate FROM vplannext;")) {

			if (next.next()) {
				dbdate = next.getString("targetDate");
			}

			if (dbdate.equalsIgnoreCase(onlinedate)) {
				return false;
			}

			LiteSQL.getdblog().info("Plan-DB-Sync");

			try (ResultSet old = LiteSQL.onQuery("SELECT * FROM vplannext;")) {

				if (old.next()) {
					LiteSQL.onUpdate("UPDATE vplancurrent SET targetDate = ?, classEntrys = ?;",
							old.getString("targetDate"), old.getInt("classEntrys"));
					LiteSQL.onUpdate("UPDATE vplannext SET targetDate = '', classEntrys = '';");
				}
			}
			return true;

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		return false;

	}

	/**
	 *
	 * @return
	 * @since 1.14.0
	 */
	private OffsetDateTime checkDate() {

		OffsetDateTime now = OffsetDateTime.now();
		int day = now.getDayOfWeek().getValue();

		if (day >= 5) {
			return now.plusDays(8 - day);
		}

		return now.plusDays(1);

	}

	/**
	 *
	 * @param date
	 * @return
	 * @since 1.14.0
	 */
	private Document read(@Nonnull OffsetDateTime date) {
		if (date == null) {
			log.error("DocumentReadError - date = null caused by\n", new NullPointerException());
			return null;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
		try {

			String xml = getVplanXML(date);

			if (xml != null) {

				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

				doc.getDocumentElement();
				return doc;
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 *
	 * @param date
	 * @return
	 * @since 1.14.0
	 */
	private String getVplanXML(OffsetDateTime date) {

		final BasicCredentialsProvider credProvider = new BasicCredentialsProvider();
		credProvider.setCredentials(new AuthScope("www.stundenplan24.de", 443),
				new UsernamePasswordCredentials("schueler",
						Klassenserver7bbot.getInstance().getPropertiesManager().getProperty("vplanpw").toCharArray()));

		try (final CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credProvider)
				.build()) {

			final HttpGet httpget = new HttpGet("https://www.stundenplan24.de/"
					+ Klassenserver7bbot.getInstance().getPropertiesManager().getProperty("schoolID")
					+ "/wplan/wdatenk/WPlanKl_" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xml");

			final String response = httpclient.execute(httpget, new BasicHttpClientResponseHandler());
			httpclient.close(CloseMode.GRACEFUL);
			return response;

		} catch (HttpHostConnectException | HttpResponseException e1) {
			log.debug("Vplan Connection failed!" + e1.getMessage());
		} catch (IOException e) {
			log.error("Vplan IO Exception - please check your connection and settings");
			log.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 *
	 */
	@Override
	public void shutdown() {
		classes.clear();

	}

	@Override
	public boolean restart() {
		log.debug("restart requested");
		return true;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public String getIdentifier() {
		return "vplan";
	}

}
