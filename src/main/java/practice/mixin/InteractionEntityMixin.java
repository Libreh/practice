package practice.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static practice.Practice.practiceRegistryKey;

@Mixin(InteractionEntity.class)
public abstract class InteractionEntityMixin extends Entity {
    public InteractionEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "HEAD"), method = "handleAttack")
    public void handleAttack(Entity attacker, CallbackInfoReturnable<Boolean> ci) {
        if (attacker instanceof ServerPlayerEntity) {
            onInteract((ServerPlayerEntity) attacker, this.getCustomName().getString());
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "interact", cancellable = true)
    public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        onInteract((ServerPlayerEntity) player, this.getCustomName().getString());
        ci.setReturnValue(ActionResult.SUCCESS);
        ci.cancel();
    }

    private void onInteract(ServerPlayerEntity player, String name) {
        player.sendMessage(Text.literal(name));
        if (name.equals("Movement")) {
            player.teleport(player.getServer().getWorld(practiceRegistryKey), 0.5, 69, 1, 180, 0);
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA, SoundCategory.MASTER, 1f, 1f);
        }
        if (name.equals("Freerun")) {
            player.teleport(player.getServer().getWorld(practiceRegistryKey), -7.5, 69, -4, 180, 0);
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA, SoundCategory.MASTER, 1f, 1.5f);
        }
    }
}
