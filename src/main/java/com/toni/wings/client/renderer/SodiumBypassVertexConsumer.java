package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Transparent wrapper that intentionally does not implement mod-specific fast-path interfaces
 * (for example Embeddium/Sodium/Oculus vertex writers), forcing vanilla vertex submission.
 */
public final class SodiumBypassVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;

    private SodiumBypassVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public static VertexConsumer wrap(VertexConsumer buffer) {
        if (buffer == null || buffer instanceof SodiumBypassVertexConsumer) {
            return buffer;
        }
        return new SodiumBypassVertexConsumer(buffer);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.delegate.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        this.delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        this.delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        this.delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        this.delegate.endVertex();
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        this.delegate.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }
}
