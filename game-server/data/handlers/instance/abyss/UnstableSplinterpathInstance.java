package instance.abyss;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author zhkchi, vlog, Luzien, Cheatkiller
 * @see <a href="https://aion.fandom.com/wiki/Abyssal_Splinter">Abyssal Splinter</a>
 */
@InstanceID(300600000)
public class UnstableSplinterpathInstance extends GeneralInstanceHandler {

	private int destroyedFragments;
	private int killedPazuzuWorms;

	public UnstableSplinterpathInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc npc) {
			switch (npc.getNpcId()) {
				case 219563 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdDH_Wakeup());
				case 219555 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdD_Wakeup());
			}
		}
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 219554: // Unstable Pazuzu
				spawnPazuzuFragment();
				spawnPazuzuTreasureBoxes();
				break;
			case 219553: // Unstable Kaluva the Fourth
				spawnKaluvaFragment();
				spawnKaluvaTreasureBoxes();
				break;
			case 219551: // Unstable Rukril
			case 219552: // Unstable Ebonsoul
				if (getNpc(npcId == 219552 ? 219551 : 219552) == null) {
					spawnDayshadeFragment();
					spawnDayshadeTreasureBoxes();
				} else {
					sendMsg(npcId == 219551 ? SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_Light_Die() : SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_Dark_Die());
					ThreadPoolManager.getInstance().schedule(() -> {
						if (getNpc(npcId == 219552 ? 219551 : 219552) != null) {
							switch (npcId) {
								case 219551 -> spawn(219552, 447.1937f, 683.72217f, 433.1805f, (byte) 108); // rukril
								case 219552 -> spawn(219551, 455.5502f, 702.09485f, 433.13727f, (byte) 108); // ebonsoul
							}
						}
					}, 60000);
				}
				npc.getController().delete();
				break;
			case 283204: // Piece of Splendor
				Npc ebonsoul = getNpc(219552);
				if (ebonsoul != null && !ebonsoul.isDead() && PositionUtil.isInRange(npc, ebonsoul, 5)) {
					ebonsoul.getEffectController().removeEffect(19159);
					deleteAliveNpcs(281907);
					break;
				}
				npc.getController().delete();
				break;
			case 283205: // Piece of Midnight
				Npc rukril = getNpc(219551);
				if (rukril != null && !rukril.isDead() && PositionUtil.isInRange(npc, rukril, 5)) {
					rukril.getEffectController().removeEffect(19266);
					deleteAliveNpcs(281908);
					break;
				}
				npc.getController().delete();
				break;
			case 219555: // Durable Yamennes Blindsight
			case 219563: // Unstable Yamennes Painflare
				spawnYamennesTreasureBoxes(npcId == 219555 ? 701579 : 701580);
				deleteAliveNpcs(219586); // Summoned Unstable Ametgolem
				spawn(730317, 328.476f, 762.585f, 197.479f, (byte) 90); // Exit
				instance.forEachPlayer(p -> SkillEngine.getInstance().applyEffectDirectly(19283, p, p));
				break;
			case 701588: // Huge Aether Fragment
				destroyedFragments++;
				onFragmentKill();
				npc.getController().delete();
				break;
			case 283206: // Luminous Waterworm
				if (++killedPazuzuWorms == 4) {
					killedPazuzuWorms = 0;
					Npc pazuzu = getNpc(219554);
					if (pazuzu != null && !pazuzu.isDead()) {
						pazuzu.getEffectController().removeEffect(19145);
						pazuzu.getEffectController().removeEffect(19291);
					}
				}
				break;
			case 219567: // Spawn Gate
			case 219579: // Spawn Gate
			case 219580: // Spawn Gate
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
		int bossId = switch (npc.getNpcId()) { // Artifact of Protection
			case 700957 -> 219563; // Unstable Yamennes Painflare (Hard Mode)
			case 701589 -> 219555; // Durable Yamennes Blindsight (Easy Mode)
			default -> 0;
		};
		if (bossId != 0) {
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdDH_Wakeup());
			spawn(bossId, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
			npc.getController().delete();
		}
	}

	private void spawnPazuzuFragment() {
		spawn(701588, 669.576f, 335.135f, 465.895f, (byte) 0);
	}

	private void spawnPazuzuTreasureBoxes() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
		spawn(701575, 649.24286f, 361.33755f, 466.0427f, (byte) 33); // Abyssal Treasure Box
		spawn(701576, 651.53204f, 357.085f, 466.1315f, (byte) 66); // Genesis Treasure Box
		spawn(701576, 647.00446f, 357.2484f, 465.8960f, (byte) 0); // Genesis Treasure Box
		spawn(701576, 653.8384f, 360.39508f, 466.4391f, (byte) 100); // Genesis Treasure Box
	}

	private void spawnKaluvaFragment() {
		spawn(701588, 633.7498f, 557.8822f, 424.99347f, (byte) 6);
	}

	private void spawnKaluvaTreasureBoxes() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
		spawn(701576, 601.2931f, 584.66705f, 422.9955f, (byte) 6); // Genesis Treasure Box
		spawn(701576, 597.2156f, 583.95416f, 423.3474f, (byte) 66); // Genesis Treasure Box
		spawn(701576, 602.9586f, 589.2678f, 422.8296f, (byte) 100); // Genesis Treasure Box
		spawn(701577, 598.82776f, 588.25946f, 422.7739f, (byte) 113); // Abyssal Treasure Box
	}

	private void spawnDayshadeFragment() {
		spawn(701588, 452.89706f, 692.36084f, 433.96838f, (byte) 6);
	}

	private void spawnDayshadeTreasureBoxes() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
		spawn(701576, 408.10938f, 650.9015f, 439.28332f, (byte) 66); // Genesis Treasure Box
		spawn(701576, 402.40375f, 655.55237f, 439.26288f, (byte) 33); // Genesis Treasure Box
		spawn(701576, 406.74445f, 655.5914f, 439.2548f, (byte) 100); // Genesis Treasure Box
		spawn(701578, 404.891f, 650.2943f, 439.2548f, (byte) 130); // Abyssal Treasure Box
	}

	private void spawnYamennesTreasureBoxes(int npcId) {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
		spawn(701576, 326.978f, 729.8414f, 197.7078f, (byte) 16); // Genesis Treasure Box
		spawn(701576, 326.5296f, 735.13324f, 197.6681f, (byte) 66); // Genesis Treasure Box
		spawn(701576, 329.8462f, 738.41095f, 197.7329f, (byte) 3); // Genesis Treasure Box
		spawn(npcId, 330.891f, 733.2943f, 197.6404f, (byte) 113); // Abyssal Treasure Box
	}

	private void deleteSummons() {
		if (instance.getNpcs(219567, 219579, 219580).stream().allMatch(Creature::isDead))
			deleteAliveNpcs(219565, 219566); // Summoned Unstable Orkanimum, Summoned Unstable Lapilima
	}

	private void onFragmentKill() {
		switch (destroyedFragments) {
			case 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_01());
			case 2 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_02());
			case 3 -> {
				deleteAliveNpcs(701589); // Artifact of Protection (Easy Mode)
				spawn(700957, 326.1821f, 766.9640f, 202.1832f, (byte) 100, 79); // Artifact of Protection (Hard Mode)
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_03());
			}
		}
	}
}
