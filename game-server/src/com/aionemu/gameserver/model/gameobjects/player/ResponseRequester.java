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

	private final Player player;
	private final Map<Integer, RequestResponseHandler<? extends Creature>> activeRequests = new ConcurrentHashMap<>();

	public ResponseRequester(Player player) {
		this.player = player;
	}

	/**
	 * Attempts to register a handler for the given messageId
	 * 
	 * @param messageId
	 *          id of the request message
	 * @return true if the request was added, false otherwise
	 */
	public boolean putRequest(int messageId, RequestResponseHandler<? extends Creature> handler) {
		if (handler == null)
			return false;
		return activeRequests.putIfAbsent(messageId, handler) == null;
	}

	/**
	 * Responds to the given messageId with the given responseCode
	 * 
	 * @param messageId
	 *          id of the message to respond to
	 * @return true, if there was a request that handled the response, false otherwise
	 */
	public boolean respond(int messageId, int responseCode) {
		RequestResponseHandler<? extends Creature> handler = activeRequests.remove(messageId);
		if (handler != null) {
			handler.handle(player, responseCode);
			return true;
		}
		return false;
	}

	/**
	 * Automatically responds 0 to all requests, passing the given player as the responder
	 */
	public void denyAll() {
		for (RequestResponseHandler<? extends Creature> handler : activeRequests.values())
			handler.handle(player, 0);
		activeRequests.clear();
	}

	/**
	 * Removes the given response handler, so a response by the player won't work anymore
	 * 
	 * @return boolean indicating if a request was removed
	 */
	public boolean remove(int messageId) {
		return activeRequests.remove(messageId) != null;
	}
}
