package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("ahserion_sky_assaulter")
public class AhserionSkyAssaulterAI extends GeneralNpcAI {

	public AhserionSkyAssaulterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(this::activate, 400);
	}

	private void activate() {
		WorldPosition p = getPosition();
		Npc assaultPod = (Npc) spawn(297188, p.getX(), p.getY(), p.getZ(), p.getHeading()); // Assault Pod
		assaultPod.getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> assaultPod.getController().deleteIfAliveOrCancelRespawn(), 6000));

		ThreadPoolManager.getInstance().schedule(this::useSkill, 1000);

		ThreadPoolManager.getInstance().schedule(this::spawnDefenders, 4000);
	}

	private void useSkill() {
		SkillEngine.getInstance().getSkill(getOwner(), 20776, 1, getOwner()).useWithoutPropSkill(); // Trooper Shock
	}

	private void spawnDefenders() {
		WorldPosition p = getPosition();
		switch (getNpcId()) {
			case 297352 -> {
				spawn(297191, p.getX() + 3, p.getY() - 3, p.getZ(), p.getHeading()); // Ahserion Troopers Assassin
				spawn(297192, p.getX(), p.getY(), p.getZ() + 0.1f, p.getHeading()); // Ahserion Troopers Sorcerer
				spawn(297191, p.getX() + 3, p.getY() + 3, p.getZ(), p.getHeading()); // Ahserion Troopers Assassin
			}
			case 297353 -> {
				spawn(297190, p.getX() - 2, p.getY() + 2, p.getZ(), p.getHeading()); // Ahserion Troopers Defender Captain
				spawn(297191, p.getX() + 2, p.getY() - 2, p.getZ(), p.getHeading()); // Ahserion Troopers Assassin
				spawn(297191, p.getX() - 2, p.getY() - 2, p.getZ() + 2, p.getHeading()); // Ahserion Troopers Assassin
			}
		}

		AIActions.deleteOwner(this);
	}
}
