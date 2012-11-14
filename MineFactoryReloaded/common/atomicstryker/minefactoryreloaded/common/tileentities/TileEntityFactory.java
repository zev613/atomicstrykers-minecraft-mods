package atomicstryker.minefactoryreloaded.common.tileentities;

import cpw.mods.fml.common.network.PacketDispatcher;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.PacketWrapper;
import atomicstryker.minefactoryreloaded.common.core.Area;
import atomicstryker.minefactoryreloaded.common.core.BlockPosition;
import atomicstryker.minefactoryreloaded.common.core.IRotateableTile;
import atomicstryker.minefactoryreloaded.common.core.Util;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.transport.IPipeEntry;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public abstract class TileEntityFactory extends TileEntity implements IRotateableTile
{
	private Orientations forwardDirection;
	
	protected TileEntityFactory()
	{
		forwardDirection = Orientations.XPos;
	}
	
	protected boolean canDropInPipeAt(Orientations o)
	{
		return true;
	}
	
	protected void dropStack(ItemStack s, float dropOffsetX, float dropOffsetY, float dropZ)
	{
		for(Orientations o : Util.findPipes(worldObj, xCoord, yCoord, zCoord))
		{
			BlockPosition bp = new BlockPosition(xCoord, yCoord, zCoord);
			bp.orientation = o;
			bp.moveForwards(1);
			TileEntity te = worldObj.getBlockTileEntity(bp.x, bp.y, bp.z);
			if(te != null && te instanceof IPipeEntry && canDropInPipeAt(o) && ((IPipeEntry)te).acceptItems())
			{
				Position ep = new Position(this);
				ep.x += 0.5;
				ep.y += 0.25;
				ep.z += 0.5;
				ep.orientation = o;
				ep.moveForwards(0.5);
				
				((IPipeEntry)te).entityEntering(s, o);
				return;
			}
		}
		
		if(Util.getBool(MineFactoryReloadedCore.machinesCanDropInChests))
		{
			for(IInventory chest : Util.findChests(worldObj, xCoord, yCoord, zCoord))
			{
				if(chest.getInvName() == "Engine")
				{
					continue;
				}
				s.stackSize = Util.addToInventory(chest, s);
				if(s.stackSize == 0)
				{
					return;
				}
			}
		}
		if(s.stackSize > 0)
		{
			EntityItem entityitem = new EntityItem(worldObj, xCoord + dropOffsetX, yCoord + dropOffsetY, zCoord + dropZ, s);
			entityitem.motionX = 0.0D;
			entityitem.motionY = 0.3D;
			entityitem.motionZ = 0.0D;
			worldObj.spawnEntityInWorld(entityitem);
		}
	}
	
    /**
     * Overriden in a sign to provide the text.
     */
	@Override
    public Packet getDescriptionPacket()
    {
        Object[] toSend = {xCoord, yCoord, zCoord, forwardDirection.ordinal()};
        return PacketWrapper.createPacket("MFReloaded", 1, toSend);
    }
	
	protected int getHarvestRadius()
	{
		return 1;
	}
	
	protected int getHarvestDistanceDown()
	{
		return 0;
	}
	
	protected int getHarvestDistanceUp()
	{
		return 0;
	}
	
	protected final Area getHarvestArea()
	{
		BlockPosition ourpos = BlockPosition.fromFactoryTile(this);
		ourpos.moveForwards(getHarvestRadius() + 1);
		return new Area(ourpos, getHarvestRadius(), 0, 0);
	}
	
	public Orientations getDirectionFacing()
	{
		return forwardDirection;
	}
	
	@Override
	public boolean canRotate()
	{
		return true;
	}
	
	@Override
	public void rotate()
	{
	    if(!worldObj.isRemote)
	    {
	        if(forwardDirection == Orientations.XPos)
	        {
	            forwardDirection = Orientations.ZPos;
	        }
	        else if(forwardDirection == Orientations.ZPos)
	        {
	            forwardDirection = Orientations.XNeg;
	        }
	        else if(forwardDirection == Orientations.XNeg)
	        {
	            forwardDirection = Orientations.ZNeg;
	        }
	        else if(forwardDirection == Orientations.ZNeg)
	        {
	            forwardDirection = Orientations.XPos;
	        }
	        else
	        {
	            forwardDirection = Orientations.XPos;
	        }
	        
	        worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	        MineFactoryReloadedCore.instance().onRotatedTileEntity(this, forwardDirection);
	    }
	}
	
	public void rotateDirectlyTo(int rotation)
	{
		forwardDirection = Orientations.dirs()[rotation];
		if (worldObj != null)
		{
		    worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	public int getRotatedSide(int side)
	{
		if(side < 2)
		{
			return side;
		}
		else if(forwardDirection == Orientations.ZPos)
		{
			return addToSide(side, 1);
		}
		else if(forwardDirection == Orientations.XNeg)
		{
			return addToSide(side, 2);
		}
		else if(forwardDirection == Orientations.ZNeg)
		{
			return addToSide(side, 3);
		}
		return side;
	}
	
	private int addToSide(int side, int shift)
	{
		int shiftsRemaining = shift;
		int out = side;
		while(shiftsRemaining > 0)
		{
			if(out == 2) out = 4;
			else if(out == 4) out = 3;
			else if(out == 3) out = 5;
			else if(out == 5) out = 2;
			shiftsRemaining--;
		}
		return out;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		super.readFromNBT(nbttagcompound);
		int rotation = nbttagcompound.getInteger("rotation");
		rotateDirectlyTo(rotation);
    }
	
	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("rotation", getDirectionFacing().ordinal());
    }
	
	protected boolean produceLiquid(int liquidId)
	{
		int amountToFill = LiquidManager.BUCKET_VOLUME;
		for(int i = 0; i < 6; i++)
		{
			Orientations or = Orientations.values()[i];
			
			BlockPosition p = new BlockPosition(xCoord, yCoord, zCoord, or);
			p.moveForwards(1);

			TileEntity tile = worldObj.getBlockTileEntity(p.x, p.y,	p.z);

			if(tile instanceof ITankContainer && !(p.x == xCoord && p.y == yCoord && p.z == zCoord))
			{
				ITankContainer lc = (ITankContainer)tile;
				amountToFill -= lc.fill(or.reverse(), new LiquidStack(liquidId, LiquidManager.BUCKET_VOLUME), true);
			}
		}
		if(amountToFill < LiquidManager.BUCKET_VOLUME)
		{
			return true;
		}
		return false;
	}
}