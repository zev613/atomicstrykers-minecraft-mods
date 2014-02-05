package atomicstryker.ropesplus.common;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockRopePseudoEnt
{
	private int delay;
	private World world;
	private int ix;
	private int iy;
	private int iz;
	private int remainrope;

    public BlockRopePseudoEnt(World w, int x, int y, int z, int l)
    {
		world = w;
		ix = x;
		iy = y;
		iz = z;
		delay = 20;
		remainrope = l;
    }
	
	public boolean OnUpdate()
	{
		if (delay < 0) return true;
		delay--;
		
		if (delay == 0)
		{
			if ((world.getBlock(ix, iy - 1, iz) == Blocks.air || world.getBlock(ix, iy - 1, iz) == Blocks.snow))
			{
				remainrope -= 1;
				if (remainrope <= 0)
				{
					return true;
				}
				
				world.setBlock(ix, iy - 1, iz, RopesPlusCore.instance.blockRope, 0, 3);
				BlockRopePseudoEnt newent = new BlockRopePseudoEnt(world, ix, iy - 1, iz, remainrope);
				RopesPlusCore.instance.addRopeToArray(newent);
				
				int[] coords = new int[3];
				coords[0] = ix;
				coords[1] = iy - 1;
				coords[2] = iz;
				RopesPlusCore.instance.addCoordsToRopeArray(coords);
			}
			return true;
		}
		
		return false;
	}
}
