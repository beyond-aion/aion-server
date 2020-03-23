/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.aionemu.gameserver.geoEngine.collision;

import java.util.ArrayList;
import java.util.Iterator;

public class CollisionResults implements Iterable<CollisionResult> {

	private static final double SLOPING_SURFACE_ANGLE_RAD = Math.toRadians(45); // players can't walk or stand on surfaces with >= 45° elevation angle
	private final ArrayList<CollisionResult> results = new ArrayList<>();
	private boolean sorted = true;
	private final byte intentions;
	private final int instanceId;
	private final boolean onlyFirst;
	private final IgnoreProperties ignoreProperties;
	private boolean canSeeCheck;
	private boolean invalidateSlopingSurface;

	public CollisionResults(byte intentions, int instanceId, IgnoreProperties ignoreProperties) {
		this(intentions, instanceId, false, ignoreProperties);
	}

	public CollisionResults(byte intentions, int instanceId) {
		this(intentions, instanceId, false, null);
	}

	public CollisionResults(byte intentions, int instanceId, boolean searchFirst) {
		this(intentions, instanceId, searchFirst, null);
	}

	public CollisionResults(byte intentions, int instanceId, boolean searchFirst, IgnoreProperties ignoreProperties) {
		this.intentions = intentions;
		this.instanceId = instanceId;
		this.onlyFirst = searchFirst;
		this.ignoreProperties = ignoreProperties;
	}

	public void clear() {
		results.clear();
	}

	@Override
	public Iterator<CollisionResult> iterator() {
		if (!sorted) {
			results.sort(null);
			sorted = true;
		}

		return results.iterator();
	}

	public void addCollision(CollisionResult result) {
		if (Float.isNaN(result.getDistance())) {
			return;
		}
		results.add(result);
		if (!onlyFirst)
			sorted = false;
	}

	public int size() {
		return results.size();
	}

	public CollisionResult getClosestCollision() {
		if (size() == 0)
			return null;

		if (!sorted) {
			results.sort(null);
			sorted = true;
		}

		return results.get(0);
	}

	public CollisionResult getFarthestCollision() {
		if (size() == 0)
			return null;

		if (!sorted) {
			results.sort(null);
			sorted = true;
		}

		return results.get(size() - 1);
	}

	public CollisionResult getCollision(int index) {
		if (!sorted) {
			results.sort(null);
			sorted = true;
		}

		return results.get(index);
	}

	/**
	 * Internal use only.
	 * 
	 * @param index
	 * @return
	 */
	public CollisionResult getCollisionDirect(int index) {
		return results.get(index);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CollisionResults[");
		for (CollisionResult result : results) {
			sb.append(result).append(", ");
		}
		if (results.size() > 0)
			sb.setLength(sb.length() - 2);

		sb.append("]");
		return sb.toString();
	}

	/**
	 * @return True if the results should only contain one collision max.
	 */
	public boolean isOnlyFirst() {
		return onlyFirst;
	}

	/**
	 * @return the intention
	 */
	public byte getIntentions() {
		return intentions;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public IgnoreProperties getIgnoreProperties() {
		return this.ignoreProperties;
	}

	public boolean isCanSeeCheck() {
		return canSeeCheck;
	}

	public void setCanSeeCheck(boolean canSeeCheck) {
		this.canSeeCheck = canSeeCheck;
	}

	public boolean shouldInvalidateSlopingSurface() {
		return invalidateSlopingSurface;
	}

	public double getSlopingSurfaceAngleRad() {
		return SLOPING_SURFACE_ANGLE_RAD;
	}

	public void setInvalidateSlopingSurface(boolean invalidateSlopingSurface) {
		this.invalidateSlopingSurface = invalidateSlopingSurface;
	}
}
