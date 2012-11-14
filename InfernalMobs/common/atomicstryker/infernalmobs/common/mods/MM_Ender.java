package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ender extends MobModifier
{
    public MM_Ender(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Ender";
    }
    
    public MM_Ender(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Ender";
        this.nextMod = prevMod;
    }
    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        long time = System.currentTimeMillis();
        if (time > lastAbilityUse+coolDown
        && source.getEntity() != null
        && teleportToEntity(source.getEntity()))
        {
            lastAbilityUse = time;
            source.getEntity().attackEntityFrom(DamageSource.causeMobDamage(mob), damage);
            
            return super.onHurt(source, 0);
        }
        
        return super.onHurt(source, damage);
    }
    
    private boolean teleportToEntity(Entity par1Entity)
    {
        Vec3 vector = Vec3.createVectorHelper(mob.posX - par1Entity.posX, mob.boundingBox.minY + (double)(mob.height / 2.0F) - par1Entity.posY + (double)par1Entity.getEyeHeight(), mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.xCoord * telDist;
        double destY = mob.posY + (double)(mob.worldObj.rand.nextInt(16) - 8) - vector.yCoord * telDist;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.zCoord * telDist;
        return teleportTo(destX, destY, destZ);
    }
    
    private boolean teleportTo(double destX, double destY, double destZ)
    {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        mob.posX = destX;
        mob.posY = destY;
        mob.posZ = destZ;
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        int blockID;

        if (mob.worldObj.blockExists(x, y, z))
        {
            boolean hitGround = false;
            while (!hitGround && y < 96)
            {
                blockID = mob.worldObj.getBlockId(x, y + 1, z);

                if (blockID == 0 || !Block.blocksList[blockID].blockMaterial.blocksMovement())
                {
                    hitGround = true;
                }
                else
                {
                    ++mob.posY;
                    ++y;
                }
            }

            if (hitGround)
            {
                mob.setPosition(mob.posX, mob.posY, mob.posZ);
            }
            else
            {
                return false;
            }
        }
        
        short var30 = 128;
        for (blockID = 0; blockID < var30; ++blockID)
        {
            double var19 = (double)blockID / ((double)var30 - 1.0D);
            float var21 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
            float var22 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
            float var23 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
            double var24 = oldX + (mob.posX - oldX) * var19 + (mob.worldObj.rand.nextDouble() - 0.5D) * (double)mob.width * 2.0D;
            double var26 = oldY + (mob.posY - oldY) * var19 + mob.worldObj.rand.nextDouble() * (double)mob.height;
            double var28 = oldZ + (mob.posZ - oldZ) * var19 + (mob.worldObj.rand.nextDouble() - 0.5D) * (double)mob.width * 2.0D;
            mob.worldObj.spawnParticle("portal", var24, var26, var28, (double)var21, (double)var22, (double)var23);
        }
        
        mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "mob.endermen.portal", 1.0F, 1.0F);
        mob.worldObj.playSoundAtEntity(mob, "mob.endermen.portal", 1.0F, 1.0F);
        return true;

    }
}