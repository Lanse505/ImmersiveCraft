package mcjty.immcraft.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import mcjty.immcraft.ImmersiveCraft;
import mcjty.immcraft.blocks.ModBlocks;
import mcjty.immcraft.config.ConfigSetup;
import mcjty.immcraft.events.ForgeEventHandlers;
import mcjty.immcraft.items.ModItems;
import mcjty.immcraft.network.ImmCraftPacketHandler;
import mcjty.immcraft.worldgen.WorldGen;
import mcjty.lib.McJtyLib;
import mcjty.lib.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.concurrent.Callable;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        McJtyLib.preInit(e);

        SimpleNetworkWrapper network = PacketHandler.registerMessages(ImmersiveCraft.MODID, "immcraft");
        ImmCraftPacketHandler.registerMessages(network);

        ConfigSetup.preInit(e);
        ModBlocks.init();
        ModItems.init();
        WorldGen.init();
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ImmersiveCraft.instance, new GuiProxy());
    }

    public void postInit(FMLPostInitializationEvent e) {
        ConfigSetup.postInit();
    }

    public World getClientWorld() {
        throw new IllegalStateException("This should only be called from client side");
    }

    public EntityPlayer getClientPlayer() {
        throw new IllegalStateException("This should only be called from client side");
    }

    public <V> ListenableFuture<V> addScheduledTaskClient(Callable<V> callableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }

    public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }


}
