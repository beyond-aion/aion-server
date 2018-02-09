package admincommands;

import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.ENpcFactionQuestState;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.spawnengine.ClusteredNpc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author Nemiroff Date: 28.12.2009
 * @modified Neon
 */
public class Info extends AdminCommand {

	public Info() {
		super("info", "Shows information about your target.");
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();

		if (target == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		sendInfo(admin, "[Info about " + target.getClass().getSimpleName() + "]\n\tName: " + target.getName() + ", ObjectId: " + target.getObjectId()
			+ "\n\tTemplateId: " + target.getObjectTemplate().getTemplateId());

		if (target instanceof Creature) {
			Creature creature = (Creature) target;
			if (creature instanceof Player) {
				Player player = (Player) creature;
				Pet pet = player.getPet();
				sendInfo(admin, (pet != null ? "Pet Id: " + pet.getObjectTemplate().getTemplateId() + ", ObjectId: " + pet.getObjectId() + "\n\t" : "")
					+ "Town ID: " + TownService.getInstance().getTownResidence(player));
				PlayerGameStats pgs = player.getGameStats();
				sendInfo(admin,
					"[Stats]\n\tPvP attack: " + pgs.getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent() * 0.1f + "%\n\tPvP p. attack: "
						+ pgs.getStat(StatEnum.PVP_ATTACK_RATIO_PHYSICAL, 0).getCurrent() * 0.1f + "%\n\tPvP m. attack: "
						+ pgs.getStat(StatEnum.PVP_ATTACK_RATIO_MAGICAL, 0).getCurrent() * 0.1f + "%\n\tPvP defend: "
						+ pgs.getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent() * 0.1f + "%\n\tPvP p. defend: "
						+ pgs.getStat(StatEnum.PVP_DEFEND_RATIO_PHYSICAL, 0).getCurrent() * 0.1f + "%\n\tPvP m. defend: "
						+ pgs.getStat(StatEnum.PVP_DEFEND_RATIO_MAGICAL, 0).getCurrent() * 0.1f + "%\n\tCast Time Boost: +"
						+ (pgs.getStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() * 0.1f - 100) + "%\n\tAttack Speed: "
						+ pgs.getAttackSpeed().getCurrent() * 0.001f + "\n\tMovement Speed: " + pgs.getMovementSpeedFloat()
						+ "\n\t----------Main Hand------------\n\tAttack: " + pgs.getMainHandPAttack().getCurrent() + "\n\tAccuracy: "
						+ pgs.getMainHandPAccuracy().getCurrent() + "\n\tCritical: " + pgs.getMainHandPCritical().getCurrent()
						+ "\n\t------------Off Hand------------\n\tFinal-Attack: "
						+ (pgs.getOffHandPAttack().getBase() + Math.round(pgs.getOffHandPAttack().getBonus() * 0.98f)) + "\n\t[Current]Attack: "
						+ pgs.getOffHandPAttack().getCurrent() + "\n\tAccuracy: " + pgs.getOffHandPAccuracy().getCurrent() + "\n\tCritical: "
						+ pgs.getOffHandPCritical().getCurrent() + "\n\t-------------Magical-------------\n\tFinal-Attack: "
						+ (pgs.getOffHandMAttack().getBase() + Math.round(pgs.getOffHandMAttack().getBonus() * 0.82f)) + "\n\t[Current]Attack: "
						+ pgs.getMainHandMAttack().getCurrent() + "\n\tAccuracy: " + pgs.getMAccuracy().getCurrent() + "\n\tCritical: "
						+ pgs.getMCritical().getCurrent() + "\n\tBoost: " + pgs.getMBoost().getCurrent()
						+ "\n\t-------------Protect--------------\n\tPhysical Defence: " + pgs.getPDef().getCurrent() + "\n\tBlock: "
						+ pgs.getBlock().getCurrent() + "\n\tParry: " + pgs.getParry().getCurrent() + "\n\tEvasion: " + pgs.getEvasion().getCurrent()
						+ "\n\tMagic Resist: " + pgs.getMResist().getCurrent());

				for (int i = 0; i < 2; i++) {
					NpcFaction faction = player.getNpcFactions().getActiveNpcFaction(i == 0);
					if (faction != null) {
						sendInfo(admin,
							player.getName() + " have join to " + (i == 0 ? "mentor" : "daily") + " faction: "
								+ DataManager.NPC_FACTIONS_DATA.getNpcFactionById(faction.getId()).getName() + "\n\tCurrent quest state: " + faction.getState().name()
								+ (faction.getState().equals(ENpcFactionQuestState.COMPLETE)
									? ("\n\tNext after: " + ((faction.getTime() - System.currentTimeMillis() / 1000) / 3600f) + " h.") : ""));
					}
				}
			} else if (creature instanceof Npc) {
				Npc npc = (Npc) creature;
				sendInfo(admin, "[Template info]\n\tRating: " + npc.getRating() + ", Rank: " + npc.getRank() + "\n\tTemplateType: " + npc.getNpcTemplateType()
					+ ", AbyssType: " + npc.getAbyssNpcType() + "\n\tRelative XP reward: " + StatFunctions.calculateExperienceReward(admin.getLevel(), npc));
				if (npc instanceof SiegeNpc)
					sendInfo(admin, "[Siege info]\n\tSiegeId: " + ((SiegeNpc) npc).getSiegeId() + ", SiegeRace: " + ((SiegeNpc) npc).getSiegeRace());
				sendInfo(admin,
					"[AI info]\n\tAI: " + npc.getAi().getName() + "\n\tState: " + npc.getAi().getState() + ", SubState: " + npc.getAi().getSubState());
				sendInfo(admin,
					"[Sense range]\n\tRadius: " + npc.getAggroRange() + "\n\tSide: " + npc.getObjectTemplate().getBoundRadius().getSide() + ", Front: "
						+ npc.getObjectTemplate().getBoundRadius().getFront() + ", Upper: " + npc.getObjectTemplate().getBoundRadius().getUpper()
						+ "\n\tDirectional bound: " + PositionUtil.getDirectionalBound(npc, admin, true)
						+ "\n\tDistance: " + (npc.getAggroRange() + PositionUtil.getDirectionalBound(npc, admin, true)) + "\n\tCollision: "
						+ (npc.getAggroRange() - npc.getObjectTemplate().getBoundRadius().getCollision()));
				sendInfo(admin, "[Spawn info]\n\tStaticId: " + npc.getSpawn().getStaticId() + ", DistToSpawn: " + npc.getDistanceToSpawnLocation() + "m");
				if (npc.isPathWalker()) {
					sendInfo(admin, "\tRouteId: " + npc.getSpawn().getWalkerId());
					if (npc.getWalkerGroup() != null) {
						ClusteredNpc snpc = npc.getWalkerGroup().getClusterData(npc);
						sendInfo(admin, "\tWalkerGroupType: " + npc.getWalkerGroup().getWalkType() + ", XDelta: " + snpc.getXDelta() + ", YDelta: "
							+ snpc.getYDelta() + ", Index: " + snpc.getWalkerIndex());
					}
				} else if (npc.isRandomWalker()) {
					sendInfo(admin, "\tRandomWalkRange: " + npc.getSpawn().getRandomWalkRange() + "m");
				}
			}
			sendInfo(admin, createZoneInfo(creature));
			sendInfo(admin, "[Tribe]\n\tRace: " + creature.getRace() + ", Tribe: " + creature.getTribe() + ", TribeBase: " + creature.getBaseTribe());
			sendInfo(admin, "[Your relation]\n\tisEnemy: " + admin.isEnemy(creature) + ", canAttack: " + RestrictionsManager.canAttack(admin, target));
			sendInfo(admin, "[Targets relation]\n\tisEnemy: " + creature.isEnemy(admin) + ", Hostility: " + creature.getType(admin));
			sendInfo(admin, "[Life stats]\n\tHP: " + creature.getLifeStats().getCurrentHp() + " / " + creature.getLifeStats().getMaxHp() + "\n\tMP: "
				+ creature.getLifeStats().getCurrentMp() + " / " + creature.getLifeStats().getMaxMp());
			sendInfo(admin, createAggroInfo(creature));
		}
	}

	private String createZoneInfo(Creature creature) {
		FortressLocation fortress = SiegeService.getInstance().findFortress(creature.getWorldId(), creature.getX(), creature.getY(), creature.getZ());
		int townId = TownService.getInstance().getTownIdByPosition(creature);
		StringBuilder sb = new StringBuilder("[Current zone]");
		sb.append("\n\t" + creature.getPosition().toCoordString());
		sb.append("\n\tFortress Location ID: " + (fortress == null ? "-" : fortress.getLocationId()));
		sb.append("\n\tTown ID: " + (townId == 0 ? "-" : townId));
		sb.append("\n\tPvP: " + creature.isInsidePvPZone());
		return sb.toString();
	}

	private String createAggroInfo(Creature creature) {
		StringBuilder sb = new StringBuilder("[AggroList]");
		int aDmg = 0, eDmg = 0, tDmg = creature.getAggroList().getTotalDamage();
		for (AggroInfo ai : creature.getAggroList().getList()) {
			if (!(ai.getAttacker() instanceof Creature))
				continue;
			Creature master = ((Creature) ai.getAttacker()).getMaster();
			if (master instanceof Player) {
				if (master.getRace() == Race.ASMODIANS)
					aDmg += ai.getDamage();
				else
					eDmg += ai.getDamage();
			}
			sb.append("\n\tName: " + (master == null ? "NULL" : master.getName()) + " Dmg: " + ai.getDamage());
		}
		if (tDmg > 0) {
			sb.append("\n\tTotal Dmg: ").append(tDmg);
			sb.append("\n\t\t(A) Dmg: ").append(aDmg);
			sb.append("\n\t\t(E) Dmg: ").append(eDmg);
			sb.append("\n\t\t(N) Dmg: ").append(tDmg - aDmg - eDmg);
		}
		return sb.toString();
	}
}
