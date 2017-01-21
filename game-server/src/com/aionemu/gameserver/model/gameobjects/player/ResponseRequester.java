package com.aionemu.gameserver.model.gameobjects.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * Manages the asking of and responding to <tt>SM_QUESTION_WINDOW</tt>
 * 
 * @author Ben
 */
public class ResponseRequester {

	private Player player;
	private Map<Integer, RequestResponseHandler<? extends Creature>> map = new ConcurrentHashMap<>();

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
	public boolean putRequest(int messageId, RequestResponseHandler<? extends Creature> handler) {
		return map.putIfAbsent(messageId, handler) == null;
	}

	/**
	 * Responds to the given message ID with the given response Returns success
	 * 
	 * @param messageId
	 * @param response
	 * @return Success
	 */
	public boolean respond(int messageId, int response) {
		RequestResponseHandler<? extends Creature> handler = map.remove(messageId);
		if (handler != null) {
			handler.handle(player, response);
			return true;
		}
		return false;
	}

	/**
	 * Automatically responds 0 to all requests, passing the given player as the responder
	 */
	public void denyAll() {
		for (RequestResponseHandler<? extends Creature> handler : map.values())
			handler.handle(player, 0);
		map.clear();
	}

	/**
	 * Removes the given response handler, so a response by the player won't work anymore
	 */
	public void remove(int messageId) {
		map.remove(messageId);
	}
}
