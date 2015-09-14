package ai.instance.danuarReliquary;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 *
 * @author Ritsu
 */
@AIName("cursed_queen_modor")
public class CursedQueenModorAI2 extends AggressiveNpcAI2 
{

	private int stage = 0;
	private int spawnCase = 0;

	@Override
	protected void handleSpawned() 
	{
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run() 
			{
				if (!isAlreadyDead())
				{
					sendMsg(1500740);
				}
			}

		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run() 
			{
				if (!isAlreadyDead())
				{
					SkillEngine.getInstance().getSkill(getOwner(), 21171, 1, getOwner()).useSkill();
				}
			}

		}, 3000);
	}

	private void sendMsg(int msg)
	{
		NpcShoutsService.getInstance().sendMsg(getOwner(), msg, getObjectId(), 0, 0);
	}
	
	@Override
	protected void handleAttack(Creature creature) 
	{
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void spawnClone()
	{
		switch(Rnd.get(1, 5)) 
		{
			case 1:
				spawn(284383, 255.5489f, 293.42154f, 253.78925f, (byte) 90);
				spawnCase = 1;
				break;
			case 2:
				spawn(284383, 232.5363f, 263.90112f, 248.65384f, (byte) 114);
				spawnCase = 2;
				break;
			case 3:
				spawn(284383, 240.11194f, 235.08876f, 251.14906f, (byte) 17);
				spawnCase = 3;
				break;
			case 4:
				spawn(284383, 271.23627f, 230.30913f, 250.92981f, (byte) 42);
				spawnCase = 4;
				break;
			case 5:
				spawn(284383, 284.6919f, 262.7201f, 248.75252f, (byte) 63);
				spawnCase = 5;
				break;
		}

		if (spawnCase != 1)
			spawn(284384, 255.5489f, 293.42154f, 253.78925f, (byte) 90);
		if (spawnCase != 2)
			spawn(284384, 232.5363f, 263.90112f, 248.65384f, (byte) 114);
		if (spawnCase != 3)
			spawn(284384, 240.11194f, 235.08876f, 251.14906f, (byte) 17);
		if (spawnCase != 4)
			spawn(284384, 271.23627f, 230.30913f, 250.92981f, (byte) 42);
		if (spawnCase != 5)
			spawn(284384, 284.6919f, 262.7201f, 248.75252f, (byte) 63);
	}

	private void checkPercentage(int hpPercentage) 
	{
		if (hpPercentage <= 75 && stage == 0) 
		{
			stage = 1;
			sendMsg(1500743);
			SkillEngine.getInstance().getSkill(getOwner(), 21165, 1, getOwner()).useSkill();
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run() 
				{
					if (!isAlreadyDead())
					{
						World.getInstance().updatePosition(getOwner(), 255.49063f, 293.35785f, 253.79933f, (byte) 90);
						PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
						spawn(284382, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
						spawn(284380, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
						spawn(284381, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
					}
				}

			}, 2000);
		}

		if (hpPercentage <= 70 && stage == 1)
		{
			stage = 2;
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{

				@Override
				public void run() 
				{
					AI2Actions.useSkill(CursedQueenModorAI2.this, 21165);
				}
			}, 10);
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run() 
				{
					if (!isAlreadyDead())
					{
						World.getInstance().updatePosition(getOwner(), 255.98627f, 259.0136f, 241.73842f, (byte) 90);
						PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
						ThreadPoolManager.getInstance().schedule(new Runnable()
						{
							@Override
							public void run() 
							{
								if (!isAlreadyDead())
								{
									SkillEngine.getInstance().getSkill(getOwner(), 21229, 60, getOwner()).useSkill();
								}
							}

						}, 4000);
					}
				}

			}, 2000);
		}

		if (hpPercentage <= 50 && stage == 2)
		{
			stage = 3;
			SkillEngine.getInstance().getSkill(getOwner(), 21165, 1, getOwner()).useSkill();
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run() 
				{
					if (!isAlreadyDead())
					{
						World.getInstance().updatePosition(getOwner(), 255.49063f, 293.35785f, 253.79933f, (byte) 90);
						PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
						ThreadPoolManager.getInstance().schedule(new Runnable()
						{
							@Override
							public void run() 
							{
								if (!isAlreadyDead())
								{
									Creature creature = getAggroList().getMostHated();
									SkillEngine.getInstance().getSkill(getOwner(), 21179, 1, getOwner()).useSkill();
									spawn(284385, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading());
									ThreadPoolManager.getInstance().schedule(new Runnable()
									{
										@Override
										public void run() 
										{
											if (!isAlreadyDead())
											{
												SkillEngine.getInstance().getSkill(getOwner(), 21165, 1, getOwner()).useSkill();
												ThreadPoolManager.getInstance().schedule(new Runnable()
												{
													@Override
													public void run() 
													{
														if (!isAlreadyDead())
														{
															getOwner().getController().onDelete();
															spawnClone();
														}
													}

												}, 2000);
											}
										}

									}, 1000);
								}
							}

						}, 2000);
					}
				}

			}, 2000);
		}
	}
}