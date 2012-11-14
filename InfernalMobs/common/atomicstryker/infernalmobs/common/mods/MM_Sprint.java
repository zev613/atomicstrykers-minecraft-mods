package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Sprint extends MobModifier
{
    public MM_Sprint(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Sprint";
    }
    
    public MM_Sprint(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Sprint";
        this.nextMod = prevMod;
    }
    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 5000L;
    private boolean sprinting;
    
    @Override
    public boolean onUpdate()
    {
        if (mob.getAttackTarget() != null)
        {
            long time = System.currentTimeMillis();
            if (time > lastAbilityUse+coolDown)
            {
                lastAbilityUse = time;
                sprinting = !sprinting;
            }
            
            if (sprinting)
            {
                doSprint();
            }
        }
        
        return super.onUpdate();
    }
    
    private double modMotionX;
    private double modMotionZ;
    
    private void doSprint()
    {
        float rotationMovement = (float)((Math.atan2(mob.motionX, mob.motionZ) * 180D) / 3.1415D);
        float rotationLook = mob.rotationYaw;
        
        // god fucking dammit notch
        if(rotationLook > 360F)
        {
            rotationLook -= (rotationLook % 360F) * 360F;
        }
        else if(rotationLook < 0F)
        {
            rotationLook += ((rotationLook * -1) % 360F) * 360F;
        }
        
        // god fucking dammit, NOTCH
        if (Math.abs(rotationMovement+rotationLook) > 10F)
        {
            rotationLook -= 360F;
        }
        
        double entspeed = GetAbsSpeed(mob);
        
        // unfuck velocity lock
        if (Math.abs(rotationMovement+rotationLook) > 10F)
        {
            modMotionX = mob.motionX;
            modMotionZ = mob.motionZ;
        }
        
        if (entspeed < 0.3D)
        {
            if (GetAbsModSpeed() > 0.6D || !(mob.onGround))
            {
                modMotionX /= 1.55;
                modMotionZ /= 1.55;
            }
        
            modMotionX *= 1.5;
            mob.motionX = modMotionX;
            modMotionZ *= 1.5;
            mob.motionZ = modMotionZ;
        }
    }
    
    private double GetAbsSpeed(EntityLiving ent)
    {
        return Math.sqrt(ent.motionX*ent.motionX + ent.motionZ*ent.motionZ);
    }
    
    private double GetAbsModSpeed()
    {
        return Math.sqrt(modMotionX*modMotionX + modMotionZ*modMotionZ);
    }
}