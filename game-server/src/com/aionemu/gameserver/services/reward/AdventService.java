package com.aionemu.gameserver.services.reward;

import java.awt.Color;
import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.survey.CustomSurveyItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Nathan
 * @modified Estrayl, Neon
 */
public class AdventService {

	private static final AdventService instance = new AdventService();
	private Map<Integer, List<CustomSurveyItem>> rewards = new FastMap<>();

	private AdventService() {
		for (int i = 1; i <= 25; i++) {
			rewards.put(i, new FastTable<>());
		}
		initMaps();
	}

	private void initMaps() {
		rewards.get(1).add(new CustomSurveyItem(125040122, 1));

		rewards.get(2).add(new CustomSurveyItem(169600206, 1));

		rewards.get(3).add(new CustomSurveyItem(169620082, 1));

		rewards.get(4).add(new CustomSurveyItem(110900666, 1));

		rewards.get(5).add(new CustomSurveyItem(170030057, 1));



		rewards.get(7).add(new CustomSurveyItem(186000236, 50));

		rewards.get(8).add(new CustomSurveyItem(162001033, 25));

		rewards.get(9).add(new CustomSurveyItem(160010095, 10));
		rewards.get(9).add(new CustomSurveyItem(160010201, 10));

		rewards.get(10).add(new CustomSurveyItem(188050006, 5)); // ely
		rewards.get(10).add(new CustomSurveyItem(188050009, 5)); // asmo

		rewards.get(11).add(new CustomSurveyItem(164002116, 10));
		rewards.get(11).add(new CustomSurveyItem(164002272, 10));

		rewards.get(12).add(new CustomSurveyItem(170030058, 1));

		rewards.get(13).add(new CustomSurveyItem(169620072, 1));

		rewards.get(14).add(new CustomSurveyItem(188051293, 1)); // ely
		rewards.get(14).add(new CustomSurveyItem(188051294, 1)); // asmo

		rewards.get(15).add(new CustomSurveyItem(164002117, 10));
		rewards.get(15).add(new CustomSurveyItem(164002118, 10));

		rewards.get(16).add(new CustomSurveyItem(186000143, 250));

		rewards.get(17).add(new CustomSurveyItem(162001013, 5));

		rewards.get(18).add(new CustomSurveyItem(169620094, 1));

		rewards.get(19).add(new CustomSurveyItem(170150003, 1));

		rewards.get(20).add(new CustomSurveyItem(162001031, 25));
		rewards.get(20).add(new CustomSurveyItem(162001032, 25));
		rewards.get(20).add(new CustomSurveyItem(162001034, 25));

		rewards.get(21).add(new CustomSurveyItem(188050006, 5)); // ely
		rewards.get(21).add(new CustomSurveyItem(188050009, 5)); // asmo

		rewards.get(22).add(new CustomSurveyItem(188052717, 1));

		rewards.get(23).add(new CustomSurveyItem(186000051, 5));

		rewards.get(24).add(new CustomSurveyItem(190020074, 1));
		rewards.get(24).add(new CustomSurveyItem(186000242, 5));
	}

	public void onLogin(Player player) {
		int day = MonthDay.now().getDayOfMonth();
		if (YearMonth.now().getMonth() != Month.DECEMBER || day > 24)
			return;
		if (!rewards.containsKey(day) || rewards.get(day).isEmpty() && day != 6)
			return;
		if (DAOManager.getDAO(AdventDAO.class).getLastReceivedDay(player) < day)
			PacketSendUtility.sendYellowMessage(player, "You can open your advent calendar door for today!"
				+ "\nType in .advent to redeem todays reward on this character.\n" + ChatUtil.color("ATTENTION:", Color.ORANGE)
				+ " Only one character per account can receive this reward!");
	}

	public void redeemReward(Player player) {
		int day = MonthDay.now().getDayOfMonth();

		if (DAOManager.getDAO(AdventDAO.class).getLastReceivedDay(player) >= day) {
			PacketSendUtility.sendMessage(player, "You have already opened todays advent calendar door on this account.");
			return;
		}

		if (day != 6 && player.getMailbox().size() + rewards.get(day).size() > 100 || !player.getMailbox().haveFreeSlots()) {
			PacketSendUtility.sendMessage(player, "Your mailbox is full, please make some room.");
			return;
		}

		if (!DAOManager.getDAO(AdventDAO.class).storeLastReceivedDay(player, day)) {
			PacketSendUtility.sendMessage(player, "Sorry. Some shugo broke our database, please report this in our bugtracker :(");
			return;
		}

		if (day == 6) {
			sendRewardMail(player, getRndMount(), 1, day);
		} else if (day <= 24) {
			for (CustomSurveyItem item : rewards.get(day)) {
				ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(item.getId());
				if (template != null && template.getRace() == player.getCommonData().getOppositeRace())
					return;
				sendRewardMail(player, item.getId(), item.getCount(), day);
			}
		}
	}

	private void sendRewardMail(Player player, int id, int count, int day) {
		SystemMailService.getInstance().sendMail("Beyond Aion", player.getName(), "Advent Calendar", "Greetings Daeva!\n\nToday is December " + day
				+ " and you know what that means! Another day, another advent calendar door.\n\nWe hope you can use this well~\n\n-Beyond Aion", id, count, 0, LetterType.EXPRESS);
	}

	public void showTodaysReward(Player player) {
		int day = MonthDay.now().getDayOfMonth();
		StringBuilder sb = new StringBuilder("Todays advent calendar reward(s):\n");
		if (day == 6)
			sb.append("One random mount (30 days)");
		else if (day <= 24) {
			Iterator<CustomSurveyItem> iter = rewards.get(day).iterator();
			while (iter.hasNext()) {
				int id = iter.next().getId();
				ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
				if (template != null && template.getRace() == player.getCommonData().getOppositeRace())
					continue;
				sb.append(ChatUtil.item(id) + (iter.hasNext() ? ", " : ""));
			}
		}
		PacketSendUtility.sendMessage(player, sb.toString());
	}

	public int getRndMount() {
		switch (Rnd.get(1, 19)) {
			case 1:
				return 190100022;
			case 2:
				return 190100031;
			case 3:
				return 190100045;
			case 4:
				return 190100055;
			case 5:
				return 190100060;
			case 6:
				return 190100069;
			case 7:
				return 190100077;
			case 8:
				return 190100083;
			case 9:
				return 190100089;
			case 10:
				return 190100095;
			case 11:
				return 190100114;
			case 12:
				return 190100126;
			case 13:
				return 190100130;
			case 14:
				return 190100133;
			case 15:
				return 190100132;
			case 16:
				return 190100142;
			case 17:
				return 190100149;
			case 18:
				return 190100153;
			case 19:
				return 190100170;
		}
		return 190100170;
	}

	public static AdventService getInstance() {
		return instance;
	}
}
