package ai.instance.tallocsHollow;

import static com.aionemu.gameserver.model.DialogAction.MAKE_MERCENARY;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.controllers.SummonController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("tallocssummon")
public class TallocsSummonAI extends NpcAI {

	private final AtomicBoolean isTransformed = new AtomicBoolean(false);

	public TallocsSummonAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == MAKE_MERCENARY && isTransformed.compareAndSet(false, true)) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			if (player.getSummon() != null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_ALREADY_HAVE_A_FOLLOWER());
				return true;
			}
			SpawnTemplate st = getSpawnTemplate();
			SpawnTemplate spawnTemplate = SpawnEngine.newSingleTimeSpawn(st.getWorldId(), getNpcId(), st.getX(), st.getY(), st.getZ(), st.getHeading(), 0,
				SpawnTemplate.NO_AI);
			Summon summon = new Summon(getObjectId(), new SummonController(), spawnTemplate, getObjectTemplate(), player, 0, false);
			player.setSummon(summon);
			summon.setTarget(player.getTarget());
			summon.setKnownlist(getKnownList());
			summon.setEffectController(new EffectController(summon));
			summon.setPosition(getPosition());
			summon.setLifeStats(getLifeStats());
			PacketSendUtility.sendPacket(player, new SM_TRANSFORM_IN_SUMMON(player, getObjectId()));
			PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(getObjectId(), 0, CreatureType.FRIEND.getId(), 0));
			summon.setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, EmotionType.CHANGE_SPEED, 0, summon.getObjectId()));
		}
		return true;
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (!isTransformed.get())
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
	}
}
