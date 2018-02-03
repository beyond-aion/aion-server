package admincommands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aionemu.commons.utils.xml.JAXBUtil;
import com.aionemu.commons.utils.xml.XmlUtil;
import com.aionemu.gameserver.ai.AIEngine;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.dataholders.CustomDrop;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.EventData;
import com.aionemu.gameserver.dataholders.ItemData;
import com.aionemu.gameserver.dataholders.NpcData;
import com.aionemu.gameserver.dataholders.NpcSkillData;
import com.aionemu.gameserver.dataholders.QuestsData;
import com.aionemu.gameserver.dataholders.SkillData;
import com.aionemu.gameserver.dataholders.XMLQuests;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.models.XMLQuest;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author MrPoke
 * @reworked Neon
 */
public class Reload extends AdminCommand {

	public Reload() {
		super("reload", "Reloads templates or handlers (static data).");

		// @formatter:off
		setSyntaxInfo(
			"<config> - Reloads all configuration settings.",
			"<commands|ai> - Reloads the specified handlers.",
			"<quests> - Reloads quest templates and handlers.",
			"<skills|npcskills> - Reloads the specified skill templates.",
			"<events> - Reloads event templates and (re)starts events.",
			"<npcs|items|customdrops|gameshop> - Reloads the specified data."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		if (params[0].equalsIgnoreCase("quests")) {
			File xml = new File("./data/static_data/quest_data/quest_data.xml");
			DataManager.QUEST_DATA = JAXBUtil.deserialize(xml, QuestsData.class, "./data/static_data/static_data.xsd");
			List<XMLQuest> templates = new ArrayList<>();
			Collection<File> files = XmlUtil.listFiles("./data/static_data/quest_script_data", true);
			JAXBUtil.deserialize(files, XMLQuests.class, "./data/static_data/static_data.xsd").forEach(e -> templates.addAll(e.getAllQuests()));
			DataManager.XML_QUESTS.setData(templates);
			QuestEngine.getInstance().reload();
			sendInfo(admin, DataManager.QUEST_DATA.size() + " quest templates loaded (" + QuestEngine.getInstance().getQuestHandlerCount() + " handlers).");
		} else if (params[0].equalsIgnoreCase("skills")) {
			File xml = new File("./data/static_data/skills/skill_templates.xml");
			DataManager.SKILL_DATA = JAXBUtil.deserialize(xml, SkillData.class, "./data/static_data/static_data.xsd");
			sendInfo(admin, DataManager.SKILL_DATA.size() + " skills loaded.");
		} else if (params[0].equalsIgnoreCase("npcskills")) {
			List<NpcSkillTemplates> templates = new ArrayList<>();
			Collection<File> files = XmlUtil.listFiles("./data/static_data/npc_skills", true);
			JAXBUtil.deserialize(files, NpcSkillData.class, "./data/static_data/static_data.xsd")
				.forEach(e -> templates.addAll(e.getAllNpcSkillTemplates()));
			DataManager.NPC_SKILL_DATA.setNpcSkillTemplates(templates);
			sendInfo(admin, DataManager.NPC_SKILL_DATA.size() + " npc skills loaded.");
		} else if (params[0].equalsIgnoreCase("npcs")) {
			File xml = new File("./data/static_data/npcs/npc_templates.xml");
			DataManager.NPC_DATA = JAXBUtil.deserialize(xml, NpcData.class, "./data/static_data/static_data.xsd");
			sendInfo(admin, DataManager.NPC_DATA.size() + " npc templates loaded.");
		} else if (params[0].equalsIgnoreCase("items")) {
			File xml = new File("./data/static_data/items/item_templates.xml");
			DataManager.ITEM_DATA = JAXBUtil.deserialize(xml, ItemData.class, "./data/static_data/static_data.xsd");
			DataManager.ITEM_DATA.cleanup();
			sendInfo(admin, DataManager.ITEM_DATA.size() + " item templates loaded.");
		} else if (params[0].equalsIgnoreCase("ai")) {
			AIEngine.getInstance().reload();
			sendInfo(admin, "AI successfully reloaded!");
		} else if (params[0].equalsIgnoreCase("commands")) {
			ChatProcessor.getInstance().reload();
			sendInfo(admin, "Chat commands successfully reloaded!");
		} else if (params[0].equalsIgnoreCase("config")) {
			Config.reload();
			sendInfo(admin, "Configs successfully reloaded!");
		} else if (params[0].equalsIgnoreCase("customdrops")) {
			File xml = new File("./data/static_data/custom_drop/custom_drop.xml");
			DataManager.CUSTOM_NPC_DROP = JAXBUtil.deserialize(xml, CustomDrop.class, "./data/static_data/static_data.xsd");
			sendInfo(admin, DataManager.CUSTOM_NPC_DROP.size() + " custom drops loaded.");
		} else if (params[0].equalsIgnoreCase("gameshop")) {
			InGameShopEn.getInstance().reload();
			sendInfo(admin, "Gameshop successfully reloaded!");
		} else if (params[0].equalsIgnoreCase("events")) {
			List<EventTemplate> templates = new ArrayList<>();
			Collection<File> files = XmlUtil.listFiles("./data/static_data/events/timed_events", true);
			JAXBUtil.deserialize(files, EventData.class, "./data/static_data/static_data.xsd").forEach(e -> templates.addAll(e.getEvents()));
			EventService.getInstance().stop();
			DataManager.EVENT_DATA.setEvents(templates);
			EventService.getInstance().start();
			sendInfo(admin, DataManager.EVENT_DATA.size() + " events loaded.");
		} else
			sendInfo(admin);
	}
}
