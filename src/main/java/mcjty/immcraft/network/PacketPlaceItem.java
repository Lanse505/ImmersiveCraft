package mcjty.immcraft.network;


import io.netty.buffer.ByteBuf;
import mcjty.immcraft.blocks.ModBlocks;
import mcjty.immcraft.blocks.inworldplacer.InWorldPlacerTE;
import mcjty.immcraft.blocks.inworldplacer.InWorldVerticalPlacerTE;
import mcjty.immcraft.varia.BlockTools;
import mcjty.lib.network.NetworkTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPlaceItem implements IMessage {
    private BlockPos blockPos;
    private EnumFacing side;
    private Vec3d hitVec;

    @Override
    public void fromBytes(ByteBuf buf) {
        blockPos = NetworkTools.readPos(buf);
        side = EnumFacing.values()[buf.readShort()];
        hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, blockPos);
        buf.writeShort(side.ordinal());
        buf.writeDouble(hitVec.x);
        buf.writeDouble(hitVec.y);
        buf.writeDouble(hitVec.z);
    }

    public PacketPlaceItem() {
    }

    public PacketPlaceItem(RayTraceResult mouseOver) {
        blockPos = mouseOver.getBlockPos();
        side = mouseOver.sideHit;
        hitVec = new Vec3d(mouseOver.hitVec.x - blockPos.getX(), mouseOver.hitVec.y - blockPos.getY(), mouseOver.hitVec.z - blockPos.getZ());
    }

    public static class Handler implements IMessageHandler<PacketPlaceItem, IMessage> {
        @Override
        public IMessage onMessage(PacketPlaceItem message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketPlaceItem message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }

            World world = player.getEntityWorld();

            Block block = world.getBlockState(message.blockPos).getBlock();
            if (world.isAirBlock(message.blockPos.up())
                    && BlockTools.isTopValidAndSolid(world, message.blockPos)
                    && message.side == EnumFacing.UP) {
                BlockTools.placeBlock(world, message.blockPos.up(), ModBlocks.inWorldPlacerBlock, player);
                BlockTools.getInventoryTE(world, message.blockPos.up()).ifPresent(p -> InWorldPlacerTE.addItems(p, player, heldItem));
            } else if (world.isAirBlock(message.blockPos.offset(message.side))
                    && BlockTools.isSideValidAndSolid(world, message.blockPos, message.side, block)) {
                BlockTools.placeBlock(world, message.blockPos.offset(message.side), ModBlocks.inWorldVerticalPlacerBlock, player);
                BlockTools.getInventoryTE(world, message.blockPos.offset(message.side)).ifPresent(p -> InWorldVerticalPlacerTE.addItems(p, player, heldItem));
            }
        }
    }
}
