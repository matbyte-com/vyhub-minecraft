package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;

public abstract class SuperClass {
    private final VyHubPlatform platform;

    public SuperClass(VyHubPlatform platform) {
        this.platform = platform;
    }

    public VyHubPlatform getPlatform() {
        return platform;
    }
}
