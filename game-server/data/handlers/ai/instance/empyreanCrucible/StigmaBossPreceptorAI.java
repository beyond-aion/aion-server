package ai.instance.empyreanCrucible;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author w4terbomb
 */
@AIName("stigma_boss_preceptor")
public class StigmaBossPreceptorAI extends CrucibleBossAI {

	private static final int HEALING_TOTEM = 282375, ETERNAL_SERVANT = 282376;
	private int heavyIn;

	public StigmaBossPreceptorAI(Npc owner) {
		super(owner);
	}

	private boolean isLight() {
		return getNpcId() == 217586;
	}

	@Override
	protected void onFightStart() {
		say(isLight() ? 1500223 : 1500226);
		cast(19615, 15);
		heavyIn = 45;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 20, 20))
			cast(hp > 50 ? 19615 : 19618, 15);
		if (--heavyIn <= 0) {
			if (hp > 50) {
				cast(19617, 10);
				cast(19616, 10, NpcSkillTargetAttribute.RANDOM);
				heavyIn = 45;
			} else {
				cast(19621, 10, NpcSkillTargetAttribute.RANDOM);
				WorldPosition p = getPosition();
				spawnTemporary(HEALING_TOTEM, p.getX(), p.getY(), p.getZ(), 30000);
				heavyIn = 40;
			}
		}
		if (every(seconds, 10, 30) && hp <= 50) {
			WorldPosition p = getPosition();
			if (hp > 30) {
				cast(19634, 10, NpcSkillTargetAttribute.ME);
				spawnServant(p.getX() + Rnd.get(-5, 5), p.getY() + Rnd.get(-5, 5), p.getZ());
				spawnServant(1798.31f, 783.21f, p.getZ());
				spawnServant(1798.01f, 810.14f, p.getZ());
			} else {
				spawnServant(p.getX() + Rnd.get(-5, 5), p.getY() + Rnd.get(-5, 5), p.getZ());
				spawnServant(p.getX() + Rnd.get(-10, 10), p.getY() + Rnd.get(-10, 10), p.getZ());
				spawnServant(1798.01f, 810.14f, p.getZ());
				spawnServant(1784.74f, 776.63f, p.getZ());
				spawnServant(1784.74f, 815.63f, p.getZ());
			}
		}
		if (passed(80)) {
			cast(19619, 10);
			cast(19618, 15);
			cast(19615, 15);
		}
		if (passed(40)) {
			cast(19612, 10, NpcSkillTargetAttribute.ME);
			cast(19620, 15);
			cast(19618, 15);
			cast(19615, 15);
			cast(19635, 10);
		}
	}

	private void spawnServant(float x, float y, float z) {
		spawnTemporary(ETERNAL_SERVANT, x, y, z, 45000);
	}

	private void spawnTemporary(int npcId, float x, float y, float z, int lifeTime) {
		if (spawn(npcId, x, y, z, (byte) 0) instanceof Npc npc)
			ThreadPoolManager.getInstance().schedule(() -> npc.getController().deleteIfAliveOrCancelRespawn(), lifeTime);
	}

	@Override
	protected void onFightEnd() {
		getPosition().getWorldMapInstance().getNpcs(HEALING_TOTEM, ETERNAL_SERVANT).forEach(npc -> npc.getController().delete());
		if (!isDead())
			say(isLight() ? 1500224 : 1500227);
	}

	@Override
	protected void handleDied() {
		say(isLight() ? 1500225 : 1500228);
		super.handleDied();
	}
}
