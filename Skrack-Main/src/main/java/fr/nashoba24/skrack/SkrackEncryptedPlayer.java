package fr.nashoba24.skrack;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;

public class SkrackEncryptedPlayer {

    private static final Random random = new Random();

    private final WrappedGameProfile gameProfile;
    private CrackStatus crackStatus = CrackStatus.UNKNOWN;
    private final byte[] verify = new byte[4];
    private String serverId;
    private SecretKey loginKey;

    public SkrackEncryptedPlayer(WrappedGameProfile gameProfile) {
        this.gameProfile = gameProfile;
        generateVerifyBytes();
    }

    private void generateVerifyBytes() {
        random.nextBytes(verify);
    }

    public void generateServerID() {
       serverId = Skrack.getNMS().generateServerID(loginKey);
    }

    public byte[] getVerifyBytes() {
        return verify;
    }

    public WrappedGameProfile getGameProfile() {
        return gameProfile;
    }

    public void setLoginKey(SecretKey loginKey) {
        this.loginKey = loginKey;
    }

    public SecretKey getLoginKey() {
        return loginKey;
    }

    public void updateCrackStatus() {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + getGameProfile().getName() +"&serverId=" + serverId);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String response = br.readLine();
            crackStatus = response==null?CrackStatus.CRACK:CrackStatus.PREMIUM;
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CrackStatus getCrackStatus() {
        return crackStatus;
    }
}
