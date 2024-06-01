package ai.instance.custom.pvpveMap;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.handler.ReturningEventHandler;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.AggressiveNpcAI;

/**
 * @author Yeats
 */
@AIName("modified_iron_wall_aggressive")
public class ModifiedIronWallAggressiveAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases;

	public ModifiedIronWallAggressiveAI(Npc owner) {
		super(owner);
		hpPhases = owner.getNpcId() == 231304 ? new HpPhases(98, 77, 56, 35, 10) : null;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (hpPhases != null)
			hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		getOwner().queueSkill(21165, 1, 3000);
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		if (PvpMapService.getInstance().isRandomBoss(getOwner())) {
			return damage > 7900 ? (7700 + Rnd.nextInt(600)) : damage;
		}
		switch (getOwner().getNpcId()) {
			case 218547: // cleric
			case 219197:
			case 218551:
			case 219190:
			case 219169:
			case 218546: // mage
			case 218550:
			case 219196:
			case 219189:
			case 219168:
				if (effect != null) {
					return getRandomDmg(damage, 1.2f, 1.6f);
				} else {
					return getRandomDmg(damage, 2.4f, 3.2f);
				}
			case 219218: // keymaster chookuri (cleric, normal)
			case 219193: // keymaster dabra (mage, hero)
			case 219191: // keymaster niksi (fighter, elite)
			case 219192: // keymaster zumita (assa, elite)
				return damage;
			default:
				if (effect != null) {
					return getRandomDmg(damage, 2f, 2.8f);
				} else {
					return getRandomDmg(damage, 1.8f, 2.3f);
				}
		}
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP) {
			if (PvpMapService.getInstance().isRandomBoss(getOwner())) {
				stat.setBase(882321);
			} else {
				switch (getOwner().getNpcId()) {
					case 219167:
					case 219169:
					case 219190:
						stat.setBaseRate(3.2f);
						break;
					case 219218:
						stat.setBaseRate(7f);
						break;
					case 219193:
					case 219191:
					case 219192:
						break;
					default:
						stat.setBaseRate(1.6f);
				}
			}
		} else {
			super.modifyOwnerStat(stat);
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (getOwner().getNpcId() == 231304) {
			switch (skillTemplate.getSkillId()) {
				case 21165:
					ThreadPoolManager.getInstance().schedule(() -> {
						WorldPosition pos = getRandomTargetPosition();
						if (pos == null)
							return;
						World.getInstance().updatePosition(getOwner(), pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
						PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
						ThreadPoolManager.getInstance()
							.schedule(() -> getOwner().queueSkill(21171, 1, 6000), 500);
					}, 500);
					break;
			}
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		if (hpPhases != null)
			hpPhases.reset();
	}

	@Override
	protected void handleNotAtHome() {
		if (getOwner().getNpcId() == 231304) {
			World.getInstance().updatePosition(getOwner(), getOwner().getSpawn().getX(), getOwner().getSpawn().getY(), getOwner().getSpawn().getZ(),
				getOwner().getSpawn().getHeading());
			PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			if (hpPhases != null)
				hpPhases.reset();
			ReturningEventHandler.onBackHome(this);
		} else {
			super.handleNotAtHome();
		}
	}

	private WorldPosition getRandomTargetPosition() {
		List<Player> knownPlayers = new ArrayList<>();
		WorldPosition pos = null;
		for (Player p : getOwner().getKnownList().getKnownPlayers().values()) {
			if (p.isDead() || p.getLifeStats().isAboutToDie())
				continue;
			if (getOwner().canSee(p) && PositionUtil.isInRange(getOwner(), p, 26) && GeoService.getInstance().canSee(getOwner(), p)) {
				knownPlayers.add(p);
			}

		}
		if (!knownPlayers.isEmpty()) {
			pos = Rnd.get(knownPlayers).getPosition();
		}
		return pos;
	}

	private float getRandomDmg(float damage, float minMultiplier, float maxMultiplier) {
		return damage * Rnd.nextFloat(minMultiplier, maxMultiplier);
	}
}
