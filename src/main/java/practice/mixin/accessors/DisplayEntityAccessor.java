package practice.mixin.accessors;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.class)
public interface DisplayEntityAccessor {

    @Accessor
    static TrackedData<Vector3f> getSCALE() {
        throw new UnsupportedOperationException();
    }
}
