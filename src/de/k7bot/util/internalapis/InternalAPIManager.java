/**
 * 
 */
package de.k7bot.util.internalapis;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.k7bot.Klassenserver7bbot;
import de.k7bot.manage.PropertiesManager;
import de.k7bot.util.internalapis.types.ApiEntry;
import de.k7bot.util.internalapis.types.InternalAPI;

/**
 * @author Felix
 *
 */
public class InternalAPIManager {

	HashMap<String, ApiEntry> apis;
	private final Logger log = LoggerFactory.getLogger(this.getClass().getCanonicalName());

	/**
	 * 
	 */
	public InternalAPIManager() {
		apis = new HashMap<>();
	}

	public void shutdownAPIs() {
		apis.entrySet().forEach(entry -> {

			entry.getValue().getAPI().shutdown();

		});

		apis.clear();
	}

	/**
	 * 
	 * @param api
	 */
	public void registerAPI(InternalAPI api) {
		apis.put(api.getClass().getCanonicalName(), new ApiEntry(api, true));
	}

	/**
	 * 
	 * @param identifyer
	 */
	public void removeAPI(String identifyer) {
		apis.remove(identifyer);
	}

	/**
	 * 
	 * @param identifyer
	 */
	public void enableApi(String identifyer) {
		apis.get(identifyer).setEnabled(true);
	}

	/**
	 * 
	 * @param identifyer
	 */
	public void disableApi(String identifyer) {
		apis.get(identifyer).setEnabled(false);
	}

	/**
	 * 
	 */
	public void checkForUpdates() {
		apis.entrySet().forEach(entry -> {

			if (entry.getValue().isEnabled()) {
				entry.getValue().getAPI().checkforUpdates();
			}

		});
	}

	/**
	 * 
	 */
	public void initializeApis() {

		log.info("Await API-ready");
		PropertiesManager propMgr = Klassenserver7bbot.getInstance().getPropertiesManager();

		if (propMgr.getEnabledApis().get("lernsax")) {
			registerAPI(new LernsaxInteractions());
			log.debug("LernsaxAPI initialized");
		}

		if (propMgr.getEnabledApis().get("vplan")) {
			registerAPI(new VplanNEW_XML("10b"));
			log.debug("VplanAPI initialized");
		}

		if (propMgr.getEnabledApis().get("gourmetta")) {
			registerAPI(new GourmettaInteractions());
			log.debug("GourmettaAPI initialized");
		}

		if (propMgr.getEnabledApis().get("kaufland")) {
			registerAPI(new KauflandInteractions());
			log.debug("KauflandAPI initialized");
		}
		
		log.info("APIs are initialized");
	}

}
