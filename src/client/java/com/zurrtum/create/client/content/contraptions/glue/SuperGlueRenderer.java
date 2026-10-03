package com.zurrtum.create.client.content.contraptions.glue;

import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class SuperGlueRenderer extends EntityRenderer<SuperGlueEntity> {

    public SuperGlueRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(SuperGlueEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(SuperGlueEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

}
