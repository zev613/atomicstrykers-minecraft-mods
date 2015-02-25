package atomicstryker.minions.common.jobmanager;import java.util.ArrayList;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.init.Blocks;import net.minecraft.util.BlockPos;import net.minecraft.world.ChunkCoordIntPair;import net.minecraft.world.World;import atomicstryker.minions.common.MinionsCore;/** * Minion Mod Runnable Tree Scanner class. Finds Trees in a certain area around a first given Tree, returns a List once done. *  *  * @author AtomicStryker */public class TreeScanner implements Runnable{	private Minion_Job_TreeHarvest boss;	private World worldObj;	private int foundTreeCount;	private int currentX;	private int currentZ;	private int currentMaxX;	private int currentMaxZ;	private Block treeBlockID;	private final ArrayList<ChunkCoordIntPair> skippableCoords;		public TreeScanner(Minion_Job_TreeHarvest creator)	{		boss = creator;		skippableCoords = new ArrayList<ChunkCoordIntPair>();		foundTreeCount = 0;		currentMaxX = 0;		currentMaxZ = 0;	}	public void setup(BlockPos coords, World worldObj)	{		currentX = coords.getX();		currentZ = coords.getZ();		this.worldObj = worldObj;	}	@Override	public void run()	{		//System.out.println("AS_TreeScanner starting to run at ["+currentX+"|"+currentZ+"]");		boolean otherDirection = false;		while (foundTreeCount < 16 && currentMaxX < 64)		{			// iterate length X			int nextXStop = currentX + (otherDirection ? currentMaxX * -1 : currentMaxX);			while (currentX != nextXStop)			{				checkForTreeAtCoords();				if (otherDirection)				{					currentX--;				}				else				{					currentX++;				}			}			// iterate length Z			int nextZStop = currentZ + (otherDirection ? currentMaxZ * -1 : currentMaxZ);			while (currentZ != nextZStop)			{				checkForTreeAtCoords();				if (otherDirection)				{					currentZ--;				}				else				{					currentZ++;				}			}						// change movement direction as per search algorithm			otherDirection = !otherDirection;						// expand lengths for next run			currentMaxX++;			currentMaxZ++;		}				MinionsCore.debugPrint("AS_TreeScanner finished work, found: "+foundTreeCount+"; checked length: "+currentMaxX);		boss.onDoneFindingTrees();	}		private void checkForTreeAtCoords()	{		if (skippableCoords.contains(new ChunkCoordIntPair(currentX, currentZ))) // to fix more-than-1-block-thick trees		{			return;		}				//System.out.println("checkForTreeAtCoords ["+currentX+"|"+currentZ+"]");		BlockPos y = this.worldObj.getTopSolidOrLiquidBlock(new BlockPos(currentX, 0, currentZ));		if (y.getY() != -1)		{			Block ID = this.worldObj.getBlockState(new BlockPos(currentX, y.getY()-1, currentZ)).getBlock();			if (MinionsCore.instance.foundTreeBlocks.contains(ID))			{				Block newID;				for (y = y.down(); (newID = this.worldObj.getBlockState(y).getBlock()) == ID; y = y.down())				{				}								if (newID == Blocks.air || newID.getMaterial() == Material.leaves || newID.isLeaves(worldObj, y))				{					return;				}				else				{					onFoundTreeBase(currentX, y.getY()+1, currentZ);				}			}		}		Thread.yield();	}		private void onFoundTreeBase(int ix, int iy, int iz)	{		for (int jx = -1; jx <= 1; jx++)		{			for (int jz = -1; jz <= 1; jz++)			{				ChunkCoordIntPair excludeCoords = new ChunkCoordIntPair(ix+jx, iz+jz);				if (!skippableCoords.contains(excludeCoords))				{					skippableCoords.add(excludeCoords);				}			}		}			    ArrayList<BlockPos> treeBlockList = new ArrayList<BlockPos>();	    ArrayList<BlockPos> leaveBlockList = new ArrayList<BlockPos>();	    treeBlockID = this.worldObj.getBlockState(new BlockPos(ix, iy, iz)).getBlock();	    indexTargetTree(ix, iy, iz, treeBlockList, leaveBlockList);			    if (treeBlockList.size() > 3)		{	    	foundTreeCount++;	    	boss.onFoundTreeBase(ix, iy, iz, treeBlockList, leaveBlockList);		}	}	    private void indexTargetTree(int ix, int iy, int iz, ArrayList<BlockPos> treeBlockList, ArrayList<BlockPos> leaveBlockList)    {    	indexTreeBlockRecursive(ix, iy, iz, treeBlockList, leaveBlockList);    }        private void indexTreeBlockRecursive(int ix, int iy, int iz, ArrayList<BlockPos> treeBlockList, ArrayList<BlockPos> leaveBlockList)    {        byte one = 1;        for (int xIter = -one; xIter <= one; xIter++)        {            for (int zIter = -one; zIter <= one; zIter++)            {                for (int yIter = 0; yIter <= one; yIter++)                {                    if (worldObj.getBlockState(new BlockPos(ix + xIter, iy + yIter, iz + zIter)).getBlock() == treeBlockID)                    {                    	BlockPos coords = new BlockPos(ix + xIter, iy + yIter, iz + zIter);                    	if (!treeBlockList.contains(coords))                    	{                    		treeBlockList.add(coords);                    		findLeavesRecursive(ix + xIter, iy + yIter, iz + zIter, 0, leaveBlockList);                    		indexTreeBlockRecursive(ix + xIter, iy + yIter, iz + zIter, treeBlockList, leaveBlockList);                    	}                    }                }            }        }    }        private void findLeavesRecursive(int ix, int iy, int iz, int fromStem, ArrayList<BlockPos> leaveBlockList)    {        for (int xIter = -1; xIter <= 1; xIter++)        {            for (int zIter = -1; zIter <= 1; zIter++)            {                for (int yIter = 0; yIter <= 1; yIter++)                {                    final BlockPos coords = new BlockPos(ix + xIter, iy + yIter, iz + zIter);                    final Block b = worldObj.getBlockState(coords).getBlock();                	if (b.getMaterial() == Material.leaves || b.isLeaves(worldObj, coords))                    {                    	if (fromStem < 4)                    	{                        	if (!leaveBlockList.contains(coords))                        	{                        		leaveBlockList.add(coords);                        		findLeavesRecursive(ix + xIter, iy + yIter, iz + zIter, fromStem+1, leaveBlockList);                        	}                    	}                    }                }            }        }    }}