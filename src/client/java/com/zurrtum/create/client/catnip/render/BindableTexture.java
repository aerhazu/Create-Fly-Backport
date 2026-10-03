package com.zurrtum.create.client.catnip.render;

import net.minecraft.util.Identifier;

public interface BindableTexture {

    default Identifier bind() {
        return getLocation();
    }

    Identifier getLocation();

}
