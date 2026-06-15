package instance.abyss;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author zhkchi, vlog, Luzien
 * @see <a href="https://aion.fandom.com/wiki/Abyssal_Splinter">Abyssal Spliter</a>
 */
@InstanceID(300220000)
public class AbyssalSplinterInstance extends GeneralInstanceHandler {

	private int destroyedFragments;
	private int killedPazuzuWorms;

	public AbyssalSplinterInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc npc) {
			switch (npc.getNpcId()) {
				case 216960 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdDH_Wakeup());
				case 216952 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdD_Wakeup());
			}
		}
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 216951: // Pazuzu
				spawnPazuzuFragment();
				spawnPazuzuTreasureBoxes();
				break;
			case 216950: // Kaluva the Fourth Fragment
				spawnKaluvaFragment();
				spawnKaluvaTreasureBoxes();
				break;
			case 216948: // Rukril
			case 216949: // Ebonsoul
				if (getNpc(npcId == 216949 ? 216948 : 216949) == null) {
					spawnDayshadeFragment();
					spawnDayshadeTreasureBoxes();
				} else {
					sendMsg(npcId == 216948 ? SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_Light_Die() : SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_Dark_Die());
					ThreadPoolManager.getInstance().schedule(() -> {
						if (getNpc(npcId == 216949 ? 216948 : 216949) != null) {
							switch (npcId) {
								case 216948 -> spawn(216948, 447.1937f, 683.72217f, 433.1805f, (byte) 108); // rukril
								case 216949 -> spawn(216949, 455.5502f, 702.09485f, 433.13727f, (byte) 108); // ebonsoul
							}
						}
					}, 60000);
				}
				npc.getController().delete();
				break;
			case 281907: // Piece of Splendor
				Npc ebonsoul = getNpc(216949);
				if (ebonsoul != null && !ebonsoul.isDead()) {
					if (PositionUtil.isInRange(npc, ebonsoul, 5)) {
						ebonsoul.getEffectController().removeEffect(19159);
						deleteAliveNpcs(281907);
						break;
					}
				}
				npc.getController().delete();
				break;
			case 281908: // Piece of Midnight
				Npc rukril = getNpc(216948);
				if (rukril != null && !rukril.isDead()) {
					if (PositionUtil.isInRange(npc, rukril, 5)) {
						rukril.getEffectController().removeEffect(19266);
						deleteAliveNpcs(281908);
						break;
					}
				}
				npc.getController().delete();
				break;
			case 216960: // Yamennes Painflare
			case 216952: // Yamennes Blindsight
				spawnYamennesTreasureBoxes(npcId == 216952 ? 700937 : 700938);
				deleteAliveNpcs(282107);
				spawn(730317, 328.476f, 762.585f, 197.479f, (byte) 90); // Exit
				break;
			case 700955: // Huge Aether Fragment
				destroyedFragments++;
				onFragmentKill();
				npc.getController().delete();
				break;
			case 281909:
				if (++killedPazuzuWorms == 5) {
					killedPazuzuWorms = 0;
					Npc pazuzu = getNpc(216951);
					if (pazuzu != null && !pazuzu.isDead()) {
						pazuzu.getEffectController().removeEffect(19145);
						pazuzu.getEffectController().removeEffect(19291);
					}
				}
				npc.getController().delete();
				break;
			case 282014: // Spawn Gate
			case 282015: // Spawn Gate
			case 282131: // Spawn Gate
				deleteSummons();
				break;
		}
	}

	@Override
	public void onInstanceDestroy() {
		destroyedFragments = 0;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 700862: // Broken Orkanimum
				int itemId = player.getRace() == Race.ASMODIANS ? 182209820 : 182209800;
				if (player.getInventory().getFirstItemByItemId(itemId) == null)
					ItemService.addItem(player, itemId, 1);
				break;
			case 700865: // Worn Book
				if (player.getRace() == Race.ASMODIANS && player.getInventory().getFirstItemByItemId(182209824) == null)
					ItemService.addItem(player, 182209824, 1);
				break;
			case 700864: // Polearm of Akarios
				if (player.getRace() == Race.ELYOS && player.getInventory().getFirstItemByItemId(182209803) == null)
					ItemService.addItem(player, 182209803, 1);
				break;
			case 701593: { // Artifact of Protection (Hard Mode)
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdDH_Wakeup());
				spawn(216960, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
				int artifactOfProtection = player.getRace() == Race.ELYOS ? 700857 : 700858; // for quest 30255 / 30355
				spawn(artifactOfProtection, 326.1821f, 766.9640f, 202.1832f, (byte) 100, 79);
				npc.getController().die();
				break;
			}
			case 700856: // Artifact of Protection (Easy Mode)
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdD_Wakeup());
				spawn(216952, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
				int artifactOfProtection = player.getRace() == Race.ELYOS ? 700857 : 700858; // for quest 30255 / 30355
				spawn(artifactOfProtection, 326.1821f, 766.9640f, 202.1832f, (byte) 100, 79);
				npc.getController().die();
				break;
		}
	}

	private void spawnPazuzuFragment() {
		spawn(700955, 669.576f, 335.135f, 465.895f, (byte) 0);
	}

	private void spawnPazuzuTreasureBoxes() {
		spawn(700934, 651.53204f, 357.085f, 466.1315f, (byte) 66); // Genesis Treasure Box
		spawn(700934, 647.00446f, 357.2484f, 465.8960f, (byte) 0); // Genesis Treasure Box
		spawn(700934, 653.8384f, 360.39508f, 466.4391f, (byte) 100); // Genesis Treasure Box
		spawn(700860, 649.24286f, 361.33755f, 466.0427f, (byte) 33); // Abyssal Treasure Box
		if (Rnd.chance() < 12)
			spawn(700861, 661.061f, 357.587f, 465.991f, (byte) 100, 67); // Pazuzu's Treasure Box
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
	}

	private void spawnKaluvaFragment() {
		spawn(700955, 633.7498f, 557.8822f, 424.99347f, (byte) 6);
	}

	private void spawnKaluvaTreasureBoxes() {
		spawn(700934, 601.2931f, 584.66705f, 422.9955f, (byte) 6); // Genesis Treasure Box
		spawn(700934, 597.2156f, 583.95416f, 423.3474f, (byte) 66); // Genesis Treasure Box
		spawn(700934, 602.9586f, 589.2678f, 422.8296f, (byte) 100); // Genesis Treasure Box
		spawn(700935, 598.82776f, 588.25946f, 422.7739f, (byte) 113); // Abyssal Treasure Box
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
	}

	private void spawnDayshadeFragment() {
		spawn(700955, 452.89706f, 692.36084f, 433.96838f, (byte) 6);
	}

	private void spawnDayshadeTreasureBoxes() {
		spawn(700934, 408.10938f, 650.9015f, 439.28332f, (byte) 66); // Genesis Treasure Box
		spawn(700934, 402.40375f, 655.55237f, 439.26288f, (byte) 33); // Genesis Treasure Box
		spawn(700934, 406.74445f, 655.5914f, 439.2548f, (byte) 100); // Genesis Treasure Box
		spawn(700936, 404.891f, 650.2943f, 439.2548f, (byte) 130); // Abyssal Treasure Box
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
	}

	private void spawnYamennesTreasureBoxes(int npcId) {
		spawn(700934, 326.978f, 729.8414f, 197.7078f, (byte) 16); // Genesis Treasure Box
		spawn(700934, 326.5296f, 735.13324f, 197.6681f, (byte) 66); // Genesis Treasure Box
		spawn(700934, 329.8462f, 738.41095f, 197.7329f, (byte) 3); // Genesis Treasure Box
		spawn(npcId, 330.891f, 733.2943f, 197.6404f, (byte) 113); // Abyssal Treasure Box
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
	}

	private void deleteSummons() {
		if (instance.getNpcs(282014, 282015, 282131).stream().allMatch(Creature::isDead))
			deleteAliveNpcs(281903, 281904); // Summoned Orkanimum, Summoned Lapilima
	}

	private void onFragmentKill() {
		switch (destroyedFragments) {
			case 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_01());
			case 2 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_02());
			case 3 -> {
				deleteAliveNpcs(700856); // Artifact of Protection (Easy Mode)
				spawn(701593, 326.1821f, 766.9640f, 202.1832f, (byte) 100, 79); // Artifact of Protection (Hard Mode)
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_03());
			}
		}
	}
}
