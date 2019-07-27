package fr.nashoba24.skrack.skript;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import fr.nashoba24.skrack.CrackStatus;
import fr.nashoba24.skrack.Skrack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class CondPremium extends Condition {

    private Expression<Player> p;

    public boolean check(Event event) {
        boolean premium = Skrack.getCrackStatus(p.getSingle(event)) == CrackStatus.PREMIUM;
        if (isNegated()) premium = !premium;
        return premium;
    }

    public String toString(Event event, boolean b) {
        return "premium";
    }

    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        p = (Expression<Player>) expressions[0];
        if (i > 1)
            setNegated(true);
        return true;
    }
}
