package org.figuramc.figura.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.access.ISkullBlockMixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(SkullBlockEntity.class)
public class SkullBlockEntityMixin extends BlockEntity implements ISkullBlockMixin {

    @Unique
    // profile.getName() gets lowercase'd after the GameProfile manager finds out that there is no player with such name.
    // We store it here before that happens.
    private String skullOwner;

    @Unique
    public void setSkullOwner(String newOwner) {
        skullOwner = newOwner;
    }

    @Unique
    @Nullable
    public String getSkullOwner() {
        return skullOwner;
    }

    public SkullBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(at = @At("HEAD"), cancellable = true, method = "updateGameprofile")
    private static void updateGameprofile(GameProfile owner, Consumer<GameProfile> callback, CallbackInfo ci) {
        if (owner != null) {
            String playerOwner = owner.getName();
            if (!StringUtil.isNullOrEmpty(playerOwner)) {
                // Check if it's a special one (should have an /)
                if (playerOwner.contains("/")) {
                    GameProfile profilex = new GameProfile(UUID.randomUUID(), owner.getName());

                    callback.accept(profilex);
                    ci.cancel();
                }
            }
        }
    }
}
