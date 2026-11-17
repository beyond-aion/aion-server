package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;

import org.glassfish.jaxb.runtime.IDResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.utils.xml.JAXBUtil;
import com.fasterxml.aalto.stax.InputFactoryImpl;

/**
 * Unmarshals the merged static data cache. The cache is a sequence of independent top-level elements, so instead of one large unmarshal the
 * element ranges recorded by {@link XmlMerger} are unmarshalled in parallel and the resulting objects are assigned to the fields of
 * {@link StaticData}. Falls back to a single unmarshal if the cache was not split or if anything goes wrong.
 */
public class StaticDataLoader {

	private static final Logger log = LoggerFactory.getLogger(StaticDataLoader.class);

	private static final XMLInputFactory XML_INPUT_FACTORY = new InputFactoryImpl(); // Aalto, thread-safe and faster than the JDK parser
	private static final Map<String, Field> FIELDS = fieldsByElementName();

	private StaticDataLoader() {
	}

	public static StaticData load(XmlMerger.MergeResult mergeResult) throws Exception {
		if (Runtime.getRuntime().availableProcessors() < 2)
			return JAXBUtil.deserialize(mergeResult.newReader(), StaticData.class);

		byte[] data = mergeResult.getBuffer();
		if (data == null)
			data = Files.readAllBytes(mergeResult.getFile().toPath());

		try {
			return parse(data, mergeResult);
		} catch (Exception e) {
			log.error("Parallel static data parsing failed, falling back to a single unmarshal", e);
			return JAXBUtil.deserialize(mergeResult.newReader(), StaticData.class);
		}
	}

	private static StaticData parse(byte[] data, XmlMerger.MergeResult mergeResult) throws Exception {
		long start = System.currentTimeMillis();
		Element itemTemplates = null;
		int topLevelElements = mergeResult.getSplit().getRanges().length;
		List<Element> others = new ArrayList<>(topLevelElements);
		for (int[] range : mergeResult.getSplit().getRanges()) {
			String name = rootElementName(data, range[0], range[1] - range[0]);
			Field field = FIELDS.get(name);
			if (field == null)
				throw new IllegalStateException("No field for top-level element " + name);
			Element element = new Element(name, field, range[0], range[1]);
			if ("item_templates".equals(name))
				itemTemplates = element;
			else
				others.add(element);
		}
		if (itemTemplates == null)
			throw new IllegalArgumentException("item_templates element not found in " + mergeResult.getFile().getName());

		StaticData staticData = new StaticData();
		JAXBContext context = JAXBUtil.getContext(StaticData.class);
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			// item_templates is parsed first because some element callbacks resolve item ids through StaticData.itemData. The others start in parallel with
			// it. @XmlIDREF references to item templates (npc equipment, initial player items) wait for item_templates at the end of their part.
			List<Future<?>> tasks = new ArrayList<>(topLevelElements);
			Future<?> itemTemplatesTask = submit(executor, context, staticData, data, itemTemplates, null);
			tasks.add(itemTemplatesTask);
			for (Element element : others)
				tasks.add(submit(executor, context, staticData, data, element, itemTemplatesTask));
			for (Future<?> task : tasks)
				task.get();
		}

		staticData.afterUnmarshal(null, null);
		log.info("Parsed {} static data parts in parallel in {} ms", topLevelElements, System.currentTimeMillis() - start);
		return staticData;
	}

	private static Future<?> submit(ExecutorService executor, JAXBContext context, StaticData staticData, byte[] data, Element element,
		Future<?> itemTemplatesTask) {
		return executor.submit(() -> {
			try {
				Unmarshaller unmarshaller = JAXBUtil.newUnmarshaller(context); // fails on errors like the single unmarshal does, the cause stays linked
				unmarshaller.setListener(new StaticDataListener(staticData));
				if (itemTemplatesTask != null)
					unmarshaller.setProperty(IDResolver.class.getName(), new ItemTemplateIdResolver(staticData, itemTemplatesTask));
				Field field = element.field();
				field.setAccessible(true);
				InputStream in = new ByteArrayInputStream(data, element.start(), element.end() - element.start());
				XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(in);
				try {
					Object value = unmarshaller.unmarshal(reader, field.getType());
					if (value instanceof JAXBElement<?> jaxbElement)
						value = jaxbElement.getValue();
					field.set(staticData, value);
				} finally {
					reader.close();
					in.close();
				}
			} catch (Exception e) {
				log.debug("Failed to parse static data element {} ({}..{})", element.name(), element.start(), element.end(), e);
				throw e;
			}
			return null;
		});
	}

	private static String rootElementName(byte[] data, int offset, int length) throws Exception {
		try (InputStream in = new ByteArrayInputStream(data, offset, length)) {
			XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(in);
			try {
				while (reader.hasNext()) {
					if (reader.next() == XMLStreamConstants.START_ELEMENT)
						return reader.getLocalName();
				}
			} finally {
				reader.close();
			}
		}
		throw new IllegalStateException("No root element found");
	}

	private static Map<String, Field> fieldsByElementName() {
		Map<String, Field> fields = new HashMap<>();
		for (Field field : StaticData.class.getDeclaredFields()) {
			XmlElement element = field.getAnnotation(XmlElement.class);
			if (element != null)
				fields.put(element.name(), field);
		}
		return fields;
	}

	public static void waitForItemTemplates(Unmarshaller unmarshaller) {
		try {
			if (unmarshaller.getProperty(IDResolver.class.getName()) instanceof ItemTemplateIdResolver itemTemplateIdResolver)
				itemTemplateIdResolver.itemTemplatesTask.get();
		} catch (PropertyException | InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Resolves @XmlIDREF references to item templates, which are declared in another part of the cache and therefore unknown to the
	 * unmarshaller of this part. Other references are resolved within the part, like the default resolver does.
	 */
	private static class ItemTemplateIdResolver extends IDResolver {

		private final StaticData staticData;
		private final Future<?> itemTemplatesTask;
		private final Map<String, Object> boundIds = new HashMap<>();

		private ItemTemplateIdResolver(StaticData staticData, Future<?> itemTemplatesTask) {
			this.staticData = staticData;
			this.itemTemplatesTask = itemTemplatesTask;
		}

		@Override
		public void bind(String id, Object obj) {
			boundIds.put(id, obj);
		}

		@Override
		public Callable<?> resolve(String id, @SuppressWarnings("rawtypes") Class targetType) {
			if (!ItemTemplate.class.isAssignableFrom(targetType))
				return () -> boundIds.get(id);
			return () -> {
				itemTemplatesTask.get(); // called at the end of this part, item_templates was submitted first and is running or done
				return staticData.itemData.getItemTemplate(Integer.parseInt(id));
			};
		}
	}

	private record Element(String name, Field field, int start, int end) {}
}
