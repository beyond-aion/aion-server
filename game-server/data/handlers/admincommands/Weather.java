package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Kwazar
 */
public class Weather extends AdminCommand {

	public Weather() {
		super("weather", "Shows/changes the weather.");

		// @formatter:off
		setSyntaxInfo(
			"<info> - Shows info for the weather in the current zone.",
			"<next> - Triggers a natural weather change on this map.",
			"<set> <code> - Changes the weather on this map, according to the weather code between 0 (default) and 12."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		switch (params[0].toLowerCase()) {
			case "info":
				for (ZoneInstance regionZone : admin.findZones()) {
					if (regionZone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER) {
						int weatherZoneId = DataManager.ZONE_DATA.getWeatherZoneId(regionZone.getZoneTemplate());
						WeatherEntry weatherEntry = WeatherService.getInstance().getWeatherEntry(admin.getWorldId(), weatherZoneId);
						if (weatherEntry != null) {
							String info = "Weather for region " + regionZone.getZoneTemplate().getXmlName() + ":";
							if (weatherEntry == WeatherEntry.NONE) {
								info += "\n\tcode: " + weatherEntry.getCode() + " (no weather)";
							} else {
								if (weatherEntry.getZoneId() > 0)
									info += "\n\tzone: " + weatherEntry.getZoneId();
								if (weatherEntry.getWeatherName() != null)
									info += "\n\tname: " + weatherEntry.getWeatherName();
								info += "\n\tcode: " + weatherEntry.getCode();
							}
							sendInfo(admin, info);
							return;
						}
					}
				}
				sendInfo(admin, "No weather found for this region.");
				return;
			case "set":
			case "next":
				int weatherCode;
				if (params[0].equalsIgnoreCase("next")) {
					if (params.length != 1) {
						sendInfo(admin);
						return;
					}
					weatherCode = -1;
				} else {
					weatherCode = NumberUtils.toInt(params[1], -1);
					if (weatherCode < 0 || weatherCode > 12) {
						sendInfo(admin, "Weather code must be between 0 and 12.");
						return;
					}
				}

				if (WeatherService.getInstance().changeWeather(admin.getWorldId(), weatherCode)) {
					String weatherName = WeatherService.getInstance().findWeatherEntry(admin).getWeatherName();
					sendInfo(admin, "Changed the weather" + (weatherName == null ? "." : " to " + weatherName + "."));
				} else {
					sendInfo(admin, "This region has no weather defined.");
				}
				return;
		}
	}
}
