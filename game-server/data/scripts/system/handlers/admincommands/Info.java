package admincommands;

import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.ENpcFactionQuestState;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.spawnengine.ClusteredNpc;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author Nemiroff Date: 28.12.2009
 */
public class Info extends AdminCommand {

	public Info() {
		super("info", "Shows information about your target.");
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();

		if (target == null) {
			sendInfo(admin, "Please select a target.");
		} else if (target instanceof Player) {
			Player player = (Player) target;
			Pet pet = player.getPet();
			sendInfo(admin,
				"[Info about " + player.getName() + "]\nPlayer Id: " + player.getObjectId() + "\n" + player.getPosition().toCoordString()
					+ (pet != null ? "\nPet Id: " + pet.getPetId() + " / ObjectId: " + pet.getObjectId() : "") + "\n Town ID: "
					+ TownService.getInstance().getTownResidence(player) + "\n Tribe: " + player.getTribe() + "\n TribeBase: " + player.getBaseTribe());

			sendInfo(admin,
				"[Stats]\nPvP attack: " + player.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent() * 0.1f + "%\nPvP p. attack: "
					+ player.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_PHYSICAL, 0).getCurrent() * 0.1f + "%\nPvP m. attack: "
					+ player.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_MAGICAL, 0).getCurrent() * 0.1f + "%\nPvP defend: "
					+ player.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent() * 0.1f + "%\nPvP p. defend: "
					+ player.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_PHYSICAL, 0).getCurrent() * 0.1f + "%\nPvP m. defend: "
					+ player.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_MAGICAL, 0).getCurrent() * 0.1f + "%\nCast Time Boost: +"
					+ (player.getGameStats().getStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() * 0.1f - 100) + "%\nAttack Speed: "
					+ player.getGameStats().getAttackSpeed().getCurrent() * 0.001f + "\nMovement Speed: " + player.getGameStats().getMovementSpeedFloat()
					+ "\n----------Main Hand------------\nAttack: " + player.getGameStats().getMainHandPAttack().getCurrent() + "\nAccuracy: "
					+ player.getGameStats().getMainHandPAccuracy().getCurrent() + "\nCritical: " + player.getGameStats().getMainHandPCritical().getCurrent()
					+ "\n------------Off Hand------------\nFinal-Attack: "
					+ (player.getGameStats().getOffHandPAttack().getBase() + Math.round(player.getGameStats().getOffHandPAttack().getBonus() * 0.98f))
					+ "\n[Current]Attack: " + player.getGameStats().getOffHandPAttack().getCurrent() + "\nAccuracy: "
					+ player.getGameStats().getOffHandPAccuracy().getCurrent() + "\nCritical: " + player.getGameStats().getOffHandPCritical().getCurrent()
					+ "\n-------------Magical-------------\nFinal-Attack: "
					+ (player.getGameStats().getOffHandMAttack().getBase() + Math.round(player.getGameStats().getOffHandMAttack().getBonus() * 0.82f))
					+ "\n[Current]Attack: " + player.getGameStats().getMainHandMAttack().getCurrent() + "\nAccuracy: "
					+ player.getGameStats().getMAccuracy().getCurrent() + "\nCritical: " + player.getGameStats().getMCritical().getCurrent() + "\nBoost: "
					+ player.getGameStats().getMBoost().getCurrent() + "\n-------------Protect--------------\nPhysical Defence: "
					+ player.getGameStats().getPDef().getCurrent() + "\nBlock: " + player.getGameStats().getBlock().getCurrent() + "\nParry: "
					+ player.getGameStats().getParry().getCurrent() + "\nEvasion: " + player.getGameStats().getEvasion().getCurrent() + "\nMagic Resist: "
					+ player.getGameStats().getMResist().getCurrent());

			for (int i = 0; i < 2; i++) {
				NpcFaction faction = player.getNpcFactions().getActiveNpcFaction(i == 0);
				if (faction != null) {
					sendInfo(admin,
						player.getName() + " have join to " + (i == 0 ? "mentor" : "daily") + " faction: "
							+ DataManager.NPC_FACTIONS_DATA.getNpcFactionById(faction.getId()).getName() + "\nCurrent quest state: " + faction.getState().name()
							+ (faction.getState().equals(ENpcFactionQuestState.COMPLETE)
								? ("\nNext after: " + ((faction.getTime() - System.currentTimeMillis() / 1000) / 3600f) + " h.") : ""));
				}
			}
			sendInfo(admin, "[Life stats]\nisAlive: " + !player.getLifeStats().isAlreadyDead() + "\nHP: " + player.getLifeStats().getCurrentHp() + " / "
				+ player.getLifeStats().getMaxHp() + "\nMP: " + player.getLifeStats().getCurrentMp() + " / " + player.getLifeStats().getMaxMp());
			sendInfo(admin, "[Current Zone Info]\nPvP: " + player.isInsidePvPZone());
		} else if (target instanceof Summon) {
			Summon summon = (Summon) admin.getTarget();
			sendInfo(admin, "[Current Zone Info]\nPvP: " + summon.isInsidePvPZone());

			int asmoDmg = 0;
			int elyDmg = 0;
			int npcDmg = 0;
			sendInfo(admin, "[AggroList]");
			for (AggroInfo ai : summon.getAggroList().getList()) {
				if (!(ai.getAttacker() instanceof Creature))
					continue;
				Creature master = ((Creature) ai.getAttacker()).getMaster();
				if (master == null)
					continue;
				if (master instanceof Player) {
					Player player = (Player) master;
					sendInfo(admin, "Name: " + player.getName() + " Dmg: " + ai.getDamage());
					if (player.getRace() == Race.ASMODIANS)
						asmoDmg += ai.getDamage();
					else
						elyDmg += ai.getDamage();
				} else
					npcDmg += ai.getDamage();
			}
			sendInfo(admin, "[TotalDmg]\n(A) Dmg: " + asmoDmg + "\n(E) Dmg: " + elyDmg + "\n(N) Dmg: " + npcDmg);
		} else if (target instanceof Npc) {
			Npc npc = (Npc) admin.getTarget();
			sendInfo(admin,
				"[Info about target]\nName: " + npc.getName() + "\nId: " + npc.getNpcId() + " / ObjectId: " + admin.getTarget().getObjectId()
					+ " / StaticId: " + npc.getSpawn().getStaticId() + "\n" + admin.getTarget().getPosition().toCoordString() + ", Angle: "
					+ PositionUtil.getAngleToTarget(admin, admin.getTarget()) + " \n Town ID:"
					+ TownService.getInstance().getTownIdByPosition((Creature) target));
			if (npc instanceof SiegeNpc) {
				SiegeNpc siegeNpc = (SiegeNpc) npc;
				sendInfo(admin, "[Siege info]\nSiegeId: " + siegeNpc.getSiegeId() + "\nSiegeRace: " + siegeNpc.getSiegeRace());
			}
			sendInfo(admin,
				"Tribe: " + npc.getTribe() + "\n TribeBase: " + npc.getBaseTribe() + "\nRace: " + npc.getObjectTemplate().getRace() + "\nNpcType: "
					+ npc.getType(admin) + "\nTemplateType: " + npc.getObjectTemplate().getNpcTemplateType().name() + "\nAbyssType: "
					+ npc.getObjectTemplate().getAbyssNpcType().name() + "\nAI: " + npc.getAi2().getName() + "\n NpcRating: "
					+ npc.getObjectTemplate().getRating().name());
			sendInfo(admin, "[Relations to target]\nisEnemy: " + admin.isEnemy(npc) + "\ncanAttack: " + RestrictionsManager.canAttack(admin, target)
				+ "\n[Relations to you]\nisEnemy: " + npc.isEnemy(admin) + "\nisAggressive: " + TribeRelationService.isAggressive(npc, admin));
			sendInfo(admin,
				"[Life stats]\nHP: " + npc.getLifeStats().getCurrentHp() + " / " + npc.getLifeStats().getMaxHp() + "\nMP: "
					+ npc.getLifeStats().getCurrentMp() + " / " + npc.getLifeStats().getMaxMp() + "\nXP: "
					+ StatFunctions.calculateExperienceReward(admin.getLevel(), npc));
			sendInfo(admin,
				"[Sense range]\nRadius: " + npc.getAggroRange() + "\nSide: " + npc.getObjectTemplate().getBoundRadius().getSide() + " / Front: "
					+ npc.getObjectTemplate().getBoundRadius().getFront() + "\nDirectional bound: " + PositionUtil.getDirectionalBound(npc, admin, true)
					+ "\nDistance: " + (npc.getAggroRange() + PositionUtil.getDirectionalBound(npc, admin, true)) + "\nCollision: "
					+ (npc.getAggroRange() - npc.getCollision()));

			sendInfo(admin, "[Current Zone Info]\nPvP: " + npc.isInsidePvPZone());

			int asmoDmg = 0;
			int elyDmg = 0;
			int npcDmg = 0;
			sendInfo(admin, "[AggroList]");
			for (AggroInfo ai : npc.getAggroList().getList()) {
				if (!(ai.getAttacker() instanceof Creature))
					continue;
				Creature master = ((Creature) ai.getAttacker()).getMaster();
				if (master == null)
					continue;
				if (master instanceof Player) {
					Player player = (Player) master;
					sendInfo(admin, "Name: " + player.getName() + " Dmg: " + ai.getDamage());
					if (player.getRace() == Race.ASMODIANS)
						asmoDmg += ai.getDamage();
					else
						elyDmg += ai.getDamage();
				} else
					npcDmg += ai.getDamage();
			}
			sendInfo(admin, "[TotalDmg]\n(A) Dmg: " + asmoDmg + "\n(E) Dmg: " + elyDmg + "\n(N) Dmg: " + npcDmg);
			if (npc.isPathWalker()) {
				WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(npc.getSpawn().getWalkerId());
				if (template != null) {
					sendInfo(admin, "[Route]\nRouteId: " + npc.getSpawn().getWalkerId() + " (Reversed: " + template.isReversed() + ")");
					if (npc.getWalkerGroup() != null) {
						ClusteredNpc snpc = npc.getWalkerGroup().getClusterData(npc);
						sendInfo(admin, "[Group]\nType: " + npc.getWalkerGroup().getWalkType() + " / XDelta: " + snpc.getXDelta() + " / YDelta: "
							+ snpc.getYDelta() + " / Index: " + snpc.getWalkerIndex());
					}
				}
			} else if (npc.isRandomWalker()) {
				sendInfo(admin, "[Route]\nRandomWalkRange: " + npc.getSpawn().getRandomWalkRange() + "m");
			}
		} else {
			sendInfo(admin, "[Info about " + target.getClass().getSimpleName() + "]\nName: " + target.getName() + "\nId: "
				+ target.getObjectTemplate().getTemplateId() + " / ObjectId: " + target.getObjectId() + "\n" + target.getPosition().toCoordString());
		}
	}
}
