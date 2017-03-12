package ai.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.ReturningEventHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.AggressiveNpcAI;

/**
 * Created by Yeats on 06.03.2017.
 */
@AIName("modified_iron_wall_aggressive")
public class ModifiedIronWallAggressiveAI extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getOwner().getNpcId() == 231304) {
			addPercents();
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (getOwner().getNpcId() == 231304)
			checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 98:
					case 77:
					case 56:
					case 35:
					case 10:
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21165, 1, 100, 0, 3000, true)));
						break;
				}
				break;
			}
		}
	}

	@Override
	public int modifyOwnerDamage(int damage, Effect effect) {
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
			case 233740:
			case 235759:
			case 235763:
			case 235767:
			case 235771:
			case 235619:
			case 235620:
			case 235621:
			case 855822:
			case 855843:
			case 230857:
			case 230858:
			case 855776:
			case 277224:
			case 219933:
			case 219934:
			case 235975:
			case 855263:
			case 231196:
			case 231304:
				return (damage > 7900 ? (7900 + Rnd.get(400)) : damage);
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
			switch (getOwner().getNpcId()) {
				case 855822: // vision of rudra
				case 230857: // guard captain ahuradim
				case 233740: // test weapon dynatoum
				case 230858: // brigade general sheba
				case 235621: // virulent ukahim
					stat.setBase(882321);
					break;
				case 855843: // vision of guardian general
				case 855263: // berserk anoha
				case 277224: // ahserion
					stat.setBase(1571296);
					break;
				case 235619: // warmage suyaroka
				case 235620: // chief medic tagnu
				case 855776: // vision of kaliga
				case 231196: // roaring kamara
					stat.setBase(709731);
					break;
				case 219167:
				case 219169:
				case 219190:

					stat.setBaseRate(3.2f);
					break;
				case 219218:
					stat.setBaseRate(7f);
					break;
				case 219934: // prectaz
				case 235975: // gatekeeper flox
				case 219933: // arcticore aizenka
				case 231304: // cursed queen modor
					stat.setBase(1168471);
					break;
				case 219193:
				case 219191:
				case 219192:
				case 235759:
				case 235763:
				case 235767:
				case 235771:
					break;
				default:
					stat.setBaseRate(1.6f);
					break;
			}
		} else {
			super.modifyOwnerStat(stat);
		}
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		if (getOwner().getNpcId() == 231304) {
			switch (usedSkill.getSkillId()) {
				case 21165:
					ThreadPoolManager.getInstance().schedule(() -> {
						WorldPosition pos = getRandomTargetPosition();
						World.getInstance().updatePosition(getOwner(), pos.getX(), pos.getY(), GeoService.getInstance().getZ(getOwner().getWorldId(), pos.getX(), pos.getY(), pos.getZ(), 0.5f, getOwner().getInstanceId()), pos.getHeading());
						PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
						ThreadPoolManager.getInstance().schedule(() -> getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21171, 1, 100, 0, 6000, true))), 500);
					}, 500);
					break;
			}
		}
	}


	@Override
	protected void handleBackHome() {
		if (getOwner().getNpcId() == 231304) {
			addPercents();
		}
		super.handleBackHome();
	}

	@Override
	protected void handleNotAtHome() {
		if (getOwner().getNpcId() == 231304) {
			World.getInstance().updatePosition(getOwner(), getOwner().getSpawn().getX(), getOwner().getSpawn().getY(),
					getOwner().getSpawn().getZ(), getOwner().getSpawn().getHeading());
			PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			System.out.println("not at home!");
			addPercents();
			ReturningEventHandler.onBackHome(this);
		} else {
			super.handleNotAtHome();
		}
	}

	private WorldPosition getRandomTargetPosition() {
		List<Player> knownPlayers = new ArrayList<>();
		WorldPosition pos = null;
		for (Player p : getOwner().getKnownList().getKnownPlayers().values()) {
				if (p.getLifeStats().isAlreadyDead() || p.getLifeStats().isAboutToDie())
					continue;
				if (getOwner().canSee(p) && PositionUtil.isInRange(getOwner(), p, 26)
						&& GeoService.getInstance().canSee(getOwner(), p)) {
					knownPlayers.add(p);
				}

		}
		if (!knownPlayers.isEmpty()) {
			pos = Rnd.get(knownPlayers).getPosition();
		}
		return pos;
	}

	private int getRandomDmg(int damage, float min, float max) {
		return (int) (damage * (min + Rnd.nextFloat() * (max-min)));
	}

	private void addPercents() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 98, 77, 56, 35, 10 });
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		percents.clear();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		percents.clear();
	}
}
