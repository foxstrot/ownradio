package ownradio.util;

import ownradio.annotation.DisplayName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Класс утилита для упрощения работы с рефлексией
 *
 * @author Alpenov Tanat
 */
public class ReflectUtil {
	private ReflectUtil() {
	}

	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	public static Map<String, String> getDisplayNameFields(Object o) {
		ResourceBundle res = ResourceUtil.getResourceBundle();

		Map<String, String> result = new HashMap<>();

		getAllFields(new LinkedList<>(), o.getClass()).stream()
				.filter(field -> field.isAnnotationPresent(DisplayName.class))
				.forEach(field -> {
					for (Annotation annotation : field.getAnnotations()) {
						if (annotation instanceof DisplayName) {
							DisplayName displayName = (DisplayName) annotation;

							if (displayName.isVisible()) {
								String key = displayName.key();
								result.put(key, res.getString(key));
							}
						}
					}

				});

		return result;
	}
}
