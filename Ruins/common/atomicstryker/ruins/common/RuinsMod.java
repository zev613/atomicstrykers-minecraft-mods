package atomicstryker.ruins.common;

import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.channels.*;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.src.AnvilChunkLoader;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.IChunkLoader;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.SaveHandler;
import net.minecraft.src.World;

@Mod(modid = "AS_Ruins", name = "Ruins Mod", version = "8.0", dependencies = "after:ExtraBiomes")
public class RuinsMod
{
    public final static int FILE_TEMPLATE = 0, FILE_COMPLEX = 1;
    public final static String TEMPLATE_EXT = "tml", COMPLEX_EXT = "cml";
    public final static int DIR_NORTH = 0, DIR_EAST = 1, DIR_SOUTH = 2, DIR_WEST = 3;
    public static final int BIOME_NONE = 500;

    private RuinHandler ruins;
    private RuinGenerator generator;
    private World curWorld = null;

    @Init
    public void load(FMLInitializationEvent evt)
    {
        GameRegistry.registerWorldGenerator(new RuinsWorldGenerator());
    }
    
    public class RuinsWorldGenerator implements IWorldGenerator
    {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
        {
            if (world.getBiomeGenForCoords(chunkX, chunkZ) == BiomeGenBase.hell)
            {
                generateNether(world, random, chunkX*16, chunkZ*16);
            }
            else if (world.getBiomeGenForCoords(chunkX, chunkZ) == BiomeGenBase.sky)
            {
                // the end!
            }
            else // normal world
            {
                generateSurface(world, random, chunkX*16, chunkZ*16);
            }
        }
    }


    private void generateNether(World world, Random random, int chunkX, int chunkZ)
    {
        checkWorld(world);
        if (ruins != null && ruins.loaded)
        {
            // can change this generation as needed. We're going for overblown
            // right now,
            // checking out side the chunk and such because generation seems to
            // breaks down.

            generator.generateNether(world, random, chunkX, 0, chunkZ);
        }
    }

    private void generateSurface(World world, Random random, int chunkX, int chunkZ)
    {
        checkWorld(world);
        if (ruins != null && ruins.loaded)
        {
            // can change this generation as needed. We're going for overblown
            // right now,
            // checking out side the chunk and such because generation seems to
            // breaks down.

            generator.generateNormal(world, random, chunkX, 0, chunkZ);
        }
    }

    private void checkWorld(World world)
    {
        if (curWorld == world)
        {
            return;
        }
        if (curWorld == null)
        {
            // new world has definitely been created.
            curWorld = world;
            createHandler(curWorld);
        }
        else
        {
            // we check the filename here in case we simply went to the nether.
            File olddir = getWorldSaveDir(curWorld);
            File newdir = getWorldSaveDir(world);
            if (olddir.compareTo(newdir) != 0)
            {
                // new world has definitely been created.
                try
                {
                    ruins.writeExclusions(olddir);
                }
                catch (Exception e)
                {
                    System.err.println("Could not write exclusions for world: " + olddir.getName());
                    e.printStackTrace();
                }
                curWorld = world;
                createHandler(curWorld);
            }
        }
    }

    public static File getWorldSaveDir(World world)
    {
        ISaveHandler worldsaver = world.getSaveHandler();
        AnvilChunkLoader loader = (AnvilChunkLoader) worldsaver.getChunkLoader(world.provider);
        System.out.println("Ruins mod determines World Save Dir to be at: "+loader.chunkSaveLocation);
        return loader.chunkSaveLocation;
    }

    public static int getBiomeFromName(String name)
    {
        for (int i = 0; i < BiomeGenBase.biomeList.length; i++)
        {
            if (BiomeGenBase.biomeList[i].biomeName.equalsIgnoreCase(name))
            {
                return BiomeGenBase.biomeList[i].biomeID;
            }
        }

        return -1;
    }

    public static File getMinecraftBaseDir()
    {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            return FMLClientHandler.instance().getClient().getMinecraftDir();
        }
            
        return FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
    }

    private void createHandler(World world)
    {
        // load in defaults
        try
        {
            File worlddir = getWorldSaveDir(world);
            ruins = new RuinHandler(worlddir);
            generator = new RuinGenerator(ruins);
        }
        catch (Exception e)
        {
            System.err.println("There was a problem loading the ruins mod:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void copyGlobalOptionsTo(File dir) throws Exception
    {
        File copyfile = new File(dir, "ruins.txt");
        if (copyfile.exists())
        {
            return;
        }
        File basedir = getMinecraftBaseDir();
        basedir = new File(basedir, "mods");
        File basefile = new File(basedir, "ruins.txt");
        if (!basefile.exists())
        {
            createDefaultGlobalOptions(basedir);
        }
        FileChannel in = new FileInputStream(basefile).getChannel();
        FileChannel out = new FileOutputStream(copyfile).getChannel();
        try
        {
            in.transferTo(0, in.size(), out);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
    }

    private static void createDefaultGlobalOptions(File dir) throws Exception
    {
        File file = new File(dir, "ruins.txt");
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        pw.println("# Global Options for the Ruins mod");
        pw.println("#");
        pw.println("# tries_per_chunk is the number of times, per chunk, that the generator will");
        pw.println("#     attempt to create a ruin.");
        pw.println("#");
        pw.println("# chance_to_spawn is the chance, out of 100, that a ruin will be generated per");
        pw.println("#     try in this chunk.  This may still fail if the ruin does not have a");
        pw.println("#     suitable place to generate.");
        pw.println("#");
        pw.println("# chance_for_site is the chance, out of 100, that another ruin will attempt to");
        pw.println("#     spawn nearby if a ruin was already successfully spawned.  This bypasses");
        pw.println("#     the normal tries per chunk, so if this chance is set high you may end up");
        pw.println("#     with a lot of ruins even with a low tries per chunk and chance to spawn.");
        pw.println("#");
        pw.println("# specific_<biome name> is the chance, out of 100, that a ruin spawning in the");
        pw.println("#     specified biome will be chosen from the biome specific folder.  If not,");
        pw.println("#     it will choose a generic ruin from the root ruin folder.");
        pw.println();
        pw.println("tries_per_chunk_normal=6");
        pw.println("chance_to_spawn_normal=10");
        pw.println("chance_for_site_normal=15");
        pw.println("chunks_behind_normal=5");
        pw.println();
        pw.println("tries_per_chunk_nether=6");
        pw.println("chance_to_spawn_nether=10");
        pw.println("chance_for_site_nether=15");
        pw.println("chunks_behind_nether=5");
        pw.println();
        // print all the biomes!
        for (int i = 0; i < BiomeGenBase.biomeList.length && BiomeGenBase.biomeList[i] != null; i++)
        {
            pw.println("specific_" + BiomeGenBase.biomeList[i].biomeName + "=75");
        }
        pw.flush();
        pw.close();
    }
}