package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Emotion packet
 * 
 * @author SoulKeeper, -Enomine-
 */
public class SM_EMOTION extends AionServerPacket {

	/**
	 * Object id of emotion sender
	 */
	private int senderObjectId;

	/**
	 * Some unknown variable
	 */
	private EmotionType emotionType;

	/**
	 * ID of emotion
	 */
	private int emotion;

	/**
	 * Object id of emotion target
	 */
	private int targetObjectId;

	/**
	 * Temporary Speed..
	 */
	private float speed;
	private int state;
	private int baseAttackSpeed;
	private int currentAttackSpeed;

	/**
	 * Coordinates of player
	 */
	private float x;
	private float y;
	private float z;
	private byte heading;

	public SM_EMOTION(Creature creature, EmotionType emotionType) {
		this(creature, emotionType, 0, 0);
	}

	public SM_EMOTION(Creature creature, EmotionType emotionType, int emotion, int targetObjectId) {
		this.senderObjectId = creature.getObjectId();
		this.emotionType = emotionType;
		this.emotion = emotion;
		this.targetObjectId = targetObjectId;
		this.state = creature.getState();
		Stat2 aSpeed = creature.getGameStats().getAttackSpeed();
		this.baseAttackSpeed = aSpeed.getBase();
		this.currentAttackSpeed = aSpeed.getCurrent();
		this.speed = creature.getGameStats().getMovementSpeedFloat();
	}

	public SM_EMOTION(int Objid, EmotionType emotionType, int state) {
		this.senderObjectId = Objid;
		this.emotionType = emotionType;
		this.state = state;
	}

	public SM_EMOTION(Player player, EmotionType emotionType, int emotion, float x, float y, float z, byte heading, int targetObjectId) {
		this.senderObjectId = player.getObjectId();
		this.emotionType = emotionType;
		this.emotion = emotion;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.targetObjectId = targetObjectId;

		this.state = player.getState();
		this.speed = player.getGameStats().getMovementSpeedFloat();
		Stat2 aSpeed = player.getGameStats().getAttackSpeed();
		this.baseAttackSpeed = aSpeed.getBase();
		this.currentAttackSpeed = aSpeed.getCurrent();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(senderObjectId);
		writeC(emotionType.getTypeId());
		writeH(state);
		writeF(speed);
		switch (emotionType) {
			case LAND_FLYTELEPORT: // fly teleport (land)
			case FLY: // toggle flight mode
			case LAND: // toggle land mode
			case SELECT_TARGET: // select target
			case JUMP:
			case SIT: // sit
			case STAND: // stand
			case ATTACKMODE_IN_MOVE: // toggle attack mode
			case NEUTRALMODE_IN_MOVE: // toggle normal mode
			case WALK: // toggle walk
			case RUN: // toggle run
			case OPEN_PRIVATESHOP: // private shop open
			case CLOSE_PRIVATESHOP: // private shop close
			case POWERSHARD_ON: // powershard on
			case POWERSHARD_OFF: // powershard off
			case ATTACKMODE_IN_STANDING: // toggle attack mode
			case NEUTRALMODE_IN_STANDING: // toggle normal mode
			case START_FEEDING:
			case END_FEEDING:
			case WINDSTREAM_START_BOOST:
			case WINDSTREAM_END_BOOST:
			case WINDSTREAM_END:
			case WINDSTREAM_EXIT:
			case OPEN_DOOR:
			case CLOSE_DOOR:
			case WINDSTREAM_STRAFE:
			case STOP_GLIDE:
			case STOP_FLY:
				break;
			case DIE: // die
			case START_LOOT: // looting start
			case END_LOOT: // looting end
			case START_QUESTLOOT: // looting start (quest)
			case END_QUESTLOOT: // looting end (quest);
				writeD(targetObjectId);
				break;
			case CHAIR_SIT: // sit (chair)
			case CHAIR_UP: // stand (chair)
				writeF(x);
				writeF(y);
				writeF(z);
				writeC(heading);
				break;
			case START_FLYTELEPORT:
				// fly teleport (start)
				writeD(emotion); // teleport Id
				break;
			case WINDSTREAM:
				// entering windstream
				writeD(emotion); // teleport Id
				writeD(targetObjectId); // distance
				break;
			case RIDE:
			case RIDE_END:
				if (targetObjectId != 0) {
					writeD(targetObjectId);// rideId
				}
				writeF(0x3F);// unk
				writeF(0x3F);// unk
				writeF(0x40);// unk
				break;
			case RESURRECT:
				// resurrect
				writeD(0);
				break;
			case EMOTE:
				// emote
				writeD(targetObjectId);
				writeH(emotion);
				writeC(1);
				break;
			case CHANGE_SPEED:
				// emote startloop
				writeH(baseAttackSpeed);
				writeH(currentAttackSpeed);
				writeC(0);// new 4.0
				break;
			default:
				if (targetObjectId != 0) {
					writeD(targetObjectId);
				}
		}
	}
}
