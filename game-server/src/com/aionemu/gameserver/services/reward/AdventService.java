package com.aionemu.gameserver.services.reward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.joda.time.DateTime;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.survey.CustomSurveyItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Nathan
 * @modified Estrayl
 */
public class AdventService {

	private static final AdventService instance = new AdventService();
	private Map<Integer, List<CustomSurveyItem>> rewards = new FastMap<>();

	private AdventService() {
		for (int i = 1; i <= 25; i++) {
			rewards.put(i, new ArrayList<>());
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

		rewards.get(10).add(new CustomSurveyItem(188050006, 5)); // ASMO 188050009

		rewards.get(11).add(new CustomSurveyItem(164002116, 10));
		rewards.get(11).add(new CustomSurveyItem(164002272, 10));

		rewards.get(12).add(new CustomSurveyItem(170030058, 1));

		rewards.get(13).add(new CustomSurveyItem(169620072, 1));

		rewards.get(14).add(new CustomSurveyItem(188051293, 1)); // ASMO 188051294

		rewards.get(15).add(new CustomSurveyItem(164002117, 10));
		rewards.get(15).add(new CustomSurveyItem(164002118, 10));

		rewards.get(16).add(new CustomSurveyItem(186000143, 250));

		rewards.get(17).add(new CustomSurveyItem(162001013, 5));

		rewards.get(18).add(new CustomSurveyItem(169620094, 1));

		rewards.get(19).add(new CustomSurveyItem(170150003, 1));

		rewards.get(20).add(new CustomSurveyItem(162001031, 25));
		rewards.get(20).add(new CustomSurveyItem(162001032, 25));
		rewards.get(20).add(new CustomSurveyItem(162001034, 25));

		rewards.get(21).add(new CustomSurveyItem(188050006, 5)); // ASMO 188050009

		rewards.get(22).add(new CustomSurveyItem(188052717, 1));

		rewards.get(23).add(new CustomSurveyItem(186000051, 5));

		rewards.get(24).add(new CustomSurveyItem(190020074, 1));
		rewards.get(24).add(new CustomSurveyItem(186000242, 5));
	}

	public void onLogin(Player player) {
		if (!DAOManager.getDAO(AdventDAO.class).containAllready(player))
			return;
		DateTime date = new DateTime(System.currentTimeMillis());
		final int day = date.getDayOfMonth();
		int month = date.getMonthOfYear();
		if (month != 12 || day > 24)
			return;
		if (!rewards.containsKey(day) || rewards.get(day).isEmpty())
			return;
		int lastDay = DAOManager.getDAO(AdventDAO.class).get(player);
		if (lastDay == -1 || lastDay >= day)
			return;
		if (isRewardPossible(player))
			sendReward(player, day);
	}

	private void sendReward(Player player, int date) {
		List<CustomSurveyItem> items = new ArrayList<>(rewards.get(date));

		if (items.isEmpty())
			return;
		
		if (date == 6) {
			sendRewardMail(player, getRndMount(), 1);
		} else {
			for (CustomSurveyItem item : items) {
				int id = getAsmodianItem(item.getId());
				sendRewardMail(player, id, item.getCount());
			}
		}
	}

	public boolean isRewardPossible(Player player) {
		if (!DAOManager.getDAO(AdventDAO.class).containAllready(player))
			return false;
		DateTime date = new DateTime(System.currentTimeMillis());
		final int day = date.getDayOfMonth();
		int month = date.getMonthOfYear();
		if (month != 12 || day > 24)
			return false;

		int lastDay = DAOManager.getDAO(AdventDAO.class).get(player);
		if (lastDay == -1 || lastDay >= day)
			return false;

		if (!player.getMailbox().haveFreeSlots()) {
			PacketSendUtility.sendMessage(player, "Your inventory is full, you should make some room and relogg!");
			return false;
		}

		DAOManager.getDAO(AdventDAO.class).set(player, day);
		return true;
	}

	private void sendRewardMail(Player player, int id, int count) {
		SystemMailService.getInstance().sendMail(
			"Beyond Aion",
			player.getName(),
			"Advent Calendar",
			"Greetings Daeva!\n\n" + "You have reached level 65 with your first character and therefore we have a special something for you."
				+ " In gratitude for your support we have prepared a package with valuable items for you.\n\n" + "Enjoy your stay on Beyond Aion!", id,
			count, 0, LetterType.EXPRESS);
	}
	
	public int getAsmodianItem(int id) {
		switch (id) {
			case 188050006:
				return 188050009;
			case 188051293:
				return 188051294;
		}
		return id;
	}

	public int getRndMount() {
		int rnd = Rnd.get(1, 19);
		switch (rnd) {
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
