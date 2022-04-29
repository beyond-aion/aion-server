package ai.instance.padmarashkasCave;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@AIName("padmarashkaegg")
public class PadmarashkaEggAI extends NpcAI {

	boolean isSmallEggProtectorSpawned = false;
	boolean isHugeEggProtectorSpawned = false;
	private Npc protector = null;

	public PadmarashkaEggAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		if (protector != null && !protector.isDead()) {
			SkillEngine.getInstance().getSkill(protector, 20176, 55, protector).useNoAnimationSkill(); // apply wrath buff
		}
		super.handleDied();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (!isSmallEggProtectorSpawned && this.getNpcId() == 282613) {
			switch (Rnd.get(1, 6)) {
				case 1:
					protector = (Npc) spawn(282715, 579.415f, 168.109f, 66.000f, (byte) 0);
					break;
				case 2:
					protector = (Npc) spawn(282715, 581.316f, 157.520f, 66.000f, (byte) 0);
					break;
				case 3:
					protector = (Npc) spawn(282715, 575.073f, 147.338f, 66.000f, (byte) 0);
					break;
				case 4:
					protector = (Npc) spawn(282715, 585.119f, 150.989f, 66.000f, (byte) 0);
					break;
				case 5:
					protector = (Npc) spawn(282716, 581.141f, 148.342f, 66.000f, (byte) 0);
					break;
				case 6:
					protector = (Npc) spawn(282716, 584.240f, 142.233f, 66.000f, (byte) 0);
					break;
			}
			isSmallEggProtectorSpawned = true;
		} else if (!isHugeEggProtectorSpawned && this.getNpcId() == 282614) {
			SpawnEliteCommander(); // Random spawn SpawnEliteCommander to protect Egg
			isHugeEggProtectorSpawned = true;
		}
	}

	private void SpawnEliteCommander() {
		protector = (Npc) rndSpawnInRange(282712, 5);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (this.getNpcId()) {
			case 282613:
				smallEggSpawn();
				break;
			case 282614:
				hugeEggSpawn();
				break;
		}
	}

	private void smallEggSpawn() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && getOwner().isSpawned()) {
				AIActions.deleteOwner(PadmarashkaEggAI.this);
				attackPlayer((Npc) spawn(282616, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0));
			}

		}, 60000); // TODO: Need right value
	}

	private void hugeEggSpawn() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && getOwner().isSpawned()) {
				AIActions.deleteOwner(PadmarashkaEggAI.this);
				attackPlayer((Npc) spawn(282620, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0));
			}
		}, 120000); // TODO: Need right value
	}

	private void attackPlayer(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Npc padma = getOwner().getPosition().getWorldMapInstance().getNpc(218756);
				if (padma != null) {
					npc.setTarget(padma.getTarget());
					npc.getAi().setStateIfNot(AIState.WALKING);
					npc.setState(CreatureState.ACTIVE, true);
					npc.getMoveController().moveToTargetObject();
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
				}
			}
		}, 1000);
	}
}
