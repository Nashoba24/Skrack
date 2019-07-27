package fr.nashoba24.skrack.nms;

import fr.nashoba24.skrack.Skrack;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.entity.Player;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.List;

public class SkrackNMS1_12_R1 implements ISkrackNMS {

    public KeyPair getKeyPair() {
        return DedicatedServer.getServer().O();
    }

    public String generateServerID(SecretKey loginKey) {
        return new BigInteger(MinecraftEncryption.a("", Skrack.getKeys().getPublic(), loginKey)).toString(16);
    }

    public boolean verifyBytes(byte[] bytes, PrivateKey privateKey, Object packet) {
        byte[] verify = ((PacketLoginInEncryptionBegin) packet).b(privateKey);
        for (int i = 0; i < 4; i++) {
            if (verify[i] != bytes[i])
                return false;
        }
        return true;
    }

    public SecretKey getLoginKey(PrivateKey privateKey, Object packet) {
        return ((PacketLoginInEncryptionBegin) packet).a(privateKey);
    }

    @SuppressWarnings("unchecked")
    public void activateEncryption(Player p, SecretKey loginKey) {
        try {
            Field g = ServerConnection.class.getDeclaredField("g");
            g.setAccessible(true);
            List<NetworkManager> list = (List<NetworkManager>) g.get(MinecraftServer.getServer().getServerConnection());
            String pAddress = p.getAddress().toString();
            NetworkManager networkManager = null;
            for (NetworkManager nm : list) {
                if (nm.getSocketAddress().toString().equals(pAddress)) {
                    networkManager = nm;
                    break;
                }
            }
            networkManager.a(loginKey);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
