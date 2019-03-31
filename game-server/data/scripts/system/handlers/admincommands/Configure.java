package admincommands;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.TransformationException;
import com.aionemu.gameserver.configs.Config;
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
		Config.getClasses().stream()
				.filter(cls -> !getPropertyFields(cls).isEmpty())
				.sorted(Comparator.comparing(Class::getSimpleName, String.CASE_INSENSITIVE_ORDER))
				.forEach(cls -> configs.put(cls.getSimpleName().toLowerCase().replace("config", ""), cls));
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
				for (Field field : getPropertyFields(cls)) {
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
				Field field = cls.getDeclaredField(fieldName);
				if (!field.isAnnotationPresent(Property.class)) {
					sendInfo(admin, cls.getSimpleName() + "." + fieldName + " is not configurable.");
					return;
				}
				String value = getFieldValue(field);
				if (params.length > 2) {
					String newValue = StringUtils.join(params, ' ', 2, params.length);
					try {
						field.set(null, ConfigurableProcessor.transform(newValue, field));
					} catch (TransformationException e) {
						sendInfo(admin, "The new value could not be set: " + e.getCause().getMessage());
						return;
					}
					sendInfo(admin,
						"The value of " + cls.getSimpleName() + "." + fieldName + " has been changed from " + value + " to " + getFieldValue(field));
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

	private static List<Field> getPropertyFields(Class<?> cls) {
		return Arrays.stream(cls.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Property.class)).collect(Collectors.toList());
	}
}
