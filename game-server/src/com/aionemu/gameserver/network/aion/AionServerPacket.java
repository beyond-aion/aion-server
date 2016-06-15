package com.aionemu.gameserver.network.aion;

import java.nio.ByteBuffer;

import com.aionemu.commons.network.packet.BaseServerPacket;
import com.aionemu.gameserver.network.Crypt;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Base class for every GS -> Aion Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class AionServerPacket extends BaseServerPacket {

	/**
	 * Constructs new server packet
	 */
	protected AionServerPacket() {
		super();
		setOpcode(ServerPacketsOpcodes.getOpcode(getClass()));
	}

	/**
	 * Write packet opcodec and two additional bytes
	 * 
	 * @param buf
	 * @param value
	 */
	private final void writeOP(int value) {
		/** obfuscate packet id */
		int op = Crypt.encodeOpcodec(value);
		buf.putShort((short) (op));
		/** put static server packet code */
		buf.put(Crypt.staticServerPacketCode);

		/** for checksum? */
		buf.putShort((short) (~op));
	}

	public final void write(AionConnection con) {
		write(con, buf);
	}

	/**
	 * Write and encrypt this packet data for given connection, to given buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	public final void write(AionConnection con, ByteBuffer buffer) {
		if (con.getState().equals(AionConnection.State.IN_GAME) && con.getActivePlayer().getPlayerAccount().getMembership() == 10) {
			if (!this.getPacketName().equals("SM_MESSAGE")) {
				PacketSendUtility.sendMessage(con.getActivePlayer(),
					"0x" + Integer.toHexString(this.getOpcode()).toUpperCase() + " : " + this.getPacketName());
			}
		}

		this.setBuf(buffer);
		buf.putShort((short) 0);
		writeOP(getOpcode());
		writeImpl(con);
		buf.flip();
		buf.putShort((short) buf.limit());
		ByteBuffer b = buf.slice();
		buf.position(0);
		con.encrypt(b);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	protected void writeImpl(AionConnection con) {

	}

	public final ByteBuffer getBuf() {
		return this.buf;
	}

	/**
	 * Write String to buffer
	 * 
	 * @param text
	 * @param size
	 */
	protected final void writeS(String text, int size) {
		if (text == null) {
			buf.put(new byte[size]);
		} else {
			final int len = text.length();
			for (int i = 0; i < len; i++)
				buf.putChar(text.charAt(i));
			buf.put(new byte[size - (len * 2)]);
		}
	}

	/**
	 * Writes a nameId (e.g. of items or skills) to show its name ingame. It's basically just writeS("$" + nameId) but nameId must not exceed 4 bytes
	 * (in UTF-8 string form it would be 8 bytes long and the bytes would be inverted). That's why we send it byte-wise.
	 * You could also send writeS({@link ChatUtil#nameId(nameId)}) but it would only create unnecessary overhead.
	 * 
	 * @param nameId
	 */
	protected final void writeNameId(int nameId) {
		if (nameId > 0) {
			writeH(0x24); // "$"
			writeD(nameId);
		}
		writeH(0x00); // string end marker
	}

	/**
	 * Writes dye information (dye status + 3 byte RGB value) to the buffer.
	 * 
	 * @param rgb
	 *          - may be null
	 */
	protected final void writeDyeInfo(Integer rgb) {
		if (rgb == null) {
			writeB(new byte[4]);
		} else {
			writeC(1); // dye status (1 = dyed, 0 = not dyed)
			writeC((rgb & 0xFF0000) >> 16); // r
			writeC((rgb & 0xFF00) >> 8); // g
			writeC(rgb & 0xFF); // b
		}
	}
}
