package ai.instance.eternalBastion;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("pashidassaultpod")
public class AssaultsPodAI extends NpcAI {

	private Future<?> task;

	public AssaultsPodAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		schedule();
	}

	@Override
	public void handleDespawned() {
		super.handleDespawned();
		if (!task.isDone())
			task.cancel(false);
	}

	@Override
	public void handleDied() {
		super.handleDied();
		if (!task.isDone())
			task.cancel(false);
	}

	private void schedule() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (!isDead()) {
					spawnSummons();
				}
			}
		}, 5000, 120000);
	}

	private void spawnSummons() {
		switch (this.getNpcId()) {
			case 231162:
				rndSpawn(231106, 1, "");
				rndSpawn(231108, 2, "");
				break;
			case 231167:
				rndSpawn(231105, 1, "");
				rndSpawn(231107, 2, "");
				break;
			case 231163:
			case 231165:
				rndSpawn(231105, 1, "assaultspodsmobs231165");
				rndSpawn(231108, 2, "assaultspodsmobs231165");
				break;
			case 231141:
			case 231164:
				rndSpawn(231108, 1, "assaultspodsmobs231164");
				rndSpawn(231107, 2, "assaultspodsmobs231164");
				break;
			case 231158:
				rndSpawn(231105, 1, "assaultspodsmobs231158");
				rndSpawn(231112, 2, "assaultspodsmobs231158");
				break;
			case 231156:
				rndSpawn(231134, 1, "assaultspodsmobs231156");
				rndSpawn(231112, 2, "assaultspodsmobs231156");
				break;
			case 231140:
				rndSpawn(231107, 1, "assaultspodsmobs231140");
				rndSpawn(231112, 2, "assaultspodsmobs231140");
				break;
			case 231159:
				rndSpawn(231107, 1, "assaultspodsmobs231159");
				rndSpawn(231112, 2, "assaultspodsmobs231159");
				break;
			case 231143:
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231140");
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231140");
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231140");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231140");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231140");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231140");
				break;
			case 231152:
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231152");
				spawnAndMove(231108, getOwner(), "assaultspodsmobs231152");
				spawnAndMove(231134, getOwner(), "assaultspodsmobs231152");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231152");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231152");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231152");
				break;
			case 231153:
				spawnAndMove(231105, getOwner(), "assaultspodsmobs231156");
				spawnAndMove(231105, getOwner(), "assaultspodsmobs231156");
				spawnAndMove(231108, getOwner(), "assaultspodsmobs231156");
				spawnAndMove(231134, getOwner(), "assaultspodsmobs231156");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231156");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231156");
				break;
			case 231154:
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231154");
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231154");
				spawnAndMove(231108, getOwner(), "assaultspodsmobs231154");
				spawnAndMove(231115, getOwner(), "assaultspodsmobs231154");
				spawnAndMove(231134, getOwner(), "assaultspodsmobs231154");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231154");
				break;
			case 231155:
				spawnAndMove(231107, getOwner(), "assaultspodsmobs231155");
				spawnAndMove(231134, getOwner(), "assaultspodsmobs231155");
				spawnAndMove(231108, getOwner(), "assaultspodsmobs231155");
				spawnAndMove(231115, getOwner(), "assaultspodsmobs231155");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231155");
				spawnAndMove(231112, getOwner(), "assaultspodsmobs231155");
				break;
			case 231160:
			case 231157:
				rndSpawn(230744, 1, "");
				rndSpawn(230745, 1, "");
				rndSpawn(230749, 1, "");
				if (!task.isDone())
					task.cancel(false);
				break;
		}
	}

	private void spawnAndMove(int npcId, Npc owner, final String walker) {
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(owner.getHeading()));
		int dist = Rnd.get(2, 10);
		float x = (float) (Math.cos(radian) * dist);
		float y = (float) (Math.sin(radian) * dist);
		final Npc npc = (Npc) spawn(npcId, owner.getX() + x, owner.getY() + y, owner.getZ() + 10, (byte) 0);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				npc.getSpawn().setWalkerId(walker);
				WalkManager.startWalking((NpcAI) npc.getAi());
				npc.setState(CreatureState.ACTIVE, true);
				PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
			}
		}, 3000);
	}

	private void rndSpawn(int npcId, int count, String walker) {
		for (int i = 0; i < count; i++) {
			Npc npc = (Npc) rndSpawnInRange(npcId, 5);
			if (!walker.isEmpty()) {
				ThreadPoolManager.getInstance().schedule(() -> {
					npc.getSpawn().setWalkerId(walker);
					WalkManager.startWalking((NpcAI) npc.getAi());
					npc.setState(CreatureState.ACTIVE, true);
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
				}, 3000);
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
