package com.toni.wings.client.flight;

public interface Animator {
    void beginLand();

    void beginCreativeHover();

    void beginGlide();

    void beginIdle();

    void beginLift();

    void beginFall();

    void update();
}
