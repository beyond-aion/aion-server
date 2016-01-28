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

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Geometry;

/**
 * @author Kirill
 */
public class CollisionResult implements Comparable<CollisionResult> {

	private Geometry geometry;
	private Vector3f contactPoint;
	private Vector3f contactNormal;
	private float distance;

	public CollisionResult(Vector3f contactPoint, float distance) {
		this.contactPoint = contactPoint;
		this.distance = distance;
	}

	public CollisionResult() {
	}

	public void setContactPoint(Vector3f point) {
		this.contactPoint = point;
	}

	public void setDistance(float dist) {
		this.distance = dist;
	}

	public int compareTo(CollisionResult other) {
		if (distance < other.distance)
			return -1;
		else if (distance > other.distance)
			return 1;
		else
			return 0;
	}

	public void setContactNormal(Vector3f norm) {
		this.contactNormal = norm;
	}

	public void setGeometry(Geometry geom) {
		this.geometry = geom;
	}

	public Vector3f getContactNormal() {
		return contactNormal;
	}

	public Vector3f getContactPoint() {
		return contactPoint;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public float getDistance() {
		return distance;
	}

}
