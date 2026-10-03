package com.zurrtum.create.client.ponder.enums;

import com.zurrtum.create.client.catnip.render.BindableTexture;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.util.Identifier;

public enum PonderSpecialTextures implements BindableTexture {

    BLANK("blank.png"),

    ;

    public static final String ASSET_PATH = "textures/special/";
    private final Identifier location;

    PonderSpecialTextures(String filename) {
        location = Ponder.asResource(ASSET_PATH + filename);
    }

    @Override
    public Identifier getLocation() {
        return location;
    }

}
