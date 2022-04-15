package admincommands;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Properties;
import com.aionemu.commons.configuration.TransformationException;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author ATracer, Rolandas, Neon
 */
public class Configure extends AdminCommand {

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

		Map<String, Class<?>> configs = Config.getClasses().stream()
					.sorted(Comparator.comparing(Class::getSimpleName, String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toMap(cls -> cls.getSimpleName().toLowerCase().replace("config", ""), cls -> cls, (u, v) -> u, LinkedHashMap::new));
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
				for (Field field : findStaticFields(cls, Predicates.alwaysTrue())) {
					try {
						String value = getFieldValue(field);
						sb.append("\n\t").append(field.getName()).append("\t=\t").append(value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						sb.append("\n\t").append(field.getName()).append("\t=\t").append("Error reading value: ").append(e.getMessage());
					}
				}
				sendInfo(admin, sb.toString());
				return;
			}
			String fieldName = params[1].toUpperCase();
			try {
				List<Field> fields = findStaticFields(cls, field -> field.getName().equals(fieldName));
				if (fields.isEmpty()) {
					sendInfo(admin, cls.getSimpleName() + "." + fieldName + " does not exist.");
					return;
				}
				Field field = fields.get(0);
				String value = getFieldValue(field);
				if (params.length > 2) {
					String newValue = StringUtils.join(params, ' ', 2, params.length);
					try {
						if (field.isAnnotationPresent(Properties.class))
							field.set(null, ConfigurableProcessor.transform(toMap(newValue), field));
						else
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
			} catch (Exception e) {
				sendInfo(admin, "Could not access " + cls.getSimpleName() + "." + fieldName);
			}
		}
	}

	private Map<String, String> toMap(String value) {
		Map<String, String> values = new LinkedHashMap<>();
		if (value.startsWith("{") && value.endsWith("}"))
			value = value.substring(1, value.length() - 1);
		String[] entries = value.contains(",") ? value.split(" *, *") : value.split(" +");
		try {
			for (String entry : entries) {
				int indexOfEqualsSign = entry.indexOf('=');
				if (indexOfEqualsSign == -1)
					throw new IllegalArgumentException("Missing value after " + entry + " (format: key=value)");
				values.put(entry.substring(0, indexOfEqualsSign).trim(), entry.substring(indexOfEqualsSign + 1).trim());
			}
		} catch (Exception e) {
			throw new TransformationException(null, e);
		}
		return values;
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

	private static List<Field> findStaticFields(Class<?> cls, Predicate<Field> filter) {
		return Arrays.stream(cls.getDeclaredFields()).filter(filter.and(field -> Modifier.isStatic(field.getModifiers()))).toList();
	}
}
