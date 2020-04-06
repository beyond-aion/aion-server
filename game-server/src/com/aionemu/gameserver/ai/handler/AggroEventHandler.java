package com.aionemu.gameserver.ai.handler;

import java.util.Collections;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class AggroEventHandler {

	private static final int SUPPORT_RANGE_OFFSET = 2; // might be <pushed_range> in client data
	/**
	 * @param npcAI
	 * @param myTarget
	 */
	public static void onAggro(NpcAI npcAI, final Creature myTarget) {
		final Npc owner = npcAI.getOwner();
		// TODO move out?
		if (myTarget instanceof Player) {
			if (((Player) myTarget).isInCustomState(CustomPlayerState.NEUTRAL_TO_ALL_NPCS))
				return;
		} else if (TribeRelationService.isFriend(owner, myTarget) || myTarget.isFlag())
			return;
		PacketSendUtility.broadcastPacket(owner,
			new SM_ATTACK(owner, myTarget, 0, 633, AttackTypeAnimation.MELEE, AttackHandAnimation.MAIN_HAND, Collections.singletonList(new AttackResult(0, AttackStatus.NORMALHIT))));

		ThreadPoolManager.getInstance().schedule(new AggroNotifier(owner, myTarget, true), 500);
		owner.getPosition().getWorldMapInstance().getInstanceHandler().onAggro(owner);
	}

	public static boolean onCreatureNeedsSupport(NpcAI npcAI, Creature creatureAskingForSupport) {
		Npc owner = npcAI.getOwner();
		if (TribeRelationService.canHelpCreature(creatureAskingForSupport, owner) && PositionUtil.isInRange(owner, creatureAskingForSupport, owner.getAggroRange() + SUPPORT_RANGE_OFFSET, false)
			&& GeoService.getInstance().canSee(owner, creatureAskingForSupport)) {
			VisibleObject attacker = creatureAskingForSupport.getTarget();
			if (attacker instanceof Creature) {
				Creature targetCreature = (Creature) attacker;
				PacketSendUtility.broadcastPacket(owner,
					new SM_ATTACK(owner, targetCreature, 0, 633, AttackTypeAnimation.MELEE, AttackHandAnimation.MAIN_HAND, Collections.singletonList(new AttackResult(0, AttackStatus.NORMALHIT))));
				ThreadPoolManager.getInstance().schedule(new AggroNotifier(owner, targetCreature, false), 500);
				return true;
			}
		}
		return false;
	}

	public static boolean onGuardAgainstAttacker(NpcAI npcAI, Creature attacker) {
		Npc owner = npcAI.getOwner();
		TribeClass tribe = owner.getTribe();
		if (!tribe.isGuard() && owner.getObjectTemplate().getNpcTemplateType() != NpcTemplateType.GUARD) {
			return false;
		}
		VisibleObject target = attacker.getTarget();
		if (target instanceof Player) {
			Player playerTarget = (Player) target;
			if (!owner.isEnemy(playerTarget) && owner.isEnemy(attacker) && PositionUtil.isInRange(owner, playerTarget, owner.getAggroRange() + SUPPORT_RANGE_OFFSET, false)
				&& GeoService.getInstance().canSee(owner, attacker)) {
				owner.getAggroList().addHate(attacker, 1);
				return true;
			}
		}
		return false;
	}

	private static final class AggroNotifier implements Runnable {

		private Npc aggressive;
		private Creature target;
		private boolean broadcast;

		AggroNotifier(Npc aggressive, Creature target, boolean broadcast) {
			this.aggressive = aggressive;
			this.target = target;
			this.broadcast = broadcast;
		}

		@Override
		public void run() {
			aggressive.getAggroList().addHate(target, 1);
			if (broadcast) {
				aggressive.getKnownList().forEachNpc(new Consumer<Npc>() {

					@Override
					public void accept(Npc object) {
						object.getAi().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, aggressive);
					}
				});
			}
			aggressive = null;
			target = null;
		}

	}

}
