package com.aionemu.gameserver.geoEngine.collision.bih;

import java.util.ArrayList;

import com.aionemu.gameserver.geoEngine.collision.bih.BIHNodeEx.BIHStackData;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;

/**
 * Modified replication of {@link com.jme3.util.TempVars TempVars}, needed for {@link com.jme3.collision.bih.BIHNode BIHNode}.
 * 
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see com.jme3.util.TempVars
 */
class BIHTempVars {

	private static final int STACK_SIZE = 5;

	private static class BIHTempVarsStack {

		int index = 0;
		BIHTempVars[] BIHTempVars = new BIHTempVars[STACK_SIZE];
	}

	private static final ThreadLocal<BIHTempVarsStack> varsLocal = new ThreadLocal<BIHTempVarsStack>() {

		@Override
		protected BIHTempVarsStack initialValue() {
			return new BIHTempVarsStack();
		}
	};

	private boolean isUsed = false;

	protected final Vector3f vect1 = new Vector3f();
	protected final Vector3f vect2 = new Vector3f();
	protected final Vector3f vect3 = new Vector3f();
	protected final Vector3f vect4 = new Vector3f();
	protected final Vector3f vect5 = new Vector3f();
	protected final Matrix4f tempMat4 = new Matrix4f();
	protected final float[] bihSwapTmp = new float[9];
	protected final ArrayList<BIHStackData> bihStack = new ArrayList<BIHStackData>();

	private BIHTempVars() {
	}

	protected static BIHTempVars get() {
		BIHTempVarsStack stack = varsLocal.get();
		BIHTempVars instance = stack.BIHTempVars[stack.index];

		if (instance == null) {
			instance = new BIHTempVars();
			stack.BIHTempVars[stack.index] = instance;
		}

		stack.index++;
		instance.isUsed = true;

		return instance;
	}

	protected void release() {
		if (!isUsed)
			throw new IllegalStateException("This instance of BIHTempVars was already released!");

		isUsed = false;

		BIHTempVarsStack stack = varsLocal.get();

		stack.index--;

		if (stack.BIHTempVars[stack.index] != this) {
			throw new IllegalStateException("An instance of BIHTempVars has not been released in a called method!");
		}
	}
}
