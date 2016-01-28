package instance;

import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.LegionDominionReward;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.instanceinfo.LegionDominionScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import javolution.util.FastTable;

/**
 * @author Yeats
 */
@InstanceID(301500000)
public class StonespearRanchInstance extends GeneralInstanceHandler {

	/**
	 * spawnpos 1:
	 * 208.4323f, 264.0647f, 96.223f, (byte) 1
	 * 214.5657f, 281.1983f, 96.1398f, (byte) 99
	 * 230.9258f, 288.3556f, 96.5095f, (byte) 87
	 * 248.5251f, 281.1702f, 96.3423f, (byte) 74
	 * 254.0089f, 264.1608f, 96.1255f, (byte) 60
	 * 248.0067f, 247.8215f, 96.0116f, (byte) 47
	 * 231.1899f, 240.6733f, 96.1348f, (byte) 30
	 * 214.2921f, 247.5521f, 96.267f, (byte) 20
	 */

	// ablauf:
	/**
	 * Round 1:
	 * 1. spawn 855765 pos 1
	 * 2. 8s delay 855765 pos 1, spawn 1x 856305 east
	 * 4. 8s delay 855765
	 * 5. 8s delay 855765, 4s delay 1x 856303 randomPos //despawn 15sek
	 * 6. 8s delay 855765
	 * 7. 8s delay 855765
	 * Round 2:
	 * start minute 1:
	 * 1. spawn 12x 855772 random position
	 * 2. 5s delay spawn 8x 855772 random position
	 * 2. 5s delay spawn 12x 855772 random position
	 * 2. 5s delay spawn 8x 855772 random position
	 * 1:30min 1x 856303 randomPos //despawn 15sek
	 * 1:40min 1x 856303 randomPos //despawn 15sek
	 * Round 3:
	 * start minute 2:
	 */
	private LegionDominionReward reward;
	private Long startTime;
	private List<Future<?>> tasks = new FastTable<>();
	private List<WorldPosition> points = new FastTable<>();
	private Future<?> timer, failTask;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
	super.onInstanceCreate(instance);
	reward = new LegionDominionReward(mapId, instanceId);
	reward.setInstanceScoreType(InstanceScoreType.PREPARING);
	SpawnTemplate temp = SpawnEngine.addNewSingleTimeSpawn(mapId, 855765, 231.14f, 264.399f, 96.23f, (byte) 1); // TODO change npcId
	temp.setStaticId(14);
	SpawnEngine.spawnObject(temp, instanceId);
	addWorldPoints();
	if (timer == null) {
		startTime = System.currentTimeMillis();
		timer = ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
			startTime = System.currentTimeMillis();
			reward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
			sendPacket(0, 0);
			startInstance();
			startFailTask();
		}
		}, 180000); // 3min
	}
	}

	@Override
	public void onEnterInstance(Player player) {
	if (!reward.isRewarded()) {
		sendPacket(0, 0);
	}
	}

	// TODO
	@Override
	public void onDie(Npc npc) {
	switch (npc.getNpcId()) {
		case 855772:
		case 855765:
		case 855767:
		case 855769:
		addPoints(npc, 100);
		break;
		case 855790:
		case 855788:
		case 855789:
		addPoints(npc, 200);
		break;
		case 855815:
		case 855813:
		case 855812:
		addPoints(npc, 300);
		break;
		case 855834:
		case 855835:
		case 855836:
		addPoints(npc, 400);
		break;
		case 856305:
		case 855776:
		addPoints(npc, 12000);
		break;
		case 856303:
		case 855810:
		addPoints(npc, 1500);
		break;
		case 855764:
		addPoints(npc, 500);
		break;
		case 855799:
		addPoints(npc, 21000);
		break;
		case 855787:
		addPoints(npc, 1000);
		break;
		case 855833:
			addPoints(npc, 2000);
			break;
		case 855822:
			addPoints(npc, 30000);
			break;
		case 855843:
			addPoints(npc, 42000);
			break;
	}
	npc.getController().onDelete();
	}

	private void startInstance() {
	startStage1_1();
	}

	private void startStage1_1() {
	spawnAtPointsTask(50, 855765, -1); // 1
	spawnAtPointsTask(8000, 856305, 0);
	spawnAtPointsTask(8000, 855765, -1); // 2
	spawnAtPointsTask(16000, 855765, -1); // 3
	spawnAtPointsTask(24000, 855765, -1); // 4
	spawnAtPointsTask(32000, 855765, -1); // 5
	spawnAtPointsTask(40000, 855765, -1); // 6
	spawnAtPointsTask(48000, 855765, -1); // 7
	rndSpawnTask(36000, 856303, 1, 22, 23);
	startStage1_2();
	}

	private void startStage1_2() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		rndSpawnTask(50, 855772, 3, 8, 16);
		rndSpawnTask(2000, 855772, 3, 7, 18);
		rndSpawnTask(4000, 855772, 6, 7, 22);
		rndSpawnTask(7000, 855772, 4, 7, 18);
		rndSpawnTask(10000, 855772, 4, 7, 18);
		rndSpawnTask(13000, 855772, 6, 7, 18);
		rndSpawnTask(17000, 855772, 8, 7, 18);
		rndSpawnTask(21000, 855772, 6, 7, 22);
		rndSpawnTask(30000, 856303, 1, 22, 23);
		rndSpawnTask(40000, 856303, 1, 22, 23);
		startStage1_3();
		}
	}, 60000));
	}

	private void startStage1_3() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855764, 230.8977f, 285.5198f, 96.42f, (byte) 80), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855764, 211.254f, 264.134f, 96.53f, (byte) 0), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855764, 231.2034f, 243.8273f, 96.37f, (byte) 30), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855764, 251.3068f, 264.307f, 96.31f, (byte) 60), instanceId);

		rndSpawnTask(500, 855765, 6, 8, 12);
		rndSpawnTask(2500, 855765, 6, 7, 11);
		rndSpawnTask(5000, 855765, 6, 12, 22);
		rndSpawnTask(10000, 855765, 8, 12, 22);
		rndSpawnTask(13000, 855765, 8, 7, 11);
		rndSpawnTask(16000, 855765, 6, 7, 11);
		rndSpawnTask(19000, 855765, 8, 17, 22);
		rndSpawnTask(22000, 855765, 8, 17, 22);
		rndSpawnTask(26000, 856303, 2, 22, 23);
		startStage1_4();
		}
	}, 60000));
	}

	private void startStage1_4() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855776, 231.14f, 264.399f, 96.5f, (byte) 10), instanceId); // kaliga 855764
		rndSpawnTask(40000, 856303, 2, 22, 23);
		startStage2_1();
		}
	}, 60000));
	}

	private void startStage2_1() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		spawnAtPointsTask(50, 855767, 0);
		spawnAtPointsTask(50, 855767, 2);
		spawnAtPointsTask(50, 855767, 4);
		spawnAtPointsTask(50, 855767, 6);
		spawnAtPointsTask(250, 855790, 1);
		spawnAtPointsTask(250, 855790, 3);
		spawnAtPointsTask(250, 855790, 5);
		spawnAtPointsTask(250, 855790, 7);
		spawnAtPointsTask(1000, 856305, 1);
		spawnAtPointsTask(8000, 855790, -1);
		spawnAtPointsTask(16000, 855767, 0);
		spawnAtPointsTask(16000, 855767, 2);
		spawnAtPointsTask(16000, 855767, 4);
		spawnAtPointsTask(16000, 855767, 6);
		spawnAtPointsTask(16000, 855790, 1);
		spawnAtPointsTask(16000, 855790, 3);
		spawnAtPointsTask(16000, 855790, 5);
		spawnAtPointsTask(16000, 855790, 7);
		spawnAtPointsTask(24000, 855790, -1);
		rndSpawnTask(20000, 856303, 1, 22, 23);
		spawnAtPointsTask(32000, 855790, -1);
		rndSpawnTask(40000, 856303, 1, 22, 23);
		spawnAtPointsTask(40000, 855767, 0);
		spawnAtPointsTask(40000, 855767, 2);
		spawnAtPointsTask(40000, 855767, 4);
		spawnAtPointsTask(40000, 855767, 6);
		spawnAtPointsTask(40000, 855790, 1);
		spawnAtPointsTask(40000, 855790, 3);
		spawnAtPointsTask(40000, 855790, 5);
		spawnAtPointsTask(40000, 855790, 7);
		spawnAtPointsTask(48000, 855790, -1);
		startStage2_2();
		}
	}, 120000));
	}

	private void startStage2_2() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		rndSpawnTask(50, 855765, 7, 11, 21);
		rndSpawnTask(500, 855765, 7, 11, 21);
		rndSpawnTask(1000, 855788, 10, 11, 21);
		rndSpawnTask(1500, 855788, 10, 11, 21);
		rndSpawnTask(28000, 856303, 2, 22, 23);
		startStage2_3();
		}
	}, 60000));
	}

	private void startStage2_3() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855787, 230.8977f, 285.5198f, 96.42f, (byte) 80), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855787, 211.254f, 264.134f, 96.53f, (byte) 0), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855787, 231.2034f, 243.8273f, 96.37f, (byte) 30), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855787, 251.3068f, 264.307f, 96.31f, (byte) 60), instanceId);
		rndSpawnTask(1, 855789, 7, 15, 20);
		rndSpawnTask(300, 855789, 7, 15, 20);
		rndSpawnTask(800, 855789, 7, 15, 20);
		rndSpawnTask(100, 855769, 7, 11, 21);
		rndSpawnTask(1000, 855769, 7, 11, 21);
		rndSpawnTask(28000, 856303, 2, 22, 23);
		startStage2_4();
		}
	}, 60000));
	}

	private void startStage2_4() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855799, 231.14f, 264.399f, 96.5f, (byte) 10), instanceId); // lanmark
		rndSpawnTask(35000, 856303, 1, 22, 23);
		rndSpawnTask(45000, 856303, 1, 22, 23);
		rndSpawnTask(55000, 856303, 1, 22, 23);
		startStage3_1();
		}
	}, 60000));
	}

	private void startStage3_1() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		spawnAtPointsTask(50, 855769, -1);
		spawnAtPointsTask(1000, 856305, 2);
		spawnAtPointsTask(8000, 855769, -1);
		spawnAtPointsTask(16000, 855769, -1);
		spawnAtPointsTask(24000, 855788, -1);
		spawnAtPointsTask(32000, 855788, -1);
		spawnAtPointsTask(40000, 855788, -1);
		spawnAtPointsTask(48000, 855788, -1);
		rndSpawnTask(36000, 856303, 1, 22, 23);
		startStage3_2();
		}
	}, 120000));
	}

	private void startStage3_2() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		rndSpawnTask(100, 855769, 8, 15, 20);
		spawnAtPointsTask(8000, 855769, -1);
		rndSpawnTask(8000, 855815, 8, 7, 14);
		spawnAtPointsTask(16000, 855788, -1);
		rndSpawnTask(24000, 855815, 8, 15, 20);
		spawnAtPointsTask(32000, 855788, -1);
		rndSpawnTask(40000, 855815, 8, 7, 14);
		spawnAtPointsTask(48000, 855788, -1);
		rndSpawnTask(36000, 856303, 1, 22, 23);
		rndSpawnTask(44000, 855769, 8, 15, 20);
		rndSpawnTask(40000, 856303, 1, 22, 23);
		startStage3_3();
		}
	}, 60000));
	}

	private void startStage3_3() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855810, 230.8977f, 285.5198f, 96.42f, (byte) 80), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855810, 211.254f, 264.134f, 96.53f, (byte) 0), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855810, 231.2034f, 243.8273f, 96.37f, (byte) 30), instanceId);
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855810, 251.3068f, 264.307f, 96.31f, (byte) 60), instanceId);

		rndSpawnTask(100, 855815, 5, 15, 20);
		rndSpawnTask(100, 855813, 5, 7, 14);
		rndSpawnTask(6000, 855815, 5, 7, 14);
		rndSpawnTask(6000, 855813, 5, 15, 20);
		rndSpawnTask(12000, 855815, 5, 15, 20);
		rndSpawnTask(12000, 855813, 5, 7, 14);
		rndSpawnTask(18000, 855815, 5, 7, 14);
		rndSpawnTask(18000, 855813, 5, 15, 20);
		rndSpawnTask(24000, 855815, 5, 15, 20);
		rndSpawnTask(24000, 855813, 5, 7, 14);
		rndSpawnTask(25000, 856303, 1, 22, 23);
		startStage3_4();
		}
	}, 60000));
	}

	private void startStage3_4() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		rndSpawnTask(1000, 855790, 10, 15, 20);
		spawnAtPointsTask(7000, 855790, -1);
		rndSpawnTask(13000, 855790, 10, 15, 20);
		spawnAtPointsTask(19000, 855790, -1);
		rndSpawnTask(25000, 855790, 10, 15, 20);
		spawnAtPointsTask(31000, 855790, -1);
		rndSpawnTask(37000, 855790, 10, 15, 20);
		spawnAtPointsTask(43000, 855790, -1);
		rndSpawnTask(20000, 856303, 1, 22, 23);
		rndSpawnTask(45000, 856303, 1, 22, 23);
		startStage3_5();
		}
	}, 60000));
	}

	private void startStage3_5() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855822, 231.14f, 264.399f, 96.5f, (byte) 10), instanceId); // stormwing
		rndSpawnTask(25000, 856303, 1, 22, 23);
		rndSpawnTask(40000, 856303, 1, 22, 23);
		rndSpawnTask(55000, 856303, 1, 22, 23);
		startStage4_1();
		}
	}, 60000));
	}

	private void startStage4_1() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		rndSpawnTask(1000, 855813, 15, 6, 9);
		rndSpawnTask(10000, 855834, 15, 6, 11);
		rndSpawnTask(19000, 855812, 16, 7, 14);
		rndSpawnTask(28000, 855834, 16, 8, 17);
		rndSpawnTask(37000, 855836, 17, 9, 20);
		rndSpawnTask(46000, 855835, 17, 9, 20);
		rndSpawnTask(25000, 856303, 1, 22, 23);
		rndSpawnTask(40000, 856303, 1, 22, 23);
		startStage4_2();
		}
	}, 60000));
	}

	private void startStage4_2() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		spawnAtPointsTask(1000, 855836, 0);
		spawnAtPointsTask(2000, 855835, 1);
		spawnAtPointsTask(3000, 855836, 2);
		spawnAtPointsTask(4000, 855835, 3);
		spawnAtPointsTask(5000, 855836, 4);
		spawnAtPointsTask(6000, 855835, 5);
		spawnAtPointsTask(7000, 855836, 6);
		spawnAtPointsTask(8000, 855835, 7);

		spawnAtPointsTask(9000, 855836, 0);
		spawnAtPointsTask(10000, 855835, 1);
		spawnAtPointsTask(11000, 855836, 2);
		spawnAtPointsTask(12000, 855835, 3);
		spawnAtPointsTask(13000, 855836, 4);
		spawnAtPointsTask(14000, 855835, 5);
		spawnAtPointsTask(15000, 855836, 6);
		spawnAtPointsTask(16000, 855835, 7);

		spawnAtPointsTask(17000, 855836, 0);
		spawnAtPointsTask(18000, 855835, 1);
		spawnAtPointsTask(19000, 855836, 2);
		spawnAtPointsTask(20000, 855835, 3);
		spawnAtPointsTask(21000, 855836, 4);
		spawnAtPointsTask(22000, 855835, 5);
		spawnAtPointsTask(23000, 855836, 6);
		spawnAtPointsTask(24000, 856303, 7);
		
		spawnAtPointsTask(25000, 855836, 0);
		spawnAtPointsTask(26000, 855835, 1);
		spawnAtPointsTask(27000, 855836, 2);
		spawnAtPointsTask(28000, 855835, 3);
		spawnAtPointsTask(29000, 855836, 4);
		spawnAtPointsTask(30000, 855835, 5);
		spawnAtPointsTask(31000, 855836, 6);
		spawnAtPointsTask(32000, 855835, 7);

		rndSpawnTask(25000, 855769, 15, 8, 20);
		rndSpawnTask(45000, 855772, 15, 8, 20);
		rndSpawnTask(40000, 856303, 1, 22, 23);
		startStage4_3();
		}
	}, 60000));
	}

	private void startStage4_3() {
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		spawnAtPointsTask(1000, 855836, 0);
		spawnAtPointsTask(1000, 855835, 1);
		spawnAtPointsTask(2000, 855836, 2);
		spawnAtPointsTask(2000, 855835, 3);
		spawnAtPointsTask(3000, 855836, 4);
		spawnAtPointsTask(3000, 855835, 5);
		spawnAtPointsTask(4000, 855836, 6);
		spawnAtPointsTask(4000, 855835, 7);

		spawnAtPointsTask(5000, 855836, 0);
		spawnAtPointsTask(50000, 855835, 1);
		spawnAtPointsTask(6000, 855836, 2);
		spawnAtPointsTask(6000, 855835, 3);
		spawnAtPointsTask(7000, 855836, 4);
		spawnAtPointsTask(7000, 855835, 5);
		spawnAtPointsTask(8000, 855836, 6);
		spawnAtPointsTask(8000, 855835, 7);

		spawnAtPointsTask(9000, 855836, 0);
		spawnAtPointsTask(9000, 855835, 1);
		spawnAtPointsTask(10000, 855836, 2);
		spawnAtPointsTask(10000, 855835, 3);
		spawnAtPointsTask(11000, 855836, 4);
		spawnAtPointsTask(11000, 855835, 5);
		spawnAtPointsTask(12000, 855836, 6);
		spawnAtPointsTask(12000, 855835, 7);

		spawnAtPointsTask(13000, 855836, 0);
		spawnAtPointsTask(13000, 855835, 1);
		spawnAtPointsTask(14000, 855836, 2);
		spawnAtPointsTask(14000, 855835, 3);
		spawnAtPointsTask(15000, 855836, 4);
		spawnAtPointsTask(15000, 855835, 5);
		spawnAtPointsTask(16000, 855836, 6);
		spawnAtPointsTask(16000, 855835, 7);

		rndSpawnTask(20000, 855788, 15, 8, 20);
		rndSpawnTask(28000, 856303, 1, 22, 23);
		rndSpawnTask(45000, 855788, 15, 6, 20);
		rndSpawnTask(50000, 856303, 1, 22, 23);
		startStage4_4();
		}
	}, 60000));
	}
	
	private void startStage4_4() {
		tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855833, 230.8977f, 285.5198f, 96.42f, (byte) 80), instanceId);
				SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855833, 211.254f, 264.134f, 96.53f, (byte) 0), instanceId);
				SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855833, 231.2034f, 243.8273f, 96.37f, (byte) 30), instanceId);
				SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855833, 251.3068f, 264.307f, 96.31f, (byte) 60), instanceId);
				
				rndSpawnTask(1000, 855834, 7, 7, 20);
				rndSpawnTask(2000, 855835, 7, 7, 20);
				rndSpawnTask(3000, 855836, 7, 7, 20);
				
				rndSpawnTask(8000, 855834, 10, 7, 20);
				rndSpawnTask(12000, 855835, 10, 7, 20);
				rndSpawnTask(16000, 855836, 10, 7, 20);
				
				rndSpawnTask(20000, 855834, 10, 7, 20);
				rndSpawnTask(24000, 855835, 10, 7, 20);
				rndSpawnTask(28000, 855836, 10, 7, 20);
				
				rndSpawnTask(32000, 855834, 10, 7, 20);
				rndSpawnTask(36000, 855835, 10, 7, 20);
				rndSpawnTask(40000, 855836, 10, 7, 20);
				
				rndSpawnTask(48000, 855834, 7, 7, 20);
				rndSpawnTask(48000, 855835, 7, 7, 20);
				rndSpawnTask(48000, 855836, 7, 7, 20);
				
				rndSpawnTask(20000, 856303, 1, 22, 23);
				rndSpawnTask(45000, 856303, 1, 22, 23);
				startStage4_5();
			}
		}, 60000));
	}
	
	private void startStage4_5() {
		tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(mapId, 855843, 231.14f, 264.399f, 96.5f, (byte) 10), instanceId); // general of illusion
				rndSpawnTask(20000, 856303, 1, 22, 23);
				rndSpawnTask(35000, 856303, 1, 22, 23);
				rndSpawnTask(55000, 856303, 1, 22, 23);
				rndSpawnTask(70000, 856303, 1, 22, 23);
				rndSpawnTask(100000, 856303, 1, 22, 23);
			}
		}, 60000));
	}

	private void startFailTask() {
	failTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		stopInstance();
		}
	}, 1800000); // 30min
	}

	private void stopInstance() {

	}

	public void addPoints(Npc npc, int points) {
	if (reward.isStartProgress()) {
		reward.addPoints(points);
		sendPacket(npc.getObjectTemplate().getNameId(), points);
	}
	}

	private void sendPacket(final int nameId, final int point) {
	for (Player p : instance.getPlayersInside()) {
		if (p != null && p.isOnline()) {
		if (nameId != 0) {
			PacketSendUtility.sendPacket(p, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), point));
		}
		PacketSendUtility.sendPacket(p, new SM_INSTANCE_SCORE(new LegionDominionScoreInfo(reward), reward, getTime()));
		}
	}
	}

	private int getTime() {
	long result = System.currentTimeMillis() - startTime;
	if (reward.isPreparing()) {
		return (int) (180000 - result);
	} else if (result < 1800000) {
		return (int) (1800000 - result);
	}
	return 0;
	}

	private void spawnAtPointsTask(int delay, int npcId, int index) {
	if (!reward.isStartProgress()) {
		return;
	}
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		if (reward.isStartProgress()) {
			spawn(npcId, index);
		}
		}
	}, delay));
	}

	private void spawn(int npcId, int index) {
	if (index >= 0) {
		WorldPosition point = points.get(index);
		if (point != null) {
		SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, point.getX(), point.getY(), point.getZ(), point.getHeading());
		if (template != null) {
			SpawnEngine.spawnObject(template, instanceId);
		}
		}
	} else {
		for (WorldPosition point : points) {
		if (!reward.isStartProgress()) {
			break;
		}
		SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, point.getX(), point.getY(), point.getZ(), point.getHeading());
		if (template != null) {
			SpawnEngine.spawnObject(template, instanceId);
		}
		}
	}
	}

	private void rndSpawnTask(int delay, int npcId, int amount, int minRange, int maxRange) {
	if (!reward.isStartProgress()) {
		return;
	}
	tasks.add(ThreadPoolManager.getInstance().schedule(new Runnable() {

		@Override
		public void run() {
		if (reward.isStartProgress()) {
			rndSpawnInRange(npcId, amount, minRange, maxRange);
		}
		}
	}, delay));
	}

	private void rndSpawnInRange(int npcId, int amount, int minRange, int maxRange) {
	for (int i = 0; i < amount; i++) {
		if (!reward.isStartProgress()) {
		break;
		}
		SpawnTemplate template = getRndSpawnInRangeTemplate(npcId, minRange, maxRange);
		if (template != null) {
		SpawnEngine.spawnObject(template, instanceId);
		}
	}
	}

	private SpawnTemplate getRndSpawnInRangeTemplate(int npcId, int minRange, int maxRange) {
	for (int i = 0; i < 10; i++) { // 10 tries should be enough to find a spot. if not fuck it. I'm not going to implement a while loop
		if (!reward.isStartProgress()) {
		break;
		}
		float direction = (Rnd.get(0, 199) / 100f);
		int range = Rnd.get(minRange, maxRange);
		float x = 231.14f + (float) (Math.cos(Math.PI * direction) * range);
		float y = 264.399f + (float) (Math.sin(Math.PI * direction) * range);
		if (isValidPoint(x, y)) {
		return SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, 96.51f, (byte) 50);
		}
	}
	return null;
	}

	private boolean isValidPoint(float x, float y) {
	if ((MathUtil.getDistance(x, y, 211.254f, 264.134f) >= 2.5) && (MathUtil.getDistance(x, y, 230.8977f, 285.5198f) >= 2.5)
		&& (MathUtil.getDistance(x, y, 251.3068f, 264.307f) >= 2.5) && (MathUtil.getDistance(x, y, 231.2034f, 243.8273f) >= 2.5)) {
		return true;
	}
	return false;
	}

	private void addWorldPoints() {
	points.add(new WorldPosition(mapId, 208.4323f, 264.0647f, 96.223f, (byte) 1));
	points.add(new WorldPosition(mapId, 214.5657f, 281.1983f, 96.1398f, (byte) 99));
	points.add(new WorldPosition(mapId, 230.9258f, 288.3556f, 96.5095f, (byte) 87));
	points.add(new WorldPosition(mapId, 248.5251f, 281.1702f, 96.3423f, (byte) 74));
	points.add(new WorldPosition(mapId, 254.0089f, 264.1608f, 96.1255f, (byte) 60));
	points.add(new WorldPosition(mapId, 248.0067f, 247.8215f, 96.0116f, (byte) 47));
	points.add(new WorldPosition(mapId, 231.1899f, 240.6733f, 96.1348f, (byte) 30));
	points.add(new WorldPosition(mapId, 214.2921f, 247.5521f, 96.267f, (byte) 20));
	}
}
