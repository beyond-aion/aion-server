package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.jaxb.runtime.IDResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * Unmarshals the merged static data cache. The cache is a sequence of independent top-level elements, so instead of one large unmarshal the
 * element ranges recorded by {@link XmlMerger} are unmarshalled in parallel and the resulting objects are assigned to the fields of
 * {@link StaticData}. Falls back to a single unmarshal if the cache was not split or if anything goes wrong.
 *
 * @author Neon
 */
public class StaticDataLoader {

	private static final Logger log = LoggerFactory.getLogger(StaticDataLoader.class);

	private static final int THREADS = Math.min(8, Runtime.getRuntime().availableProcessors());
	private static final ThreadLocal<XMLInputFactory> XML_INPUT_FACTORY = ThreadLocal.withInitial(XMLInputFactory::newInstance);
	private static final Map<String, Field> FIELDS = fieldsByElementName();

	private StaticDataLoader() {
	}

	public static StaticData load(XmlMerger.MergeResult mergeResult) throws IOException {
		XmlMerger.Split split = mergeResult.getSplit();
		if (split == null || THREADS < 2)
			return JAXBUtil.deserialize(mergeResult.newReader(), StaticData.class);

		byte[] data = mergeResult.getBuffer();
		if (data == null) {
			try {
				data = Files.readAllBytes(mergeResult.getFile().toPath());
			} catch (IOException e) {
				log.warn("Could not read {}, falling back to a single unmarshal", mergeResult.getFile(), e);
				return JAXBUtil.deserialize(mergeResult.newReader(), StaticData.class);
			}
		}

		try {
			return parse(data, split);
		} catch (Exception e) {
			log.warn("Parallel static data parsing failed, falling back to a single unmarshal", e);
			return JAXBUtil.deserialize(mergeResult.newReader(), StaticData.class);
		}
	}

	private static StaticData parse(byte[] data, XmlMerger.Split split) throws Exception {
		long start = System.currentTimeMillis();
		StaticData staticData = new StaticData();
		JAXBContext context = JAXBUtil.getContext(StaticData.class);

		Element itemTemplates = null;
		List<Element> others = new ArrayList<>(split.getRanges().length);
		for (int[] range : split.getRanges()) {
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

		ExecutorService executor = Executors.newFixedThreadPool(Math.min(THREADS, others.size() + 1), r -> {
			Thread thread = new Thread(r, "static-data-loader");
			thread.setDaemon(true);
			return thread;
		});
		try {
			// item_templates is parsed first because some element callbacks resolve item ids through StaticData.itemData. The others start in
			// parallel with it and are retried once it is available if their callbacks hit a not yet loaded item.
			// @XmlIDREF references to item templates (npc equipment, initial player items) wait for item_templates at the end of their part.
			Future<?> itemTemplatesTask = itemTemplates == null ? null : submit(executor, context, staticData, data, itemTemplates, null);
			List<ElementTask> tasks = new ArrayList<>(others.size());
			for (Element element : others)
				tasks.add(new ElementTask(element, submit(executor, context, staticData, data, element, itemTemplatesTask)));
			if (itemTemplatesTask != null)
				itemTemplatesTask.get();

			List<Element> retry = new ArrayList<>();
			for (ElementTask task : tasks) {
				try {
					task.future().get();
				} catch (ExecutionException e) {
					retry.add(task.element());
				}
			}
			if (!retry.isEmpty()) {
				log.debug("Retrying {} static data parts after item templates: {}", retry.size(), retry.stream().map(Element::name).toList());
				for (Element element : retry)
					submit(executor, context, staticData, data, element, itemTemplatesTask).get();
			}
		} finally {
			executor.shutdownNow();
		}

		staticData.afterUnmarshal(null, null);
		staticData.waitForAfterUnmarshalTasksToFinish();
		log.info("Parsed {} static data parts in parallel in {} ms", others.size() + (itemTemplates == null ? 0 : 1), System.currentTimeMillis() - start);
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
				XMLStreamReader reader = XML_INPUT_FACTORY.get().createXMLStreamReader(in);
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
			XMLStreamReader reader = XML_INPUT_FACTORY.get().createXMLStreamReader(in);
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

	private record Element(String name, Field field, int start, int end) {
	}

	private record ElementTask(Element element, Future<?> future) {
	}
}
