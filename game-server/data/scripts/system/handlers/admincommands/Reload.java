package admincommands;

import static org.apache.commons.io.filefilter.FileFilterUtils.and;
import static org.apache.commons.io.filefilter.FileFilterUtils.makeSVNAware;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.prefixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import javolution.util.FastTable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.aionemu.gameserver.ai2.AI2Engine;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.dataholders.CustomDrop;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.EventData;
import com.aionemu.gameserver.dataholders.NpcDropData;
import com.aionemu.gameserver.dataholders.QuestsData;
import com.aionemu.gameserver.dataholders.SkillData;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.dataholders.XMLQuests;
import com.aionemu.gameserver.dataholders.loadingutils.XmlValidationHandler;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author MrPoke
 */
public class Reload extends AdminCommand {

	private static final Logger log = LoggerFactory.getLogger(Reload.class);
	private static final String SYNTAX = "syntax //reload <quest | skill | npc | items | portal | commands | drop | gameshop | events | config | ai>";

	public Reload() {
		super("reload");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length != 1) {
			PacketSendUtility.sendMessage(admin, SYNTAX);
			return;
		}
		if (params[0].equals("quest")) {
			File xml = new File("./data/static_data/quest_data/quest_data.xml");
			File dir = new File("./data/static_data/quest_script_data");
			try {
				QuestEngine.getInstance().shutdown();
				JAXBContext jc = JAXBContext.newInstance(StaticData.class);
				Unmarshaller un = jc.createUnmarshaller();
				un.setSchema(getSchema("./data/static_data/static_data.xsd"));
				QuestsData newQuestData = (QuestsData) un.unmarshal(xml);
				QuestsData questsData = DataManager.QUEST_DATA;
				questsData.setQuestsData(newQuestData.getQuestsData());
				XMLQuests questScriptsData = DataManager.XML_QUESTS;
				questScriptsData.getQuest().clear();
				for (File file : listFiles(dir, true)) {
					XMLQuests data = ((XMLQuests) un.unmarshal(file));
					if (data != null)
						if (data.getQuest() != null)
							questScriptsData.getQuest().addAll(data.getQuest());
				}
				QuestEngine.getInstance().reload(null);
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "Quest reload failed!");
				log.error("quest reload fail", e);
			} finally {
				PacketSendUtility.sendMessage(admin, "Quest reload Success!");
			}
		} else if (params[0].equals("skill")) {
			File dir = new File("./data/static_data/skills");
			try {
				JAXBContext jc = JAXBContext.newInstance(StaticData.class);
				Unmarshaller un = jc.createUnmarshaller();
				un.setSchema(getSchema("./data/static_data/static_data.xsd"));
				List<SkillTemplate> newTemplates = new FastTable<SkillTemplate>();
				for (File file : listFiles(dir, true)) {
					SkillData data = (SkillData) un.unmarshal(file);
					if (data != null)
						newTemplates.addAll(data.getSkillTemplates());
				}
				DataManager.SKILL_DATA.setSkillTemplates(newTemplates);
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "Skill reload failed!");
				log.error("Skill reload failed!", e);
			} finally {
				PacketSendUtility.sendMessage(admin, "Skill reload Success!");
			}
		} else if (params[0].equals("npc")) {
			DataManager.NPC_DATA.reload(admin);
		} else if (params[0].equals("items")) {
			DataManager.ITEM_DATA.reload(admin);
		} else if (params[0].equals("ai")) {
			AI2Engine.getInstance().reload();
			PacketSendUtility.sendMessage(admin, "Ai reload Success!");
		}

		else if (params[0].equals("portal")) {
			// File dir = new File("./data/static_data/portals");
			try {
				JAXBContext jc = JAXBContext.newInstance(StaticData.class);
				Unmarshaller un = jc.createUnmarshaller();
				un.setSchema(getSchema("./data/static_data/static_data.xsd"));
				// List<PortalTemplate> newTemplates = new FastTable<PortalTemplate>();
				// for (File file : listFiles(dir, true)) {
				// PortalData data = (PortalData) un.unmarshal(file);
				// if (data != null && data.getPortals() != null)
				// newTemplates.addAll(data.getPortals());
				// }
				// DataManager.PORTAL_DATA.setPortals(newTemplates);
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "Portal reload failed!");
				log.error("Portal reload failed!", e);
			} finally {
				PacketSendUtility.sendMessage(admin, "Portal reload Success!");
			}
		} else if (params[0].equals("commands")) {
			ChatProcessor.getInstance().reload();
			PacketSendUtility.sendMessage(admin, "Admin commands successfully reloaded!");
		} else if (params[0].equals("config")) {
			Config.reload();
			PacketSendUtility.sendMessage(admin, "Configs successfully reloaded!");
		} else if (params[0].equals("drop")) {
			File xml = new File("./data/static_data/custom_drop/custom_drop.xml");
			CustomDrop data = null;
			try {
				JAXBContext jc = JAXBContext.newInstance(CustomDrop.class);
				Unmarshaller un = jc.createUnmarshaller();
				un.setEventHandler(new XmlValidationHandler());
				un.setSchema(getSchema("./data/static_data/static_data.xsd"));
				data = (CustomDrop) un.unmarshal(xml);
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "CustomDrop reload failed! Keeping the last version ...");
				log.error("CustomDrop reload failed!", e);
				return;
			}
			if (data != null)
				DataManager.CUSTOM_NPC_DROP = data;
			NpcDropData.reload();
			PacketSendUtility.sendMessage(admin, "NpcDrops successfully reloaded!");
		} else if (params[0].equals("gameshop")) {
			InGameShopEn.getInstance().reload();
			PacketSendUtility.sendMessage(admin, "Gameshop successfully reloaded!");
		} else if (params[0].equals("events")) {
			File eventXml = new File("./data/static_data/events_config/events_config.xml");
			EventData data = null;
			try {
				JAXBContext jc = JAXBContext.newInstance(EventData.class);
				Unmarshaller un = jc.createUnmarshaller();
				un.setEventHandler(new XmlValidationHandler());
				un.setSchema(getSchema("./data/static_data/static_data.xsd"));
				data = (EventData) un.unmarshal(eventXml);
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "Event reload failed! Keeping the last version ...");
				log.error("Event reload failed!", e);
				return;
			}
			if (data != null) {
				EventService.getInstance().stop();
				String text = data.getActiveText();
				if (text == null || text.trim().length() == 0)
					text = "NONE";
				DataManager.EVENT_DATA.setAllEvents(data.getAllEvents(), data.getActiveText());
				PacketSendUtility.sendMessage(admin, "Active events: " + text);
				EventService.getInstance().start();
			}
		} else
			PacketSendUtility.sendMessage(admin, SYNTAX);

	}

	private Schema getSchema(String xml_schema) {
		Schema schema = null;
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {
			schema = sf.newSchema(new File(xml_schema));
		} catch (SAXException saxe) {
			throw new Error("Error while getting schema", saxe);
		}

		return schema;
	}

	private Collection<File> listFiles(File root, boolean recursive) {
		IOFileFilter dirFilter = recursive ? makeSVNAware(HiddenFileFilter.VISIBLE) : null;

		return FileUtils.listFiles(root, and(and(notFileFilter(prefixFileFilter("new")), suffixFileFilter(".xml")), HiddenFileFilter.VISIBLE), dirFilter);
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, SYNTAX);
	}
}
