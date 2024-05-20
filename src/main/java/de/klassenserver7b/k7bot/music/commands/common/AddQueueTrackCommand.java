/**
 *
 */
package de.klassenserver7b.k7bot.music.commands.common;

import de.klassenserver7b.k7bot.music.commands.generic.GenericPlayCommand;
import de.klassenserver7b.k7bot.music.lavaplayer.AudioLoadResult;
import de.klassenserver7b.k7bot.music.lavaplayer.MusicController;
import de.klassenserver7b.k7bot.music.utilities.AudioLoadOption;

/**
 * @author K7
 *
 */
public class AddQueueTrackCommand extends GenericPlayCommand {

	private boolean isEnabled;

	/**
	 *
	 */
	public AddQueueTrackCommand() {
		super();
		isEnabled = true;
	}

	@Override
	public String getHelp() {
		return "Lädt den/die ausgewählte/-n Track / Livestream / Playlist und fügt ihn/sie der aktuellen Queue hinzu.\n - z.B. [prefix]addtoqueue [url / YouTube Suchbegriff]";
	}

	@Override
	public String[] getCommandStrings() {
		return new String[] { "addtoqueue", "aq" };
	}

	@Override
	protected AudioLoadResult generateAudioLoadResult(MusicController controller, String url) {
		return new AudioLoadResult(controller, url, AudioLoadOption.APPEND);
	}

	@Override
	protected GenericPlayCommand getChildClass() {
		return this;
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
