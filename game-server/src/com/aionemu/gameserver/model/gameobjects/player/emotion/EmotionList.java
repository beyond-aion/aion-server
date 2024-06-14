package com.aionemu.gameserver.model.gameobjects.player.emotion;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dao.PlayerEmotionListDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.EmotionLearnAction;
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
			PacketSendUtility.sendPacket(owner, new SM_EMOTION_LIST((byte) 1, Collections.singletonList(emotion)));
		}
	}

	public void remove(int emotionId) {
		emotions.remove(emotionId);
		PlayerEmotionListDAO.deleteEmotion(owner.getObjectId(), emotionId);
		PacketSendUtility.sendPacket(owner, new SM_EMOTION_LIST((byte) 0, getEmotions()));
	}

	public boolean contains(int emotionId) {
		return emotions != null && emotions.containsKey(emotionId);
	}

	public boolean canUse(int emotionId) {
		return !EmotionLearnAction.isLearnable(emotionId) || contains(emotionId) || owner.hasPermission(MembershipConfig.EMOTIONS_ALL);
	}

	public Collection<Emotion> getEmotions() {
		if (emotions == null)
			return Collections.emptyList();
		return emotions.values();
	}
}
