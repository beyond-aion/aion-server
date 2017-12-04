package admincommands;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;
import com.aionemu.commons.configuration.TransformationException;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.administration.DeveloperConfig;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.CacheConfig;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.DropConfig;
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
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 * @modified Rolandas
 * @reworked Neon
 */
public class Configure extends AdminCommand {

	private static final Map<String, Class<?>> configs = new LinkedHashMap<>();

	static {
		List<Class<?>> classes = Arrays.asList(AIConfig.class, AdminConfig.class, AutoGroupConfig.class, CacheConfig.class, CraftConfig.class,
			CustomConfig.class, DeveloperConfig.class, DropConfig.class, EventsConfig.class, FallDamageConfig.class, GSConfig.class,
			GeoDataConfig.class, GroupConfig.class, HTMLConfig.class, HousingConfig.class, InGameShopConfig.class, LegionConfig.class, LoggingConfig.class,
			MembershipConfig.class, NameConfig.class, NetworkConfig.class, PeriodicSaveConfig.class, PricesConfig.class, PunishmentConfig.class,
			RankingConfig.class, RatesConfig.class, SecurityConfig.class, ShutdownConfig.class, SiegeConfig.class, ThreadConfig.class, WorldConfig.class);

		for (Class<?> cls : classes)
			configs.put(cls.getSimpleName().toLowerCase().replace("config", ""), cls);
	}

	public Configure() {
		super("configure", "Shows/changes config settings.");

		// @formatter:off
		setSyntaxInfo(
			"<list> - Shows all available configuration categories.",
			"<category> - Shows all available properties of the specified configuration.",
			"<category> <property> - Shows the properties active value.",
			"<category> <property> <value> - Changes the properties value to the new value."
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
						String value = getFieldValue(field);
						sb.append("\n\t").append(field.getName()).append("\t=\t").append(value);
					} catch (IllegalArgumentException | IllegalAccessException e) { // skip this property
					}
				}
				sendInfo(admin, sb.toString());
				return;
			}
			String fieldName = params[1].toUpperCase();
			try {
				Field property = cls.getDeclaredField(fieldName);
				String value = getFieldValue(property);
				if (params.length > 2) {
					String newValue = StringUtils.join(params, ' ', 2, params.length);
					Class<?> classType = property.getType();
					PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer(classType);
					try {
						property.set(null, pt.transform(newValue, property));
					} catch (TransformationException e) {
						sendInfo(admin, "The new value could not be set: " + e.getCause().getMessage());
						return;
					}
					sendInfo(admin,
						"The value of " + cls.getSimpleName() + "." + fieldName + " has been changed from " + value + " to " + getFieldValue(property));
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

	private String getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
		Object value = field.get(null);
		if (value != null && value.getClass().isArray()) {
			if (value.getClass().getComponentType().isPrimitive()) {
				int length = Array.getLength(value);
				Object[] objArr = new Object[length];
				for (int i = 0; i < length; i++)
					objArr[i] = Array.get(value, i);
				value = Arrays.toString(objArr);
			} else
				value = Arrays.toString((Object[]) value);
		}
		return String.valueOf(value);
	}
}
