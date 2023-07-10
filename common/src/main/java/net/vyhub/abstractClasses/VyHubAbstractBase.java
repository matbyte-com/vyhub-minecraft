package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;

public abstract class VyHubAbstractBase {
    protected final VyHubPlatform platform;

    public VyHubAbstractBase(VyHubPlatform platform) {
        this.platform = platform;
    }

    public VyHubPlatform getPlatform() {
        return platform;
    }
}
