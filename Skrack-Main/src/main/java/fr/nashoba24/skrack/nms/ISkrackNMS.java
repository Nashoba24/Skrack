package fr.nashoba24.skrack.nms;

import org.bukkit.entity.Player;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;

public interface ISkrackNMS {

    KeyPair getKeyPair();

    String generateServerID(SecretKey loginKey);

    boolean verifyBytes(byte[] bytes, PrivateKey privateKey, Object packet);

    SecretKey getLoginKey(PrivateKey privateKey, Object packet);

    void activateEncryption(Player p, SecretKey loginKey);
}
