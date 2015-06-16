package instance.rakes;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
@InstanceID(301050000)
public class IDShulack_Rose_03Instance extends GeneralInstanceHandler {

	private AtomicBoolean firstCannon = new AtomicBoolean();
	private AtomicBoolean secondCannon = new AtomicBoolean();
	private AtomicBoolean thirdCannon = new AtomicBoolean();
	private AtomicBoolean fourthCannon = new AtomicBoolean();
	private AtomicBoolean teleportEnabled = new AtomicBoolean();
	private AtomicBoolean teleportEnabled2 = new AtomicBoolean();
	private AtomicInteger killedBoss = new AtomicInteger();

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 230727:
			case 230728:
			case 230729:
			case 230730:
				killedBoss.incrementAndGet();
				break;
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730782:
				if (!teleportEnabled.get()) {
					if (player.getInventory().getItemCountByItemId(185000150) != 0) {
						player.getInventory().decreaseByItemId(185000150, 1);
						teleportEnabled.compareAndSet(false, true);
						TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 730.1445f, 508.8899f, 1012.68414f, (byte) 0,
							TeleportAnimation.BEAM_ANIMATION);
					}
					else
						PacketSendUtility.sendPacket(player, STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM(new DescriptionId(1622805)));
				}
				else
					TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 730.1445f, 508.8899f, 1012.68414f, (byte) 0,
						TeleportAnimation.BEAM_ANIMATION);
				break;
			case 730792:
				if (firstCannon.compareAndSet(false, true)) {
					SkillEngine.getInstance().getSkill(npc, 20385, 60, npc).useNoAnimationSkill();
					spawn(230729, 569.7891f, 557.9851f, 1022.0135f, (byte) 90);
				}
				else
					PacketSendUtility.sendPacket(player, STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
				break;
			case 730794:
				if (secondCannon.compareAndSet(false, true)) {
					SkillEngine.getInstance().getSkill(npc, 20385, 60, npc).useNoAnimationSkill();
					spawn(230727, 523.2158f, 549.9458f, 1021.8255f, (byte) 0);
				}
				else
					PacketSendUtility.sendPacket(player, STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
				break;
			case 730772:
				if (thirdCannon.compareAndSet(false, true)) {
					SkillEngine.getInstance().getSkill(npc, 20385, 60, npc).useNoAnimationSkill();
					spawn(230730, 635.5690f, 539.4214f, 1031.0442f, (byte) 40);
				}
				else
					PacketSendUtility.sendPacket(player, STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
				break;
			case 730786:
				if (fourthCannon.compareAndSet(false, true)) {
					SkillEngine.getInstance().getSkill(npc, 20385, 60, npc).useNoAnimationSkill();
					spawn(230728, 609.4983f, 455.3484f, 1021.7974f, (byte) 60);
				}
				else
					PacketSendUtility.sendPacket(player, STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
				break;
			case 730788:
				if (killedBoss.compareAndSet(4, 0)) {
					SkillEngine.getInstance().getSkill(npc, 20385, 60, npc).useNoAnimationSkill();
					spawn(230741, 487.5103f, 508.6524f, 1032.8385f, (byte) 60);
				}
				else
					PacketSendUtility.sendPacket(player, STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
				break;
		}
	}
}
