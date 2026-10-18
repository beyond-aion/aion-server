package com.aionemu.gameserver.model.gameobjects.player.emotion;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.dao.PlayerEmotionListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION_LIST;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class EmotionList {

	private Map<Integer, Emotion> emotions;
	private Player owner;

	public EmotionList(Player owner) {
		this.owner = owner;
	}

	public void add(int emotionId, int dispearTime, boolean isNew) {
		if (emotions == null)
			emotions = new LinkedHashMap<>();

		Emotion emotion = new Emotion(emotionId, dispearTime);
		emotions.put(emotionId, emotion);

		if (isNew) {
			ExpireTimerTask.getInstance().registerExpirable(emotion, owner);
			PlayerEmotionListDAO.insertEmotion(owner, emotion);
			PacketSendUtility.sendPacket(owner, new SM_EMOTION_LIST(SM_EMOTION_LIST.Action.ADD, Collections.singletonList(emotion)));
		}
	}

	public void remove(int emotionId) {
		Emotion emotion = emotions.remove(emotionId);
		PlayerEmotionListDAO.deleteEmotion(owner.getObjectId(), emotionId);
		if (emotion != null)
			PacketSendUtility.sendPacket(owner, new SM_EMOTION_LIST(SM_EMOTION_LIST.Action.REMOVE, Collections.singletonList(emotion)));
	}

	public boolean contains(int emotionId) {
		return emotions != null && emotions.containsKey(emotionId);
	}

	public boolean canUse(int emotionId) {
		return !DataManager.ITEM_DATA.isLearnableEmotion(emotionId) || contains(emotionId);
	}

	public Collection<Emotion> getEmotions() {
		if (emotions == null)
			return Collections.emptyList();
		return emotions.values();
	}
}
