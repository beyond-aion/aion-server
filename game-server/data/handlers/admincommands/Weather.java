package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.world.WeatherTable;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Kwazar
 */
public class Weather extends AdminCommand {

	public Weather() {
		super("weather", "Shows/changes the weather.", """
			info - Shows the weather at your position and when it changes.
			list - Lists the weather codes of this map.
			next - Ends the running weather and rolls a new one on this map.
			set <code> - Sets the weather code on every channel of this map, 0 clears it.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		WeatherService weatherService = WeatherService.getInstance();
		if (params[0].equalsIgnoreCase("info")) {
			int zoneId = weatherService.getWeatherZoneIdAt(admin);
			String zone = zoneId > 0 ? " in weather zone " + zoneId : " outside of weather zones";
			sendInfo(admin, "Weather" + zone + ": " + describe(weatherService.getWeatherAt(admin)) + ".");
			int minutes = weatherService.getMinutesUntilNextChange(admin);
			if (minutes >= 0)
				sendInfo(admin, "Next change in " + minutes + " game minutes.");
			return;
		}
		if (params[0].equalsIgnoreCase("list")) {
			WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(admin.getWorldId());
			if (table == null) {
				sendInfo(admin, "This map has no weather defined.");
				return;
			}
			StringBuilder sb = new StringBuilder("Weathers of this map:");
			for (WeatherEntry entry : table.getEntries())
				sb.append('\n').append(entry.getCode()).append(": ").append(entry.getName()).append(" (").append(describeZone(entry)).append(')');
			sendInfo(admin, sb.toString());
			return;
		}
		if (params[0].equalsIgnoreCase("next")) {
			if (!weatherService.nextWeather(admin)) {
				sendInfo(admin, "This map has no weather defined.");
				return;
			}
			sendInfo(admin, "Changed the weather to " + describe(weatherService.getWeatherAt(admin)) + ".");
			return;
		}
		if (params[0].equalsIgnoreCase("set") && params.length >= 2) {
			int weatherCode = Integer.parseInt(params[1]);
			WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(admin.getWorldId());
			WeatherEntry entry = weatherCode == 0 || table == null ? null : table.getEntry(weatherCode);
			if (weatherCode != 0 && entry == null) {
				sendInfo(admin, "This map has no weather with code " + weatherCode + ".");
				return;
			}
			if (!weatherService.changeWeather(admin, weatherCode)) {
				sendInfo(admin, "This map has no weather defined.");
				return;
			}
			if (entry == null)
				sendInfo(admin, "Changed the weather to clear on every channel of this map.");
			else
				sendInfo(admin, "Changed the weather to " + describe(entry) + " in " + describeZone(entry) + " on every channel of this map.");
			sendInfo(admin, "At your position: " + describe(weatherService.getWeatherAt(admin)) + ".");
			return;
		}
		sendInfo(admin);
	}

	/**
	 * @return The weather name with its code, or "clear" for the absence of weather.
	 */
	private static String describe(WeatherEntry entry) {
		return entry == null ? "clear" : entry.getName() + " (code " + entry.getCode() + ")";
	}

	private static String describeZone(WeatherEntry entry) {
		return entry.getZoneId() == 0 ? "the whole map" : "weather zone " + entry.getZoneId();
	}
}
