package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;


public class EntityArrow303Torch extends EntityArrow303
{
    
    public EntityArrow303Torch(World world)
    {
        super(world);
    }

    public EntityArrow303Torch(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Torch Arrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowTorch;
        tip = Block.torchWood;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:torcharrow";
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Block.torchWood.blockID))
        {
        	setDead();
        }
        return super.onHitBlock(x, y, z);
    }

    @Override
    public boolean onHitTarget(Entity entity)
    {
    	entity.setFire(300/20);
        return super.onHitTarget(entity);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("flame",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Torch entityarrow = new EntityArrow303Torch(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
