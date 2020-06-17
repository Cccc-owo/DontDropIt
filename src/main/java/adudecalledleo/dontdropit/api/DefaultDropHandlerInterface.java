package adudecalledleo.dontdropit.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DefaultDropHandlerInterface implements DropHandlerInterface {
    public static final DefaultDropHandlerInterface INSTANCE = new DefaultDropHandlerInterface();

    @Override
    public boolean isDropKeyDown(MinecraftClient mc) {
        return mc.options.keyDrop.isPressed();
    }

    @Override
    public ItemStack getCurrentStack(MinecraftClient mc) {
        return mc.player.getMainHandStack();
    }

    @Override
    public void drop(boolean entireStack, MinecraftClient mc) {
        PlayerActionC2SPacket.Action action = entireStack ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(action, BlockPos.ORIGIN, Direction.DOWN));
        boolean doSwingAnim = mc.player.inventory.takeInvStack(mc.player.inventory.selectedSlot,
                entireStack && !mc.player.getMainHandStack().isEmpty() ? mc.player.getMainHandStack().getCount() : 1)
                                       != ItemStack.EMPTY;
        if (doSwingAnim)
            mc.player.swingHand(Hand.MAIN_HAND);
    }
}