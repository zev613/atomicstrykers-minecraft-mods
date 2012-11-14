package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Berserk extends MobModifier
{
    public MM_Berserk(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Berserk";
    }
    
    public MM_Berserk(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Berserk";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null)
        {
            mob.attackEntityFrom(DamageSource.generic, damage);
            damage *= 2;
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    public Class[] getWhiteListMobClasses()
    {
        return allowed;
    }
    private static Class[] allowed = { EntityMob.class, EntityWolf.class };
    
    @Override
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class[] disallowed = { EntityCreeper.class };
}