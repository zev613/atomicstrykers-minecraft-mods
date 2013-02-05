package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Wither extends MobModifier
{
    public MM_Wither(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Wither";
    }
    
    public MM_Wither(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Wither";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLiving))
        {
            ((EntityLiving)source.getEntity()).addPotionEffect(new PotionEffect(Potion.wither.id, 120, 0));
        }
        
        return super.onHurt(source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null)
        {
            entity.addPotionEffect(new PotionEffect(Potion.wither.id, 120, 0));
        }
        
        return super.onAttack(entity, source, damage);
    }
}