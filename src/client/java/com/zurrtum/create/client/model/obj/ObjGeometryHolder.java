package com.zurrtum.create.client.model.obj;

import org.jetbrains.annotations.Nullable;

/**
 * Mixed into {@code JsonUnbakedModel} so that a model file using {@code "loader": "neoforge:obj"} can carry its
 * parsed OBJ geometry directly on a real {@code JsonUnbakedModel} instance, instead of a separate class implementing
 * {@code UnbakedModel}. This keeps such models usable anywhere vanilla expects a concrete {@code JsonUnbakedModel}
 * (e.g. as the resolved target of another model's {@code "parent"} reference), which a standalone custom
 * {@code UnbakedModel} implementation cannot be, since {@code JsonUnbakedModel.setParents} requires its parent to
 * be a {@code JsonUnbakedModel}.
 */
public interface ObjGeometryHolder {
    @Nullable
    ObjGeometry create$getObjGeometry();

    void create$setObjGeometry(ObjGeometry geometry);
}
