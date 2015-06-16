package admincommands;

import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
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
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Nemiroff Date: 28.12.2009
 */
public class Info extends AdminCommand {

	public Info() {
		super("info");
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();
	
		if (target instanceof Player) {
			Player player = (Player) target;
			PacketSendUtility.sendMessage(admin, "[Info about " + player.getName() +"]"
				+ "\nPlayer Id: " + player.getObjectId()
				+ "\nMap ID: " + player.getWorldId()
				+ "\nX: " + player.getPosition().getX() + " / Y: " + player.getPosition().getY()
				+ " / Z: " + player.getPosition().getZ() + " / Heading: " + player.getPosition().getHeading()
				+ "\n Town ID: "+TownService.getInstance().getTownResidence(player)
				+ "\n Tribe: "+ player.getTribe()
				+ "\n TribeBase: "+ player.getBaseTribe());

			PacketSendUtility.sendMessage(admin, "[Stats]"
				+ "\nPvP attack: " + player.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent() * 0.1f + "%"
				+ "\nPvP p. attack: " + player.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_PHYSICAL, 0).getCurrent() * 0.1f + "%"
				+ "\nPvP m. attack: " + player.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_MAGICAL, 0).getCurrent() * 0.1f + "%"
				+ "\nPvP defend: " + player.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent() * 0.1f + "%"
				+ "\nPvP p. defend: " + player.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_PHYSICAL, 0).getCurrent() * 0.1f + "%"
				+ "\nPvP m. defend: " + player.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_MAGICAL, 0).getCurrent() * 0.1f + "%"
				+ "\nCast Time Boost: +" + (player.getGameStats().getStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() * 0.1f - 100) + "%"
				+ "\nAttack Speed: " + player.getGameStats().getAttackSpeed().getCurrent() * 0.001f
				+ "\nMovement Speed: " + player.getGameStats().getMovementSpeedFloat()
				+ "\n----------Main Hand------------\nAttack: " + player.getGameStats().getMainHandPAttack().getCurrent()
				+ "\nAccuracy: " + player.getGameStats().getMainHandPAccuracy().getCurrent()
				+ "\nCritical: " + player.getGameStats().getMainHandPCritical().getCurrent()
				+ "\n------------Off Hand------------\nAttack: " + player.getGameStats().getOffHandPAttack().getCurrent()
				+ "\nAccuracy: " + player.getGameStats().getOffHandPAccuracy().getCurrent()
				+ "\nCritical: " + player.getGameStats().getOffHandPCritical().getCurrent()
				+ "\n-------------Magical-------------\nAttack: " + player.getGameStats().getMainHandMAttack().getCurrent()
				+ "\nAccuracy: " + player.getGameStats().getMAccuracy().getCurrent()
				+ "\nCritical: " + player.getGameStats().getMCritical().getCurrent()
				+ "\nBoost: " + player.getGameStats().getMBoost().getCurrent()
				+ "\n-------------Protect--------------\nPhysical Defence: " + player.getGameStats().getPDef().getCurrent()
				+ "\nBlock: " + player.getGameStats().getBlock().getCurrent()
				+ "\nParry: " + player.getGameStats().getParry().getCurrent()
				+ "\nEvasion: " + player.getGameStats().getEvasion().getCurrent()
				+ "\nMagic Resist: " + player.getGameStats().getMResist().getCurrent());

			for (int i = 0; i < 2; i++) {
				NpcFaction faction = player.getNpcFactions().getActiveNpcFaction(i == 0);
				if (faction != null) {
					PacketSendUtility.sendMessage(admin, player.getName() + " have join to " + (i == 0 ? "mentor" : "daily") + " faction: " + DataManager.NPC_FACTIONS_DATA.getNpcFactionById(faction.getId()).getName()
						+ "\nCurrent quest state: " + faction.getState().name()
						+ (faction.getState().equals(ENpcFactionQuestState.COMPLETE) ? ("\nNext after: " + ((faction.getTime() - System.currentTimeMillis() / 1000) / 3600f) + " h.") : ""));
				}
			}
			PacketSendUtility.sendMessage(admin, "[Life stats]"
												 + "\nisAlive: " + !player.getLifeStats().isAlreadyDead()
												 + "\nHP: " + player.getLifeStats().getCurrentHp() + " / " + player.getLifeStats().getMaxHp()
												 + "\nMP: " + player.getLifeStats().getCurrentMp() + " / " + player.getLifeStats().getMaxMp());
			PacketSendUtility.sendMessage(admin, "[Current Zone Info]"
					+ "\nPvP: " + player.isInsidePvPZone());
		}
		else if (target instanceof Summon) {
			Summon summon = (Summon) admin.getTarget();
			PacketSendUtility.sendMessage(admin, "[Current Zone Info]"
					+ "\nPvP: " + summon.isInsidePvPZone());

			int asmoDmg = 0;
			int elyDmg = 0;
			int npcDmg = 0;
			PacketSendUtility.sendMessage(admin, "[AgroList]");
			for (AggroInfo ai : summon.getAggroList().getList()) {
				if (!(ai.getAttacker() instanceof Creature))
					continue;
				Creature master = ((Creature) ai.getAttacker()).getMaster();
				if (master == null)
					continue;
				if (master instanceof Player) {
					Player player = (Player) master;
					PacketSendUtility.sendMessage(admin, "Name: " + player.getName() + " Dmg: " + ai.getDamage());
					if (player.getRace() == Race.ASMODIANS)
						asmoDmg += ai.getDamage();
					else
						elyDmg += ai.getDamage();
				}
				else
					npcDmg += ai.getDamage();
			}
			PacketSendUtility.sendMessage(admin, "[TotalDmg]" + "\n(A) Dmg: " + asmoDmg + "\n(E) Dmg: " + elyDmg + "\n(N) Dmg: " + npcDmg);
		}
		else if (target instanceof Npc) {
			Npc npc = (Npc) admin.getTarget();
			PacketSendUtility.sendMessage(admin,
				"[Info about target]" 
					+ "\nName: " + npc.getName() 
					+ "\nId: " + npc.getNpcId() + " / ObjectId: " + admin.getTarget().getObjectId() + " / StaticId: " + npc.getSpawn().getStaticId()
					+ "\nMap ID: " + admin.getTarget().getWorldId()
					+ "\nX: " + admin.getTarget().getX() + " / Y: " + admin.getTarget().getY()
					+ " / Z: " + admin.getTarget().getZ() + " / Heading: " + admin.getTarget().getHeading()
					+ " / Angle: " + PositionUtil.getAngleToTarget(admin, admin.getTarget()) + " \n Town ID:"+TownService.getInstance().getTownIdByPosition((Creature) target));
			if (npc instanceof SiegeNpc){
				SiegeNpc siegeNpc = (SiegeNpc)npc;
				PacketSendUtility.sendMessage(admin,
					"[Siege info]" + "\nSiegeId: "+ siegeNpc.getSiegeId() + "\nSiegeRace: "+ siegeNpc.getSiegeRace());
			}
			PacketSendUtility.sendMessage(admin, "Tribe: " + npc.getTribe()
				+ "\n TribeBase: " + npc.getBaseTribe()
				+ "\nRace: " + npc.getObjectTemplate().getRace()
				+ "\nNpcType: " + CreatureType.getCreatureType(npc.getType(admin))
				+
				"\nTemplateType: "
				+ npc.getObjectTemplate().getNpcTemplateType().name() + "\nAbyssType: "
				+ npc.getObjectTemplate().getAbyssNpcType().name() + "\nAI: " + npc.getAi2().getName() + "\n NpcRating: "+npc.getObjectTemplate().getRating().name());
			PacketSendUtility.sendMessage(
				admin,
				"[Relations to target]" + "\nisEnemy: " + admin.isEnemy(npc) + "\ncanAttack: "
					+ RestrictionsManager.canAttack(admin, target) + "\n[Relations to you]" + "\nisEnemy: " + npc.isEnemy(admin)
					+ "\nisAggressive: " + TribeRelationService.isAggressive(npc, admin));
			PacketSendUtility.sendMessage(admin, "[Life stats]" + "\nHP: " + npc.getLifeStats().getCurrentHp()
				+ " / " + npc.getLifeStats().getMaxHp() + "\nMP: " + npc.getLifeStats().getCurrentMp()
				+ " / " + npc.getLifeStats().getMaxMp()
				+ "\nXP: " + npc.getObjectTemplate().getStatsTemplate().getMaxXp());
			PacketSendUtility.sendMessage(admin, "[Sense range]" + "\nRadius: " + npc.getAggroRange()
				+ "\nSide: " + npc.getObjectTemplate().getBoundRadius().getSide() + " / Front: " + npc.getObjectTemplate().getBoundRadius().getFront()
				+ "\nDirectional bound: " + PositionUtil.getDirectionalBound(npc, admin, true)
				+ "\nDistance: " + (npc.getAggroRange() + PositionUtil.getDirectionalBound(npc, admin, true))
				+ "\nCollision: " + (npc.getAggroRange() - npc.getCollision()));

			PacketSendUtility.sendMessage(admin, "[Current Zone Info]"
					+ "\nPvP: " + npc.isInsidePvPZone());

			int asmoDmg = 0;
			int elyDmg = 0;
			int npcDmg = 0;
			PacketSendUtility.sendMessage(admin, "[AgroList]");
			for (AggroInfo ai : npc.getAggroList().getList()) {
				if (!(ai.getAttacker() instanceof Creature))
					continue;
				Creature master = ((Creature) ai.getAttacker()).getMaster();
				if (master == null)
					continue;
				if (master instanceof Player) {
					Player player = (Player) master;
					PacketSendUtility.sendMessage(admin, "Name: " + player.getName() + " Dmg: " + ai.getDamage());
					if (player.getRace() == Race.ASMODIANS)
						asmoDmg += ai.getDamage();
					else
						elyDmg += ai.getDamage();
				}
				else
					npcDmg += ai.getDamage();
			}
			PacketSendUtility.sendMessage(admin, "[TotalDmg]" + "\n(A) Dmg: " + asmoDmg + "\n(E) Dmg: " + elyDmg + "\n(N) Dmg: " + npcDmg);
			if (npc.getSpawn().getWalkerId() != null)
			{
				WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(npc.getSpawn().getWalkerId());
				if (template != null) {
					PacketSendUtility.sendMessage(admin, "[Route]" + "\nRouteId: " + npc.getSpawn().getWalkerId() + " (Reversed: "
						+ template.isReversed() + ")" + "\nRandomWalk: " + npc.getSpawn().getRandomWalk());
					if (npc.getWalkerGroup() != null) {
						ClusteredNpc snpc = npc.getWalkerGroup().getClusterData(npc);
						PacketSendUtility.sendMessage(admin, "[Group]" + "\nType: " + npc.getWalkerGroup().getWalkType() + 
							" / XDelta: " + snpc.getXDelta() + " / YDelta: " + snpc.getYDelta() + " / Index: " + snpc.getWalkerIndex());						
					}
				}
			}
		}
		else if (target instanceof Gatherable) {
			Gatherable gather = (Gatherable) target;
			PacketSendUtility.sendMessage(admin, "[Info about gather]\n" + "Name: " + gather.getName()
				+ "\nId: " + gather.getObjectTemplate().getTemplateId() + " / ObjectId: " + admin.getTarget().getObjectId()
				+ "\nMap ID: " + admin.getTarget().getWorldId()
				+ "\nX: " + admin.getTarget().getX() + " / Y: " + admin.getTarget().getY() + " / Z: " + admin.getTarget().getZ()
				+ " / Heading: " + admin.getTarget().getHeading());
		}
	}


	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}

}