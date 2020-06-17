package adudecalledleo.dontdropit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient_DisableDropKey {
    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;wasPressed()Z",
            ordinal = 6))
    public boolean dontdropit$disableDropKey(KeyBinding binding) {
        return false;
    }
}
