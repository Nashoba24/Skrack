package fr.nashoba24.skrack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.nashoba24.skrack.nms.ISkrackNMS;
import fr.nashoba24.skrack.skript.SkriptHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.security.KeyPair;
import java.util.HashMap;

public class Skrack extends JavaPlugin implements Listener {

    private static KeyPair keys;
    private static ISkrackNMS nms;
    private static final HashMap<String, SkrackEncryptedPlayer> skrackPlayers = new HashMap<String, SkrackEncryptedPlayer>();

    @Override
    public void onEnable() {
        if (Bukkit.getOnlineMode()) {
            this.getLogger().severe("This plugin doesn't work is online mode. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        initNMS();
        keys = nms.getKeyPair();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Login.Client.START) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    WrappedGameProfile gp = event.getPacket().getGameProfiles().read(0);
                    if (!skrackPlayers.containsKey(event.getPlayer().getAddress().toString())) {

                        SkrackEncryptedPlayer sep = new SkrackEncryptedPlayer(gp);
                        skrackPlayers.put(event.getPlayer().getAddress().toString(), sep);

                        PacketContainer encryptPacket = new PacketContainer(PacketType.Login.Server.ENCRYPTION_BEGIN);
                        encryptPacket.getModifier().write(0, "");
                        encryptPacket.getModifier().write(1, keys.getPublic());
                        encryptPacket.getModifier().write(2, sep.getVerifyBytes());

                        ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), encryptPacket);

                        event.setCancelled(true);
                    }
                }
                catch (Exception ignored) {}
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Login.Client.ENCRYPTION_BEGIN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                SkrackEncryptedPlayer sep = skrackPlayers.get(event.getPlayer().getAddress().toString());

                if (nms.verifyBytes(sep.getVerifyBytes(), keys.getPrivate(), event.getPacket().getHandle())) {
                    event.setCancelled(true);
                }
                sep.setLoginKey(nms.getLoginKey(keys.getPrivate(), event.getPacket().getHandle()));
                sep.generateServerID();
                nms.activateEncryption(event.getPlayer(), sep.getLoginKey());
                sep.updateCrackStatus();

                PacketContainer loginPacket = new PacketContainer(PacketType.Login.Client.START);
                loginPacket.getGameProfiles().write(0, sep.getGameProfile());
                try {
                    ProtocolLibrary.getProtocolManager().recieveClientPacket(event.getPlayer(), loginPacket);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });

        if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
            SkriptHook.enable(this);
            this.getLogger().info("Skript support loaded!");
        }

        this.getLogger().info("Skrack enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Skrack disabled!");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        skrackPlayers.remove(e.getPlayer().getAddress().toString());
    }

    public static KeyPair getKeys() {
        return keys;
    }

    public static ISkrackNMS getNMS() {
        return nms;
    }

    public static CrackStatus getCrackStatus(Player p) {
        SkrackEncryptedPlayer sep = skrackPlayers.get(p.getAddress().toString());
        if (sep == null) {
            return CrackStatus.UNKNOWN;
        }
        else {
            return sep.getCrackStatus();
        }
    }

    private void initNMS() {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
        try {
            @SuppressWarnings("unchecked") Class<ISkrackNMS> cl = (Class<ISkrackNMS>) Class.forName("fr.nashoba24.skrack.nms.SkrackNMS" + version);
            nms = cl.newInstance();
        } catch (Exception e) {
            Bukkit.getLogger().severe("This version (" + version + ") of craftbukkit is not supported! Supported version are from 1.8 to 1.14.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}
