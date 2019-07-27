package fr.nashoba24.skrack.skript;

import ch.njol.skript.Skript;
import org.bukkit.plugin.java.JavaPlugin;

public class SkriptHook {

    public static void enable(JavaPlugin plugin) {
        Skript.registerAddon(plugin);

        Skript.registerCondition(CondCrackStatusKnown.class, "crack status of %player% is known", "%player%['s] crack status is known", "crack status of %player% is(n't| not) known", "%player%['s] crack status is(n't| not) known");
        Skript.registerCondition(CondPremium.class, "%player% is premium", "%player% is(n't| not) crack[ed]", "%player% is crack[ed]", "%player% is(n't| not) premium");
    }
}
