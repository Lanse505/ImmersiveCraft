package mcjty.immcraft.network;


import io.netty.buffer.ByteBuf;
import mcjty.immcraft.api.input.KeyType;
import mcjty.immcraft.varia.BlockTools;
import mcjty.lib.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendKey implements IMessage {
    private KeyType keyType;
    private BlockPos blockPos;
    private EnumFacing side;
    private Vec3d hitVec;

    @Override
    public void fromBytes(ByteBuf buf) {
        keyType = KeyType.values()[buf.readShort()];
        blockPos = NetworkTools.readPos(buf);
        side = EnumFacing.values()[buf.readShort()];
        hitVec = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(keyType.ordinal());
        NetworkTools.writePos(buf, blockPos);
        buf.writeShort(side.ordinal());
        buf.writeDouble(hitVec.x);
        buf.writeDouble(hitVec.y);
        buf.writeDouble(hitVec.z);
    }

    public PacketSendKey() {
    }

    public PacketSendKey(KeyType keyType) {
        this.keyType = keyType;
        RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
        if (mouseOver == null) {
            return;
        }
        blockPos = mouseOver.getBlockPos();
        if (blockPos == null) {
            return;
        }
        side = mouseOver.sideHit;
        hitVec = new Vec3d(mouseOver.hitVec.x - blockPos.getX(), mouseOver.hitVec.y - blockPos.getY(), mouseOver.hitVec.z - blockPos.getZ());
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public static class Handler implements IMessageHandler<PacketSendKey, IMessage> {
        @Override
        public IMessage onMessage(PacketSendKey message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSendKey message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            BlockTools.getTE(null, world, message.blockPos)
                    .ifPresent(p -> p.onKeyPress(message.keyType, playerEntity, message.side, getLocalSide(world, message), message.hitVec));
        }

        private static EnumFacing getLocalSide(World world, PacketSendKey message) {
            return BlockTools.getBlock(world, message.blockPos)
                    .map(p -> p.worldToBlockSpace(world, message.blockPos, message.side))
                    .orElse(null);
        }
    }
}
