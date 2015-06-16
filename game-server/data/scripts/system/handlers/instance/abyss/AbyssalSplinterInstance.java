package instance.abyss;

import java.util.List;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author zhkchi
 * @reworked vlog, Luzien
 * @see http://gameguide.na.aiononline.com/aion/Abyssal+Splinter+Walkthrough
 */
@InstanceID(300220000)
public class AbyssalSplinterInstance extends GeneralInstanceHandler {

	private int destroyedFragments;
	private int killedPazuzuWorms = 0;
	private boolean bossSpawned = false;

	@Override
	public void onDie(Npc npc) {
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 216951: // Pazuzu the Life Current
				spawnPazuzuHugeAetherFragment();
				spawnPazuzuGenesisTreasureBoxes();
				spawnPazuzuAbyssalTreasureBox();
				spawnPazuzusTreasureBox();
				break;
			case 216950: // Kaluva the Fourth Fragment
				spawnKaluvaHugeAetherFragment();
				spawnKaluvaGenesisTreasureBoxes();
				spawnKaluvaAbyssalTreasureBox();
				break;
			case 216948: //rukril 
			case 216949: //ebonsoul
				if (getNpc(npcId == 216949 ? 216948 : 216949) == null) {
					spawnDayshadeAetherFragment();
					spawnDayshadeGenesisTreasureBoxes();
					spawnDayshadeAbyssalTreasureChest();
				}
				else {
					sendMsg(npcId == 216948 ? 1400634 : 1400635); //Defeat Rukril/Ebonsoul in 1 min!
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {

							if (getNpc(npcId == 216949 ? 216948 : 216949) != null) {
								switch (npcId) {
									case 216948:
										spawn(216948, 447.1937f, 683.72217f, 433.1805f, (byte) 108); // rukril
										break;
									case 216949:
										spawn(216949, 455.5502f, 702.09485f, 433.13727f, (byte) 108); // ebonsoul
										break;
								}
							}
						}

					}, 60000);
				}
				npc.getController().onDelete();
				break;
			case 281907:
				Npc ebonsoul = getNpc(216949);
				if (ebonsoul != null && !ebonsoul.getLifeStats().isAlreadyDead()) {
					if (MathUtil.isIn3dRange(npc, ebonsoul, 5)) {
						ebonsoul.getEffectController().removeEffect(19159);
						deleteNpcs(instance.getNpcs(281907));
						break;
					}
				}
				npc.getController().onDelete();
				break;
			case 281908:
				Npc rukril = getNpc(216948);
				if (rukril != null && !rukril.getLifeStats().isAlreadyDead()) {
					if (MathUtil.isIn3dRange(npc, rukril, 5)) {
						rukril.getEffectController().removeEffect(19266);
						deleteNpcs(instance.getNpcs(281908));
						break;
					}
				}
				npc.getController().onDelete();
				break;
			case 216960: // Yamennes Painflare
			case 216952: // Yamennes Blindsight
				spawnYamennesGenesisTreasureBoxes();
				spawnYamennesAbyssalTreasureBox(npcId == 216952 ? 700937 : 700938);
				deleteNpcs(instance.getNpcs(282107));
				spawn(730317, 328.476f, 762.585f, 197.479f, (byte) 90); //Exit
				break;
			case 700955: // HugeAetherFragment
				destroyedFragments++;
				onFragmentKill();
				npc.getController().onDelete();
				break;

			case 281909:
				if (++killedPazuzuWorms == 5) {
					killedPazuzuWorms = 0;
					Npc pazuzu = getNpc(216951);
					if (pazuzu != null && !pazuzu.getLifeStats().isAlreadyDead()) {
						pazuzu.getEffectController().removeEffect(19145);
						pazuzu.getEffectController().removeEffect(19291);
					}
				}
				npc.getController().onDelete();
				break;
			case 282014:
			case 282015:
			case 282131:// Spawn Gate
				removeSummoned();
				npc.getController().onDelete();
				break;
		}
	}

	private boolean isSpawned(int npcId) {
		return !instance.getNpcs(npcId).isEmpty();
	}

	@Override
	public void onInstanceDestroy() {
		destroyedFragments = 0;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (npc.getNpcId() == 700856) {
			QuestEnv env = new QuestEnv(npc, player, 0, 0);
			QuestEngine.getInstance().onDialog(env);
			if (!isSpawned(216960) && !isSpawned(216952) && !bossSpawned) { // No bosses spawned
				if (!isSpawned(700955) && destroyedFragments == 3) { // No Huge Aether Fragments spawned (all destroyed)
					sendMsg(1400732);
					spawn(216960, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
				}
				else {
					sendMsg(1400731);
					spawn(216952, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
				}
				bossSpawned = true;
			}
		}
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0
			: lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

	private void spawnPazuzuHugeAetherFragment() {
		spawn(700955, 669.576f, 335.135f, 465.895f, (byte) 0);
	}

	private void spawnPazuzuGenesisTreasureBoxes() {
		spawn(700934, 651.53204f, 357.085f, 466.8837f, (byte) 66);
		spawn(700934, 647.00446f, 357.2484f, 466.14117f, (byte) 0);
		spawn(700934, 653.8384f, 360.39508f, 466.8837f, (byte) 100);
	}

	private void spawnPazuzuAbyssalTreasureBox() {
		spawn(700860, 649.24286f, 361.33755f, 467.89145f, (byte) 33);
	}

	private void spawnPazuzusTreasureBox() {
		if (Rnd.get(0, 100) >= 80) { // 20% chance, not retail
			spawn(700861, 649.243f, 362.338f, 466.0451f, (byte) 0);
		}
	}

	private void spawnKaluvaHugeAetherFragment() {
		spawn(700955, 633.7498f, 557.8822f, 424.99347f, (byte) 6);
	}

	private void spawnKaluvaGenesisTreasureBoxes() {
		spawn(700934, 601.2931f, 584.66705f, 424.2829f, (byte) 6);
		spawn(700934, 597.2156f, 583.95416f, 424.2829f, (byte) 66);
		spawn(700934, 602.9586f, 589.2678f, 424.2829f, (byte) 100);
	}

	private void spawnKaluvaAbyssalTreasureBox() {
		spawn(700935, 598.82776f, 588.25946f, 424.29065f, (byte) 113);
	}

	private void spawnDayshadeAetherFragment() {
		spawn(700955, 452.89706f, 692.36084f, 433.96838f, (byte) 6);
	}

	private void spawnDayshadeGenesisTreasureBoxes() {
		spawn(700934, 408.10938f, 650.9015f, 439.28332f, (byte) 66);
		spawn(700934, 402.40375f, 655.55237f, 439.26288f, (byte) 33);
		spawn(700934, 406.74445f, 655.5914f, 439.2548f, (byte) 100);
	}

	private void spawnDayshadeAbyssalTreasureChest() {
		sendMsg(1400636); //A Treasure Box Appeared
		spawn(700936, 404.891f, 650.2943f, 439.2548f, (byte) 130);
	}

	private void spawnYamennesGenesisTreasureBoxes() {
		spawn(700934, 326.978f, 729.8414f, 198.46796f, (byte) 16);
		spawn(700934, 326.5296f, 735.13324f, 198.46796f, (byte) 66);
		spawn(700934, 329.8462f, 738.41095f, 198.46796f, (byte) 3);
	}

	private void spawnYamennesAbyssalTreasureBox(int npcId) {
		spawn(npcId, 330.891f, 733.2943f, 198.55286f, (byte) 113);
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private void removeSummoned()
	{
		Npc gate1 = getNpc(282014);
		Npc gate2 = getNpc(282015);
		Npc gate3 = getNpc(282131);
		if((gate1 == null || gate1.getLifeStats().isAlreadyDead())
			&& (gate2 == null || gate2.getLifeStats().isAlreadyDead()) 
			&& (gate3 == null || gate3.getLifeStats().isAlreadyDead()))
		{
			deleteNpcs(instance.getNpcs(281903));// Summoned Orkanimum
			deleteNpcs(instance.getNpcs(281904));// Summoned Lapilima
		}
	}

	private void onFragmentKill() {
		switch (destroyedFragments) {
			case 1:
				// The destruction of the Huge Aether Fragment has destabilized the artifact!
				sendMsg(1400689);
				break;
			case 2:
				// The destruction of the Huge Aether Fragment has put the artifact protector on alert!
				sendMsg(1400690);
				break;
			case 3:
				// The destruction of the Huge Aether Fragment has caused abnormality on the artifact. The artifact protector is
				// furious!
				sendMsg(1400691);
				break;
		}
	}
}
