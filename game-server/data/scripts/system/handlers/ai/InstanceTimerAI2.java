package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.group.PlayerFilters.SameInstanceFilter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("instancetimer")
public class InstanceTimerAI2 extends AggressiveNpcAI2 {

	private boolean isInTimer = false;
	private long curentTime;

	protected void setInstanceTimer(Creature creature) {

		int time = 0;
		switch (getNpcId()) {
			case 215222:
			case 215221:
			case 215179:
			case 215178:
			case 215136:
			case 215135:
				time = 600000;
				break;
		}

		Player player;
		if (creature instanceof Player) {
			player = (Player) creature;
		}
		else if (creature instanceof Summon){
			player = (Player) creature.getMaster();
		}
		else {
			return;
		}
		isInTimer = true;
		curentTime = System.currentTimeMillis();
		sendTime(player, time);
	}

	private void sendTime(Player player, int time) {		
		if (player.isInTeam()) {
			player.getCurrentTeam().sendPacket(new SM_QUEST_ACTION(0, time / 1000), new SameInstanceFilter(player));
		}
		else {
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, time / 1000));
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if(!isInTimer)
			setInstanceTimer(creature);
	}

	@Override
	public long getRemainigTime() {
		long time = System.currentTimeMillis() - curentTime;
		if (time < 0) {
			return 0;
		}
		return time > 600000 ? 0 : time;
	}
}