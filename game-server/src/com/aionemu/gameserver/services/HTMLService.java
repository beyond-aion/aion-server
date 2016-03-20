package com.aionemu.gameserver.services;

import java.util.List;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.GuideDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.guide.Guide;
import com.aionemu.gameserver.model.templates.Guides.GuideTemplate;
import com.aionemu.gameserver.model.templates.Guides.SurveyTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTIONNAIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * Use this service to send raw html to the client.
 * 
 * @author lhw, xTz
 */
public class HTMLService {

	private static final Logger log = LoggerFactory.getLogger("ITEM_HTML_LOG");

	public static String getHTMLTemplate(GuideTemplate template) {
		String context = HTMLCache.getInstance().getHTML("guideTemplate.xhtml");

		StringBuilder sb = new StringBuilder();
		sb.append("<reward_items multi_count='").append(template.getRewardCount()).append("'>\n");
		for (SurveyTemplate survey : template.getSurveys()) {
			sb.append("<item_id count='").append(survey.getCount()).append("'>").append(survey.getItemId()).append("</item_id>\n");
		}
		sb.append("</reward_items>\n");
		context = context.replace("%reward%", sb);
		context = context.replace("%radio%", template.getSelect().isEmpty() ? " " : template.getSelect());
		context = context.replace("%html%", template.getMessage().isEmpty() ? " " : template.getMessage());
		context = context.replace("%rewardInfo%", template.getRewardInfo().isEmpty() ? " " : template.getRewardInfo());
		return context;
	}

	public static void pushSurvey(final String html) {
		final int messageId = IDFactory.getInstance().nextId();
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				sendData(player, messageId, html);
			}
		});
	}

	public static void showHTML(Player player, String html) {
		sendData(player, IDFactory.getInstance().nextId(), html);
	}

	public static void sendData(Player player, int messageId, String html) {
		byte packet_count;
		double ceil = Math.ceil(html.length() / (Short.MAX_VALUE - 8d) + 1);
		if (ceil > Byte.MAX_VALUE)
			return;

		packet_count = (byte) ceil;
		for (byte i = 0; i < packet_count; i++) {
			try {
				int from = i * (Short.MAX_VALUE - 8), to = (i + 1) * (Short.MAX_VALUE - 8);
				if (from < 0)
					from = 0;
				if (to > html.length())
					to = html.length();
				String sub = html.substring(from, to);
				player.getClientConnection().sendPacket(new SM_QUESTIONNAIRE(messageId, i, packet_count, sub));
			} catch (Exception e) {
				log.error("htmlservice.sendData", e);
			}
		}
	}

	public static void sendGuideHtml(Player player, int fromLevel, int toLevel) {
		for (int level = fromLevel; level <= toLevel; level++) {
			GuideTemplate[] surveyTemplate = DataManager.GUIDE_HTML_DATA.getTemplatesFor(player.getPlayerClass(), player.getRace(), level);

			for (GuideTemplate template : surveyTemplate) {
				if (!template.isActivated())
					continue;
				int id = IDFactory.getInstance().nextId();
				sendData(player, id, getHTMLTemplate(template));
				DAOManager.getDAO(GuideDAO.class).saveGuide(id, player, template.getTitle());
			}
		}
	}

	public static void onPlayerLogin(Player player) {
		if (player == null)
			return;

		List<Guide> guides = DAOManager.getDAO(GuideDAO.class).loadGuides(player.getObjectId());

		for (Guide guide : guides) {
			GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(guide.getTitle());
			if (template != null) {
				if (template.isActivated())
					sendData(player, guide.getGuideId(), getHTMLTemplate(template));
			} else {
				log.warn("Null guide template for title: {}", guide.getTitle());
			}
		}
	}

	public static void getReward(Player player, int messageId, List<Integer> items) {
		if (player == null || messageId < 1) {
			return;
		}

		if (SurveyService.getInstance().isActive(player, messageId)) {
			return;
		}

		Guide guide = DAOManager.getDAO(GuideDAO.class).loadGuide(player.getObjectId(), messageId);

		if (guide != null) {
			GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(guide.getTitle());
			if (template == null) {
				return;
			}

			if (items.size() > template.getRewardCount()) {
				return;
			}

			if (items.size() > player.getInventory().getFreeSlots()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
				return;
			}
			List<SurveyTemplate> templates = null;
			if (template.getSurveys().size() != template.getRewardCount()) {
				templates = getSurveyTemplates(template.getSurveys(), items);
			} else {
				templates = template.getSurveys();
			}
			if (templates.isEmpty()) {
				return;
			}
			for (SurveyTemplate item : templates) {
				ItemService.addItem(player, item.getItemId(), item.getCount());
				if (LoggingConfig.LOG_ITEM) {
					log.info(String.format("[ITEM] Item Guide ID/Count - %d/%d to player %s.", item.getItemId(), item.getCount(), player.getName()));
				}
			}
			DAOManager.getDAO(GuideDAO.class).deleteGuide(guide.getGuideId());
			IDFactory.getInstance().releaseId(guide.getGuideId());
			items.clear();
		}
	}

	private static List<SurveyTemplate> getSurveyTemplates(List<SurveyTemplate> surveys, List<Integer> items) {
		List<SurveyTemplate> templates = new FastTable<SurveyTemplate>();
		for (SurveyTemplate survey : surveys) {
			if (items.contains(survey.getItemId())) {
				templates.add(survey);
			}
		}
		return templates;
	}
}
