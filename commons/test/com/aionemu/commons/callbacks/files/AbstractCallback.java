package com.aionemu.commons.callbacks.files;

import com.aionemu.commons.callbacks.Callback;

@SuppressWarnings("rawtypes")
public abstract class AbstractCallback implements Callback {

    @Override
    public Class<? extends Callback> getBaseClass() {
        return AbstractCallback.class;
    }
}
