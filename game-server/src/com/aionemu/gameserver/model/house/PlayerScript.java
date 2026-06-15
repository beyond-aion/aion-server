package com.aionemu.gameserver.model.house;

import java.nio.charset.StandardCharsets;

import com.aionemu.gameserver.utils.xml.CompressUtil;

/**
 * @author Rolandas, Neon, Sykra
 */
public record PlayerScript(int id, byte[] compressedBytes, int uncompressedSize) {

	public static final PlayerScript LUA_SANDBOX_FIX; // mitigates https://appsec.space/posts/aion-housing-exploit/

	static {
		byte[] script = """
		<?xml version="1.0" encoding="UTF-16" ?>
		<lboxes>
			<lbox>
				<id>101</id>
				<name><![CDATA[Lua Fix]]></name>
				<desc><![CDATA[Secures the Lua environment against malicious actors]]></desc>
				<script><![CDATA[
		_G.debug = nil
		_G.dofile = nil
		_G.io = nil
		_G.load = nil
		_G.loadfile = nil
		_G.loadstring = nil
		_G.package = nil
		_G.require = nil
		]]></script>
				<icon>1</icon>
			</lbox>
		</lboxes>
		""".getBytes(StandardCharsets.UTF_16LE);
		LUA_SANDBOX_FIX = new PlayerScript(0, CompressUtil.compress(script), script.length);
	}

	public boolean hasData() {
		return compressedBytes != null && compressedBytes.length > 0;
	}

}
