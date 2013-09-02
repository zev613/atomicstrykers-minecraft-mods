package atomicstryker.ropesplus.common.arrows;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;

public class EntityArrow303Laser extends EntityArrow303
{
    
    private final String sound = "damage.fallbig";
    
    public boolean pierced;
    public Set<Entity> piercedMobs;
    
    public EntityArrow303Laser(World world)
    {
        super(world);
    }

    public EntityArrow303Laser(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
    }
    
    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Penetrating Arrow";
        craftingResults = 4;
        itemId = Settings_RopePlus.itemIdArrowLaser;
        tip = Item.redstone;
        curvature = 0.0F;
        slowdown = 1.3F;
        precision = 0.0F;
        speed = 2.0F;
        pierced = false;
        piercedMobs = new HashSet<Entity>();
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:laserarrow";
    }

    @Override
    public boolean onHitTarget(Entity entity)
    {
        entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) shooter), 8);
        pierced = true;
        piercedMobs.add(entity);
        target = null;
        worldObj.playSoundAtEntity(this, sound, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
        return false;
    }

    @Override
    public boolean isInSight(Entity entity)
    {
        return canSee(this, entity) && canSee(entity, this);
    }

    private boolean canSee(Entity entity, Entity ent)
    {
        MovingObjectPosition mop = worldObj.clip(worldObj.getWorldVec3Pool().getVecFromPool(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ),
                worldObj.getWorldVec3Pool().getVecFromPool(ent.posX, ent.posY + (double) ent.getEyeHeight(), ent.posZ));
        return mop == null || mop.typeOfHit == EnumMovingObjectType.TILE && isTransparent(worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ));
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        if (!isTransparent(inTileBlockID))
        {
            if (pierced)
            {
                setDead();
            }
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else
        {
            return false;
        }
    }

    private boolean isTransparent(int i)
    {
        return Block.lightOpacity[i] != 255;
    }
    
    @Override
    public boolean canTarget(Entity entity)
    {
        return !piercedMobs.contains(entity) && super.canTarget(entity);
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Laser entityarrow = new EntityArrow303Laser(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }

}
