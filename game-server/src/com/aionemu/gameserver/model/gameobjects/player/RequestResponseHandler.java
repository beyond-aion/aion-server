package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * Implemented by handlers of <tt>CM_QUESTION_RESPONSE</tt> responses
 * 
 * @author Ben, Lyahim
 */
public abstract class RequestResponseHandler<T extends Creature> {

	private T requester;

	public RequestResponseHandler(T requester) {
		this.requester = requester;
	}

	/**
	 * Called when a response is received
	 * 
	 * @param requested
	 *          Player whom requested this response
	 * @param responder
	 *          Player whom responded to this request
	 * @param responseCode
	 *          The response the player gave, usually 0 = no 1 = yes
	 */
	public void handle(Player responder, int response) {
		if (response == 0)
			denyRequest(requester, responder);
		else
			acceptRequest(requester, responder);
	}

	/**
	 * Called when the player accepts a request
	 * 
	 * @param requester
	 *          Creature whom requested this response
	 * @param responder
	 *          Player whom responded to this request
	 */
	public abstract void acceptRequest(T requester, Player responder);

	/**
	 * Called when the player denies a request
	 * 
	 * @param requester
	 *          Creature whom requested this response
	 * @param responder
	 *          Player whom responded to this request
	 */
	public void denyRequest(T requester, Player responder) {
	}

}
