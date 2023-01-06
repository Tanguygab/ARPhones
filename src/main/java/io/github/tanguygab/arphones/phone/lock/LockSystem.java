package io.github.tanguygab.arphones.phone.lock;

import io.github.tanguygab.arphones.ARPhones;
import io.github.tanguygab.arphones.phone.Phone;

import java.util.UUID;

public class LockSystem {

    private final UUID phone;
    private boolean isLocked;
    private LockMode lockMode;
    private UnlockMode unlockMode;
    private String key;
    private boolean faceRecognition;

    public LockSystem(UUID phone, boolean isLocked, LockMode lockMode, UnlockMode unlockMode, String key, boolean faceRecognition) {
        this.phone = phone;
        this.isLocked = isLocked;
        this.lockMode = lockMode;
        this.unlockMode = unlockMode;
        this.key = key;
        this.faceRecognition = faceRecognition;
    }
    public LockSystem(UUID phone) {
        this(phone,false,LockMode.NONE,UnlockMode.ALWAYS_LOCKED,null,false);
    }


    private void set(String key, Object value) {
        ARPhones.get().dataFile.set("phones."+phone+".lock."+key,value);
    }

    public boolean isLocked() {
        return lockMode != LockMode.NONE && isLocked;
    }
    public void setLocked(boolean locked) {
        if (locked && lockMode == LockMode.NONE) return;
        isLocked = locked;
        set("is-locked",locked);
    }

    public LockMode getLockMode() {
        return lockMode;
    }
    public void setLockMode(LockMode mode) {
        lockMode = mode;
        set("lock-mode",mode+"");
        setKey(mode.getDefaultKey());
    }

    public UnlockMode getUnlockMode() {
        return unlockMode;
    }
    public void setUnlockMode(UnlockMode mode) {
        unlockMode = mode;
        set("unlock-mode",mode+"");
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
        set("key",key);
    }

    public boolean hasFaceRecognition() {
        return faceRecognition;
    }
    public void setFaceRecognition(boolean enabled) {
        faceRecognition = enabled;
        set("face-recognition",enabled);
    }
}
