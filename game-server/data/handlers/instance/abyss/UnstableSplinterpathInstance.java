package instance.abyss;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
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
	private int killedPazuzuWorms = 0;

	public UnstableSplinterpathInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 219554: // (ex 219942) Pazuzu the Life Current
				spawnPazuzuHugeAetherFragment();
				spawnPazuzuGenesisTreasureBoxes();
				spawnPazuzuAbyssalTreasureBox();
				spawnPazuzusTreasureBox();
				break;
			case 219553: // (ex 219941) Kaluva the Fourth Fragment
				spawnKaluvaHugeAetherFragment();
				spawnKaluvaGenesisTreasureBoxes();
				spawnKaluvaAbyssalTreasureBox();
				break;
			case 219551: // (ex 219939) Unstable rukril
			case 219552: // (ex 219940) Unstable ebonsoul
				if (getNpc(npcId == 219552 ? 219551 : 219552) == null) {
					spawnDayshadeAetherFragment();
					spawnDayshadeGenesisTreasureBoxes();
					spawnDayshadeAbyssalTreasureChest();
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
			case 283204: // ex 284022
				Npc ebonsoul = getNpc(219552);
				if (ebonsoul != null && !ebonsoul.isDead()) {
					if (PositionUtil.isInRange(npc, ebonsoul, 5)) {
						ebonsoul.getEffectController().removeEffect(19159);
						deleteAliveNpcs(281907);
						break;
					}
				}
				npc.getController().delete();
				break;
			case 283205: // ex 284023:
				Npc rukril = getNpc(219551);
				if (rukril != null && !rukril.isDead()) {
					if (PositionUtil.isInRange(npc, rukril, 5)) {
						rukril.getEffectController().removeEffect(19266);
						deleteAliveNpcs(281908);
						break;
					}
				}
				npc.getController().delete();
				break;
			case 219563: // ex 219951 unstable Yamennes Painflare
			case 219555: // ex 219943 strengthened Yamennes Blindsight
				spawnYamennesGenesisTreasureBoxes();
				spawnYamennesAbyssalTreasureBox(npcId == 219563 ? 701579 : 701580);
				deleteAliveNpcs(219586); // Ex 219974
				spawn(730317, 328.476f, 762.585f, 197.479f, (byte) 90); // Exit
				for (Player p : instance.getPlayersInside())
					SkillEngine.getInstance().applyEffectDirectly(19283, p, p);
				break;
			case 701588: // HugeAetherFragment
				destroyedFragments++;
				onFragmentKill();
				npc.getController().delete();
				break;

			case 283206: // ex 284024:
				if (++killedPazuzuWorms == 4) {
					killedPazuzuWorms = 0;
					Npc pazuzu = getNpc(219554);
					if (pazuzu != null && !pazuzu.isDead()) {
						pazuzu.getEffectController().removeEffect(19145);
						pazuzu.getEffectController().removeEffect(19291);
					}
				}
				npc.getController().delete();
				break;
			case 219567: // ex 219955
			case 219579: // ex 219967
			case 219580: // ex 219968 Spawn Gate
				removeSummoned();
				npc.getController().delete();
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
			case 700957:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdDH_Wakeup());
				spawn(219563, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
				npc.getController().delete();
				break;
			case 701589:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdD_Wakeup());
				spawn(219555, 329.70886f, 733.8744f, 197.60938f, (byte) 0);
				npc.getController().delete();
				break;
		}
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(player, 8));
		return true;
	}

	private void spawnPazuzuHugeAetherFragment() {
		spawn(701588, 669.576f, 335.135f, 465.895f, (byte) 0);
	}

	private void spawnPazuzuGenesisTreasureBoxes() {
		spawn(701576, 651.53204f, 357.085f, 466.1315f, (byte) 66);
		spawn(701576, 647.00446f, 357.2484f, 465.8960f, (byte) 0);
		spawn(701576, 653.8384f, 360.39508f, 466.4391f, (byte) 100);
	}

	private void spawnPazuzuAbyssalTreasureBox() {
		spawn(701575, 649.24286f, 361.33755f, 466.0427f, (byte) 33);
	}

	private void spawnPazuzusTreasureBox() {
		if (Rnd.chance() < 20) { // 20% chance, not retail
			spawn(700861, 649.243f, 362.338f, 466.0118f, (byte) 0);
		}
	}

	private void spawnKaluvaHugeAetherFragment() {
		spawn(701588, 633.7498f, 557.8822f, 424.99347f, (byte) 6);
	}

	private void spawnKaluvaGenesisTreasureBoxes() {
		spawn(701576, 601.2931f, 584.66705f, 422.9955f, (byte) 6);
		spawn(701576, 597.2156f, 583.95416f, 423.3474f, (byte) 66);
		spawn(701576, 602.9586f, 589.2678f, 422.8296f, (byte) 100);
	}

	private void spawnKaluvaAbyssalTreasureBox() {
		spawn(701577, 598.82776f, 588.25946f, 422.7739f, (byte) 113);
	}

	private void spawnDayshadeAetherFragment() {
		spawn(701588, 452.89706f, 692.36084f, 433.96838f, (byte) 6);
	}

	private void spawnDayshadeGenesisTreasureBoxes() {
		spawn(701576, 408.10938f, 650.9015f, 439.28332f, (byte) 66);
		spawn(701576, 402.40375f, 655.55237f, 439.26288f, (byte) 33);
		spawn(701576, 406.74445f, 655.5914f, 439.2548f, (byte) 100);
	}

	private void spawnDayshadeAbyssalTreasureChest() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
		spawn(701578, 404.891f, 650.2943f, 439.2548f, (byte) 130);
	}

	private void spawnYamennesGenesisTreasureBoxes() {
		spawn(701576, 326.978f, 729.8414f, 197.7078f, (byte) 16);
		spawn(701576, 326.5296f, 735.13324f, 197.6681f, (byte) 66);
		spawn(701576, 329.8462f, 738.41095f, 197.7329f, (byte) 3);
	}

	private void spawnYamennesAbyssalTreasureBox(int npcId) {
		spawn(npcId, 330.891f, 733.2943f, 197.6404f, (byte) 113);
	}

	private void removeSummoned() {
		if (instance.getNpcs(219567, 219579, 219580).stream().allMatch(Creature::isDead)) {
			deleteAliveNpcs(219565, 219566); // Summoned Unstable Orkanimum, Summoned Unstable Lapilima
		}
	}

	private void onFragmentKill() {
		switch (destroyedFragments) {
			case 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_01());
			case 2 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_02());
			case 3 -> {
				deleteAliveNpcs(701589);
				spawn(700957, 326.1821f, 766.9640f, 202.1832f, (byte) 100, 79);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_Artifact_Die_03());
			}
		}
	}
}
