package admincommands;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.administration.DeveloperConfig;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.CacheConfig;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.HTMLConfig;
import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.configs.main.InGameShopConfig;
import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.main.NameConfig;
import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.configs.main.PricesConfig;
import com.aionemu.gameserver.configs.main.PunishmentConfig;
import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.configs.main.RateConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author ATracer
 * @modified Rolandas
 * @reworked Neon
 */
public class Configure extends AdminCommand {

	private static final Map<String, Class<?>> configs = new FastMap<>();

	static {
		List<Class<?>> classes = FastTable.of(AIConfig.class, AdminConfig.class, AutoGroupConfig.class, CacheConfig.class, CraftConfig.class,
			CustomConfig.class, DeveloperConfig.class, DropConfig.class, EnchantsConfig.class, EventsConfig.class, FallDamageConfig.class, GSConfig.class,
			GeoDataConfig.class, GroupConfig.class, HTMLConfig.class, HousingConfig.class, InGameShopConfig.class, LegionConfig.class, LoggingConfig.class,
			MembershipConfig.class, NameConfig.class, NetworkConfig.class, PeriodicSaveConfig.class, PricesConfig.class, PunishmentConfig.class,
			RankingConfig.class, RateConfig.class, SecurityConfig.class, ShutdownConfig.class, SiegeConfig.class, ThreadConfig.class, WorldConfig.class);

		for (Class<?> cls : classes)
			configs.put(cls.getSimpleName().toLowerCase().replace("config", ""), cls);
	}

	public Configure() {
		super("configure", "Shows/changes config settings.");

		// @formatter:off
		setParamInfo(
			"<list> - Shows a list of available configuration categories.",
			"<configname> - Lists all available properties of the specified configuration.",
			"<configname> <property> - Shows the current setting of the specified configuration.",
			"<configname> <property> <value> - Changes the specified configuration setting to the new value."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if ("list".equalsIgnoreCase(params[0])) {
			StringBuilder sb = new StringBuilder("List of available configuration names:");
			for (String configname : configs.keySet())
				sb.append("\n\t").append(configname);
			sendInfo(admin, sb.toString());
		} else {
			Class<?> cls = configs.get(params[0].toLowerCase());
			if (cls == null) {
				sendInfo(admin, "Invalid configuration name. You can get a list of available configuration categories via the <list> parameter.");
				return;
			}
			if (params.length < 2) {
				StringBuilder sb = new StringBuilder("List of available properties for ").append(cls.getSimpleName()).append(":");
				for (Field field : cls.getDeclaredFields()) {
					try {
						String val = String.valueOf(field.get(null)); // can throw an exception if not accessible
						sb.append("\n\t").append(field.getName()).append("\t-\t(").append(val).append(")");
					} catch (IllegalArgumentException | IllegalAccessException e) { // skip this property
					}
				}
				sendInfo(admin, sb.toString());
				return;
			}
			String fieldName = params[1].toUpperCase();
			try {
				Field property = cls.getDeclaredField(fieldName);
				Object value = property.get(null);
				if (params.length > 2) {
					String newValue = StringUtils.join(params, " ", 2, params.length);
					Class<?> classType = property.getType();
					try {
						if (classType == String.class)
							property.set(null, newValue);
						else if (classType == boolean.class || classType == Boolean.class)
							property.set(null, Boolean.parseBoolean(newValue));
						else if (classType == byte.class || classType == Byte.class)
							property.set(null, Byte.parseByte(newValue));
						else if (classType == short.class || classType == Short.class)
							property.set(null, Short.parseShort(newValue));
						else if (classType == int.class || classType == Integer.class)
							property.set(null, Integer.parseInt(newValue));
						else if (classType == long.class || classType == Long.class)
							property.set(null, Long.parseLong(newValue));
						else if (classType == float.class || classType == Float.class)
							property.set(null, Float.parseFloat(newValue));
						else if (classType == double.class || classType == Double.class)
							property.set(null, Double.parseDouble(newValue));
						else {
							sendInfo(admin, "The value cannot be changed via command. Please modify the corresponding *.properties file and reload the config.");
							return;
						}
					} catch (Exception e) {
						sendInfo(admin, "The new value could not be set (data type: " + classType.getSimpleName() + ").");
						return;
					}
					sendInfo(admin, "The value of " + cls.getSimpleName() + "." + fieldName + " has been changed from " + value + " to " + property.get(null));
				} else {
					sendInfo(admin, "The current value of " + cls.getSimpleName() + "." + fieldName + " is " + value);
				}
			} catch (NoSuchFieldException e) {
				sendInfo(admin, "The property " + cls.getSimpleName() + "." + fieldName + " does not exist.");
			} catch (Exception e) {
				sendInfo(admin, "Could not access " + cls.getSimpleName() + "." + fieldName);
			}
		}
	}
}
