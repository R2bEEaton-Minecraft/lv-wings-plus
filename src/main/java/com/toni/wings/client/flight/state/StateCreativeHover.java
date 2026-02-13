package com.toni.wings.client.flight.state;

import com.toni.wings.client.flight.Animator;

public final class StateCreativeHover extends State {
    public StateCreativeHover() {
        super(Animator::beginCreativeHover);
    }

    @Override
    protected State createCreativeHover() {
        return this;
    }
}
