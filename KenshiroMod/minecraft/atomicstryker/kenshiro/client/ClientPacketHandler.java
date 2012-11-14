package atomicstryker.kenshiro.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.ForgePacketWrapper;
import atomicstryker.kenshiro.common.KenshiroMod;
import atomicstryker.kenshiro.common.PacketType;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(data);
        
        if (packetType == PacketType.HANDSHAKE.ordinal())
        {
            KenshiroClient.instance().setServerHasKenshiroInstalled(true);
        }
        else if (packetType == PacketType.BLOCKPUNCHED.ordinal())
        {
            
        }
        else if (packetType == PacketType.ENTITYPUNCHED.ordinal())
        {
            Class[] decodeAs = { Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            Entity target = KenshiroMod.instance().getEntityByID(FMLClientHandler.instance().getClient().theWorld, (Integer)packetReadout[0]);
            if (target != null
            && target instanceof EntityLiving)
            {
                KenshiroClient.instance().onEntityPunched((EntityLiving) target);
            }
        }
        else if (packetType == PacketType.KENSHIROSTARTED.ordinal())
        {
            
        }
        else if (packetType == PacketType.ENTITYKICKED.ordinal())
        {
            Class[] decodeAs = { Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            Entity target = KenshiroMod.instance().getEntityByID(FMLClientHandler.instance().getClient().theWorld, (Integer)packetReadout[1]);
            if (target != null
            && target instanceof EntityLiving)
            {
                Entity kicker = KenshiroMod.instance().getEntityByID(FMLClientHandler.instance().getClient().theWorld, (Integer)packetReadout[0]);
                if (kicker != null
                && kicker instanceof EntityPlayer)
                {
                    KenshiroClient.instance().onEntityKicked((EntityPlayer)kicker, (EntityLiving) target);
                }
            }
        }
        else if (packetType == PacketType.SOUNDEFFECT.ordinal())
        {
            Class[] decodeAs = { String.class, Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            String sound = (String) packetReadout[0];
            FMLClientHandler.instance().getClient().theWorld.playSound((Integer)packetReadout[1]+0.5D, (Integer)packetReadout[2]+0.5D, (Integer)packetReadout[3]+0.5D, sound, 1.0F, 1.0F);
        }
    }

}