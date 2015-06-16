package com.aionemu.commons.callbacks.files;

import com.aionemu.commons.callbacks.metadata.ObjectCallback;

public class TestCallbackIntObject {

    private final int value;

	public TestCallbackIntObject(){
		this(0);
	}

    public TestCallbackIntObject(int value) {
        this.value = value;
    }

    @ObjectCallback(AbstractCallback.class)
    public int getValue() {
        return value;
    }
}
