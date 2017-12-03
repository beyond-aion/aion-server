package ai.instance.engulfedOphidianBridgeInstance;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ActionItemNpcAI;

/**
 * @author cheatkiller
 */
@AIName("engulfedophidianexplosiondevice")
public class ExplosionDeviceAI extends ActionItemNpcAI {

	private List<Npc> bomb = new ArrayList<>();

	public ExplosionDeviceAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (checkScroll(player)) {
			switch (getOwner().getNpcId()) {
				case 701969:
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701939 : 701953, 660.36865f, 461.0626f, 600.2547f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701939 : 701953, 667.22784f, 488.69467f, 599.8417f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701939 : 701953, 670.0791f, 458.5767f, 599.75f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701939 : 701953, 671.87946f, 472.70087f, 600.772f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701939 : 701953, 679.0547f, 488.28452f, 599.75f, (byte) 116));
					PacketSendUtility.broadcastToMap(getOwner(), 1402057);
					break;
				case 701970:
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701940 : 701954, 539.50714f, 430.42578f, 620.25f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701940 : 701954, 540.8305f, 438.79446f, 620.25f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701940 : 701954, 544.2634f, 448.85068f, 620.19464f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701940 : 701954, 535.0376f, 449.98453f, 620.25f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701940 : 701954, 532.6748f, 441.45563f, 620.25f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701940 : 701954, 528.1262f, 448.87216f, 620.3671f, (byte) 116));
					PacketSendUtility.broadcastToMap(getOwner(), 1402067);
					break;
				case 701971:
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 598.453f, 569.7365f, 590.91034f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 608.8183f, 568.04224f, 590.6276f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 616.0901f, 560.89703f, 590.6867f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 614.1525f, 547.63904f, 590.625f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 603.2911f, 542.8298f, 590.625f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 593.12506f, 547.4969f, 590.625f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701941 : 701955, 591.4903f, 559.3725f, 590.625f, (byte) 116));
					PacketSendUtility.broadcastToMap(getOwner(), 1402062);
					break;
				case 701972:
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 477.32898f, 537.0476f, 597.375f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 482.75482f, 546.7067f, 597.5f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 486.86075f, 523.6781f, 597.375f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 492.95834f, 533.3082f, 598.8186f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 493.98892f, 549.46606f, 597.6485f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 503.4563f, 526.20776f, 597.5f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 505.39758f, 551.0327f, 597.7016f, (byte) 116));
					bomb.add((Npc) spawn(player.getRace() == Race.ELYOS ? 701942 : 701956, 508.31046f, 539.70984f, 598.1651f, (byte) 116));
					PacketSendUtility.broadcastToMap(getOwner(), 1402072);
					break;
			}
			boom();
			player.getInventory().decreaseByItemId(164000278, 1);
		} else {
			PacketSendUtility.broadcastToMap(getOwner(), 1402005);
		}
	}

	private void boom() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (Npc npc : bomb) {
					npc.getController().useSkill(21178);
				}
			}
		}, 5000);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				for (Npc npc : bomb) {
					npc.getController().delete();
				}
			}
		}, 20000);
	}

	private boolean checkScroll(Player player) {
		Item key = player.getInventory().getFirstItemByItemId(164000278);
		if (key != null && key.getItemCount() >= 1) {
			return true;
		}
		return false;
	}
}
