package ai.instance.eternalBastion;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller, Estrayl
 */
@AIName("eternal_bastion_assault_machine")
public class EternalBastionAssaultMachineAI extends EternalBastionConstructAI {

	public EternalBastionAssaultMachineAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		spawnSkillArea();
		ThreadPoolManager.getInstance().schedule(this::spawnSummons, 3, TimeUnit.SECONDS);
	}

	private void spawnSkillArea() {
		int skillAreaNpcId = switch (getNpcId()) {
			case 231140, 231156, 231157, 231158, 231159, 231160, 231162, 231167 -> 284686;
			case 231141, 231163, 231164, 231165 -> 284699;
			default -> 0;
		};
		if (skillAreaNpcId != 0)
			spawn(skillAreaNpcId, getPosition().getX(), getPosition().getY(), getPosition().getZ(), (byte) 0);
	}

	private void spawnSummons() {
		switch (getNpcId()) {
			case 231140 -> {
				spawnWithWalker(231106, 633.457f, 245.792f, 238.075f, (byte) 33, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD01");
				spawnWithWalker(231108, 635.457f, 247.792f, 238.075f, (byte) 33, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD01");
				spawnWithWalker(231108, 631.457f, 247.792f, 238.075f, (byte) 33, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD01");
			}
			case 231156 -> {
				spawnWithWalker(231106, 642.871f, 343.420f, 238.075f, (byte) 20, "NPCPathIDLDF5b_TD_Z1_S5_POD01");
				spawnWithWalker(231108, 644.871f, 345.420f, 238.075f, (byte) 20, "NPCPathIDLDF5b_TD_Z1_S5_POD01");
				spawnWithWalker(231108, 640.871f, 345.420f, 238.075f, (byte) 20, "NPCPathIDLDF5b_TD_Z1_S5_POD01");
			}
			case 231157 -> {
				spawnWithWalker(231106, 776.242f, 326.041f, 253.434f, (byte) 40, "NPCPathIDLDF5b_TD_Z4_POD02");
				spawnWithWalker(231108, 778.242f, 328.041f, 253.434f, (byte) 40, "NPCPathIDLDF5b_TD_Z4_POD02");
				spawnWithWalker(231108, 774.242f, 328.041f, 253.434f, (byte) 40, "NPCPathIDLDF5b_TD_Z4_POD02");
			}
			case 231158 -> {
				spawnWithWalker(231106, 765.481f, 393.614f, 243.354f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD3");
				spawnWithWalker(231108, 767.481f, 395.614f, 243.354f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD3");
				spawnWithWalker(231108, 763.481f, 395.614f, 243.354f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD3");
			}
			case 231141 -> {
				spawnWithWalker(231105, 667.631f, 297.565f, 225.700f, (byte) 20, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD2");
				spawnWithWalker(231107, 669.631f, 299.565f, 225.700f, (byte) 20, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD2");
				spawnWithWalker(231107, 665.631f, 299.565f, 225.700f, (byte) 20, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD2");
			}
			case 231163 -> {
				spawnWithWalker(231105, 731.089f, 365.461f, 230.941f, (byte) 7, "NPCPathIDLDF5b_TD_Z1_S5_POD02");
				spawnWithWalker(231107, 731.089f, 365.461f, 230.941f, (byte) 7, "NPCPathIDLDF5b_TD_Z1_S5_POD02");
				spawnWithWalker(231107, 731.089f, 365.461f, 230.941f, (byte) 7, "NPCPathIDLDF5b_TD_Z1_S5_POD02");
			}
			case 231159 -> {
				spawnWithWalker(231106, 699.760f, 302.938f, 249.303f, (byte) 100, "NPCPathIDLDF5b_TD_Z4_POD01");
				spawnWithWalker(231108, 701.760f, 304.938f, 249.303f, (byte) 100, "NPCPathIDLDF5b_TD_Z4_POD01");
				spawnWithWalker(231108, 697.760f, 304.938f, 249.303f, (byte) 100, "NPCPathIDLDF5b_TD_Z4_POD01");
			}
			case 231164, 231165 -> {
				spawnWithWalker(231106, 724.927f, 359.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
				spawnWithWalker(231108, 726.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
				spawnWithWalker(231108, 722.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			}
			case 231160 -> {
				spawnWithWalker(230749, 705.203f, 261.673f, 252.434f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z2_POD01");
				spawnWithWalker(230745, 708.203f, 264.673f, 252.434f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z2_POD01");
				spawnWithWalker(230744, 702.203f, 264.673f, 252.434f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z2_POD01");
			}
			case 231162 -> {
				spawnWithWalker(231106, 747.057f, 296.569f, 233.874f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S72");
				spawnWithWalker(231108, 749.057f, 298.569f, 233.771f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S72");
				spawnWithWalker(231108, 745.057f, 298.569f, 233.889f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S72");
			}
			case 231167 -> {
				spawnWithWalker(231105, 738.529f, 294.467f, 233.889f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S42");
				spawnWithWalker(231107, 740.529f, 296.467f, 233.889f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S42");
				spawnWithWalker(231107, 736.529f, 296.467f, 233.752f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S42");
			}
			// Siege Towers
			case 231143 -> {
				spawnWithWalker(231107, 623.235f, 263.392f, 238.484f, (byte) 3, "NPCPathIDLDF5b_TD_Z1_S4_T1");
				spawnWithWalker(231105, 625.235f, 265.392f, 238.484f, (byte) 3, "NPCPathIDLDF5b_TD_Z1_S4_T1");
				spawnWithWalker(231105, 621.235f, 265.392f, 238.484f, (byte) 3, "NPCPathIDLDF5b_TD_Z1_S4_T1");
			}
			case 231152 -> {
				spawnWithWalker(231107, 621.920f, 298.179f, 238.075f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T2");
				spawnWithWalker(231105, 623.920f, 300.179f, 238.075f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T2");
				spawnWithWalker(231105, 619.920f, 300.179f, 238.075f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T2");
			}
			case 231153 -> {
				spawnWithWalker(231107, 644.089f, 351.522f, 239.764f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T3");
				spawnWithWalker(231105, 646.089f, 353.522f, 241.151f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T3");
				spawnWithWalker(231105, 642.089f, 353.522f, 239.809f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T3");
			}
			case 231154 -> {
				spawnWithWalker(231107, 664.091f, 394.303f, 240.223f, (byte) 83, "NPCPathIDLDF5b_TD_Z1_S4_T4");
				spawnWithWalker(231105, 666.091f, 396.303f, 240.223f, (byte) 83, "NPCPathIDLDF5b_TD_Z1_S4_T4");
				spawnWithWalker(231105, 662.091f, 396.303f, 240.223f, (byte) 83, "NPCPathIDLDF5b_TD_Z1_S4_T4");
			}
			case 231155 -> {
				spawnWithWalker(231107, 692.867f, 396.708f, 241.594f, (byte) 85, "NPCPathIDLDF5b_TD_Z1_S4_T5");
				spawnWithWalker(231105, 694.867f, 398.708f, 242.018f, (byte) 85, "NPCPathIDLDF5b_TD_Z1_S4_T5");
				spawnWithWalker(231105, 690.867f, 398.708f, 241.594f, (byte) 85, "NPCPathIDLDF5b_TD_Z1_S4_T5");
			}
		}
	}

	private void spawnWithWalker(int npcId, float x, float y, float z, byte h, final String walker) {
		spawn(npcId, x, y, z, h).getSpawn().setWalkerId(walker);
	}

}
