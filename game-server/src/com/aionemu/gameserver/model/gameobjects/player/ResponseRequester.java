package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the asking of and responding to <tt>SM_QUESTION_WINDOW</tt>
 * 
 * @author Ben
 */
public class ResponseRequester {

	private Player player;
	private Map<Integer, RequestResponseHandler> map = new HashMap<>();

	public ResponseRequester(Player player) {
		this.player = player;
	}

	/**
	 * Adds this handler to this messageID, returns false if there already exists one
	 * 
	 * @param messageId
	 *          ID of the request message
	 * @return true or false
	 */
	public synchronized boolean putRequest(int messageId, RequestResponseHandler handler) {
		return map.putIfAbsent(messageId, handler) == null;
	}

	/**
	 * Responds to the given message ID with the given response Returns success
	 * 
	 * @param messageId
	 * @param response
	 * @return Success
	 */
	public synchronized boolean respond(int messageId, int response) {
		RequestResponseHandler handler = map.remove(messageId);
		if (handler != null) {
			handler.handle(player, response);
			return true;
		}
		return false;
	}

	/**
	 * Automatically responds 0 to all requests, passing the given player as the responder
	 */
	public synchronized void denyAll() {
		for (RequestResponseHandler handler : map.values())
			handler.handle(player, 0);
		map.clear();
	}
}
