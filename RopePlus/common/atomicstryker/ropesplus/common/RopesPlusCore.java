package atomicstryker.ropesplus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.MinecraftForge;
import atomicstryker.ropesplus.client.ClientPacketHandler;
import atomicstryker.ropesplus.common.arrows.EntityArrow303;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Confusion;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Dirt;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Ex;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Fire;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Grass;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Ice;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Laser;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Rope;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Slime;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Torch;
import atomicstryker.ropesplus.common.arrows.EntityArrow303Warp;
import atomicstryker.ropesplus.common.arrows.ItemArrow303;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;

@Mod(modid = "RopesPlus", name = "Ropes+", version = "1.2.3")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
connectionHandler = ConnectionHandler.class,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_Ropes"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_Ropes"}, packetHandler = ServerPacketHandler.class))
public class RopesPlusCore
{
    @SidedProxy(clientSide = "atomicstryker.ropesplus.client.RopesPlusClient", serverSide = "atomicstryker.ropesplus.common.CommonProxy")
    public static CommonProxy proxy;
    
	public final static Class coreArrowClasses[] =
	{
			EntityArrow303Dirt.class,
			EntityArrow303Ex.class,
			EntityArrow303Fire.class,
			EntityArrow303Grass.class,
			EntityArrow303Ice.class,
			EntityArrow303Laser.class,
			EntityArrow303Slime.class,
			EntityArrow303Torch.class, 
			EntityArrow303Warp.class,
			EntityArrow303Confusion.class,
			EntityArrow303Rope.class
	};
	
	public static List<EntityArrow303> arrows;
	public static Item bowRopesPlus;
	private static List<ItemArrow303> arrowItems;
	public static RopesPlusCore instance;
	
    // Rope mod part
    public static Block blockRopeCentralPos;
    public static List ropeEntArray;
    private static List<int[]> ropePosArray;
    
    // Grappling Hook mod part
    public static Block blockRopeWallPos;
    public static Block blockGrapplingHook;
    public static Item itemRope;
    public static Item itemGrapplingHook;
    private static HashMap<EntityPlayer, EntityGrapplingHook> grapplingHookMap;
    
    /* Arrow Selection Maps */
    private static HashMap<EntityPlayer, Integer> selectedSlotMap;
    private HashMap<EntityPlayer, Boolean> cycledMap;
	
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
    	instance = this;
    	arrows = new ArrayList();
    	arrowItems = new ArrayList();
    	ropeEntArray = new ArrayList();
    	ropePosArray = new ArrayList();
    	grapplingHookMap = new HashMap<EntityPlayer, EntityGrapplingHook>();
    	selectedSlotMap = new HashMap<EntityPlayer, Integer>();
    	cycledMap = new HashMap<EntityPlayer, Boolean>();
    	
        Settings_RopePlus.InitSettings(event.getSuggestedConfigurationFile());
        proxy.loadConfig(event.getSuggestedConfigurationFile());
    }
    
    @Init
    public void load(FMLInitializationEvent evt)
    {
        // Rope mod part
        itemRope = new ItemRope(Settings_RopePlus.itemIdRope);
        blockRopeCentralPos = (new BlockRopeCenter(Settings_RopePlus.blockIdRopeDJRoslin, Settings_RopePlus.ropeTexture)).setHardness(0.3F);

        // Grappling Hook mod part
        itemGrapplingHook = new ItemGrapplingHook(Settings_RopePlus.itemIdGrapplingHook).setIconIndex(13).setItemName("itemGrapplingHook");
        blockRopeWallPos = (new BlockRopeWall(Settings_RopePlus.blockIdRope, Settings_RopePlus.ropeTexture)).setHardness(0.5F).setStepSound(Block.soundClothFootstep).setBlockName("blockRope");
        blockGrapplingHook = (new BlockGrapplingHook(Settings_RopePlus.blockIdGrapplingHook, 0)).setHardness(0.0F).setStepSound(Block.soundMetalFootstep).setBlockName("blockGrHk");
        
        GameRegistry.registerBlock(blockGrapplingHook);
        GameRegistry.registerBlock(blockRopeWallPos);
        GameRegistry.registerBlock(blockRopeCentralPos);
        
        ItemStack ropeCentral = new ItemStack(blockRopeCentralPos, 12);
        ropeCentral.setItemName("blockRopeItem");
        GameRegistry.addRecipe(ropeCentral, new Object[] {" # ", " # ", " # ", Character.valueOf('#'), Item.silk});
        
        ItemStack stackGrHk = new ItemStack(itemGrapplingHook, 1);
        stackGrHk.setItemName("itemGrHk");
        GameRegistry.addRecipe(stackGrHk, new Object[] {" X ", " # ", " # ", Character.valueOf('#'), blockRopeCentralPos, Character.valueOf('X'), Item.ingotIron});
        
        LanguageRegistry.instance().addName(blockRopeCentralPos, "Rope");
        LanguageRegistry.instance().addName(blockRopeWallPos, "GrHk Rope");
        LanguageRegistry.instance().addName(blockGrapplingHook, "Grappling Hook");
        LanguageRegistry.instance().addName(itemRope, "GrHk Rope");
        LanguageRegistry.instance().addName(itemGrapplingHook, "Grappling Hook");
        
        TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
        
        EntityRegistry.registerModEntity(EntityGrapplingHook.class, "GrapplingHook", 1, this, 25, 5, true);
        
        // 303 Arrows
        bowRopesPlus = new ItemBowRopesPlus(Settings_RopePlus.itemIdRopesPlusBow).setIconCoord(5, 1).setItemName("bowRopesPlus");
        LanguageRegistry.instance().addName(bowRopesPlus, "RopesPlusBow");
        
        for(Class class1 : coreArrowClasses)
        {
            addArrowToRegister(constructArrowInstance(class1));
        }
        arrows.add(new EntityArrow303(null));
        
        MinecraftForge.EVENT_BUS.register(new RopesPlusBowController());
        
        GameRegistry.registerDispenserHandler(new DispenserHandler());
        
        proxy.load();
    }
    
    @PostInit
    public void modsLoaded(FMLPostInitializationEvent evt)
    {
        int index = 2;
        EntityArrow303 entArrow303 = null;
        for(Iterator iter = RopesPlusCore.arrows.iterator(); iter.hasNext();)
        {
            entArrow303 = (EntityArrow303)iter.next();
            makeItem(entArrow303);
            String name = (new StringBuilder()).append(entArrow303.name).append("303").toString();
            
            EntityRegistry.registerModEntity(entArrow303.getClass(), name, index, this, 25, 5, true);
            index++;
            
            //System.out.println("registered "+name+" as Networked Entity for ALL TIME!!!");
        }
    }
    
    @ServerStarted
    public void serverStarted(FMLServerStartedEvent event)
    {
        // cant think of anything yet
    }

	private static void makeItem(EntityArrow303 entityarrow303)
	{
		Item item = null;
		if(entityarrow303.itemId == Item.arrow.shiftedIndex)
		{
		    /*
		     * dont replace vanilla arrow thank you
		     * Item.itemsList[Item.arrow.shiftedIndex] = null;
		     * item = Item.arrow = (new ItemArrow303(Item.arrow.shiftedIndex - 256, entityarrow303)).setItemName(Item.arrow.getItemName());
			 */
		}
		else if (Item.arrow != null && entityarrow303.itemId != -1)
		{
			item = (new ItemArrow303(entityarrow303.itemId - 256, entityarrow303)).setItemName(entityarrow303.name);
			GameRegistry.addRecipe(new ItemStack(entityarrow303.itemId, entityarrow303.craftingResults, 0), new Object[] {
				"X", "#", "Y", Character.valueOf('X'), entityarrow303.tip, Character.valueOf('#'), Item.stick, Character.valueOf('Y'), Item.feather
			});

			arrowItems.add((ItemArrow303) item);
			LanguageRegistry.instance().addName(item, entityarrow303.name);
			item.setIconIndex(entityarrow303.getArrowIconIndex());
		}
	}

	public static EntityArrow303 constructArrowInstance(Class class1)
	{
		try
		{
			return (EntityArrow303)class1.getConstructor(new Class[] {net.minecraft.src.World.class}).newInstance(new Object[] {(World)null});
		}
		catch(Throwable throwable)
		{
			throw new RuntimeException(throwable);
		}
	}

	public static void addArrowToRegister(EntityArrow303 entityarrow303)
	{
		arrows.add(entityarrow303);
	}
	
	public static Item getArrowItemByTip(Object desiredtip)
	{
		for(Iterator iterator = arrowItems.iterator(); iterator.hasNext();)
		{
			ItemArrow303 itemarrow303 = (ItemArrow303)iterator.next();
			if(itemarrow303.arrow.tip == desiredtip)
			{
				return (Item)itemarrow303;
			}
		}

		return null;
	}
	
	public static void onRopeArrowHit(World world, int x, int y, int z)
	{
		int[] coords = new int[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		addCoordsToRopeArray(coords);
		
		BlockRopePseudoEnt newent = new BlockRopePseudoEnt(world, x, y, z, 31);
		addRopeToArray(newent);
	}
	
	public static void addRopeToArray(BlockRopePseudoEnt ent)
	{
		ropeEntArray.add(ent);
	}
	
	public static void addRopeToArray(TileEntityRope newent)
	{
		ropeEntArray.add(newent);
	}
	
	public static void addCoordsToRopeArray(int[] coords)
	{
		ropePosArray.add(coords);
	}
	
	public static void removeCoordsFromRopeArray(int[] coords)
	{
		ropePosArray.remove(coords);
	}
	
	public static int[] areCoordsArrowRope(int i, int j, int k)
	{
		for(int w = 0; w < ropePosArray.size(); w++)
		{
			int[] coords = (int[])ropePosArray.get(w);
			
			if (i == coords[0] && j == coords[1] && k == coords[2])
			{
				return coords;
			}
		}
		return null;
	}
    
    public static int selectedSlot(EntityPlayer p)
    {
        if (selectedSlotMap.get(p) == null)
        {
            return 0;
        }
        
        return selectedSlotMap.get(p);
    }
    
    public static void setselectedSlot(EntityPlayer p, int i)
    {
        selectedSlotMap.put(p, i);      
    }
    
    public static HashMap<EntityPlayer, EntityGrapplingHook> getGrapplingHookMap()
    {
    	return grapplingHookMap;
    }
}