package io.quarkus.ts.mcp.app;

import java.util.List;

import io.quarkiverse.mcp.server.FeatureManager;
import io.quarkiverse.mcp.server.Icon;
import io.quarkiverse.mcp.server.IconsProvider;

public class OuterIcon implements IconsProvider {
    @Override
    public List<Icon> get(FeatureManager.FeatureInfo feature) {
        return List.of(new Icon("file://quarkus_icon_black.svg", "image/svg"));
    }
}
