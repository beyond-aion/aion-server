package instance.dredgion;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300440000)
public class TerathDredgionInstance extends DredgionInstance {

	public TerathDredgionInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (isInstanceStarted.compareAndSet(false, true)) {
			sp(730558, 415.034f, 174.004f, 433.940f, (byte) 0, 34, 720000);
			sp(730559, 572.038f, 185.252f, 433.940f, (byte) 0, 10, 720000);
			sendMsgByRace(1401424, Race.PC_ALL, 720000);
			if (Rnd.chance() < 21) {
				sp(233372, 476.63f, 312.16f, 402.89807f, (byte) 97, 720000, "5540A84BAD08498B96C315281F6418D0BD825175");
			}
			if (Rnd.chance() < 21) {
				sp(233373, 485.403f, 596.602f, 390.944f, (byte) 90, 720000);
			}
			if (Rnd.chance() < 21) {
				sp(233379, 486.26382f, 906.011f, 405.24463f, (byte) 90, 720000);
			}
			if (Rnd.chance() < 51) {
				switch (Rnd.nextInt(2)) { // Supervisor Chitan
					case 0:
						spawn(233362, 421.89111f, 285.20471f, 409.7311f, (byte) 80);
						break;
					default:
						spawn(233362, 551.407f, 289.058f, 409.7311f, (byte) 80);
						break;
				}
			}
			int spawnTime = Rnd.get(10, 15) * 60 * 1000 + 120000;
			sendMsgByRace(1401417, Race.PC_ALL, spawnTime);
			sp(233377, 484.664f, 314.207f, 403.715f, (byte) 30, spawnTime); // Enforcer Udara
			startInstanceTask();
		}
		super.onEnterInstance(player);
	}

	private void onDieSurkana(Npc npc, Player mostPlayerDamage, int points) {
		Race race = mostPlayerDamage.getRace();
		captureRoom(race, npc.getNpcId() + 14 - 701454);
		for (Player player : instance.getPlayersInside()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_ROOM_DESTROYED(race.getL10n(), npc.getObjectTemplate().getL10n()));
		}
		if (killedSurkanas.incrementAndGet() == 5) {
			spawn(233371, 485.423f, 808.826f, 416.868f, (byte) 30);
			sendMsgByRace(1401416, Race.PC_ALL, 0);
		}
		getPlayerReward(mostPlayerDamage).incrementCapturedZones();
		updateScore(mostPlayerDamage, npc, points, false);
		npc.getController().delete();
	}

	@Override
	public void onDie(Npc npc) {
		Player mostPlayerDamage = npc.getAggroList().getMostPlayerDamage();
		if (mostPlayerDamage == null || instanceScore.getInstanceProgressionType() != InstanceProgressionType.START_PROGRESS) {
			return;
		}
		Race race = mostPlayerDamage.getRace();
		switch (npc.getNpcId()) {
			case 701441:
			case 701442:
				onDieSurkana(npc, mostPlayerDamage, 400);
				return;
			case 701443:
			case 701451:
			case 701452:
			case 701453:
			case 701454:
				onDieSurkana(npc, mostPlayerDamage, 700);
				return;
			case 701448:
			case 701449:
				onDieSurkana(npc, mostPlayerDamage, 800);
				return;
			case 701450:
				onDieSurkana(npc, mostPlayerDamage, 900);
				return;
			case 701444:
			case 701445:
				onDieSurkana(npc, mostPlayerDamage, 1000);
				return;
			case 701446:
			case 701447:
				onDieSurkana(npc, mostPlayerDamage, 1100);
				return;
			case 730572:
				spawn(730566, 446.729f, 493.224f, 395.938f, (byte) 0, 12);
				npc.getController().delete();
				return;
			case 730573:
				spawn(730567, 520.404f, 493.261f, 395.938f, (byte) 0, 133);
				npc.getController().delete();
				return;
			case 730570:
				sendMsgByRace(1401418, race, 0);
				spawn(730560, 396.979f, 184.392f, 433.940f, (byte) 0, 42);
				break;
			case 730571:
				sendMsgByRace(1401418, race, 0);
				spawn(730561, 554.64f, 173.535f, 433.940f, (byte) 0, 9);
				break;
			case 233378: // Master at Arms Vandukar
				sendMsgByRace(1401419, Race.PC_ALL, 0);
				if (race == Race.ASMODIANS) {
					spawn(730563, 496.178f, 761.770f, 390.805f, (byte) 0, 186);
				} else {
					spawn(730562, 473.759f, 761.864f, 390.805f, (byte) 0, 33);
				}
				return;
			case 233377: // Enforcer Udara
				updateScore(mostPlayerDamage, npc, 1000, false);
				if (Rnd.nextBoolean()) {
					spawn(701455, 484.500f, 495.700f, 397.425f, (byte) 33);
					sendMsgByRace(1401421, Race.PC_ALL, 0);
				}
				return;
			case 233370:
				updateScore(mostPlayerDamage, npc, 500, false);
				return;
			case 233371: // Captain Anusa
				if (!instanceScore.isRewarded()) {
					updateScore(mostPlayerDamage, npc, 1000, false);
					stopInstance(instanceScore.getRaceWithHighestPoints());
				}
				return;
			case 701439:
				updateScore(mostPlayerDamage, npc, 100, false);
				npc.getController().delete();
				return;
		}
		super.onDie(npc);
	}

	@Override
	public void doReward(Player player, PvpInstancePlayerReward reward, Race winningRace) {
		if (reward.getRace() == winningRace) {
			reward.setReward1(186000242, 1, 0); // CUSTOM: Ceramium Medal
			if (Rnd.chance() < 20)
				reward.setReward2(188950017, 1, 0); // CUSTOM: Special Courier Pass (Abyss Eternal/Lv. 61-65)
		} else {
			reward.setReward1(186000147, 1, 0); // CUSTOM: Mithril Medal
		}
		super.doReward(player, reward, winningRace);
	}

	@Override
	protected void openFirstDoors() {
		instance.setDoorState(4, true);
		instance.setDoorState(173, true);
	}

}
