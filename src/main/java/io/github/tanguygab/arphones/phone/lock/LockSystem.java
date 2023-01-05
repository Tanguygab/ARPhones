package io.github.tanguygab.arphones.phone.lock;

public class LockSystem {

    private boolean isLocked;

    private LockMode lockMode;
    private UnlockMode unlockMode;
    private String key;
    private boolean faceRecognition;

    public LockSystem(boolean isLocked, LockMode lockMode, UnlockMode unlockMode, String key, boolean faceRecognition) {
        this.isLocked = isLocked;
        this.lockMode = lockMode;
        this.unlockMode = unlockMode;
        this.key = key;
        this.faceRecognition = faceRecognition;
    }
    public LockSystem() {
        this(false,LockMode.NONE,UnlockMode.ALWAYS_LOCKED,null,false);
    }


    public boolean isLocked() {
        return lockMode != LockMode.NONE && isLocked;
    }
    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public LockMode getLockMode() {
        return lockMode;
    }
    public void setLockMode(LockMode mode) {
        lockMode = mode;
    }

    public UnlockMode getUnlockMode() {
        return unlockMode;
    }
    public void setUnlockMode(UnlockMode mode) {
        unlockMode = mode;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public boolean hasFaceRecognition() {
        return faceRecognition;
    }
    public void setFaceRecognition(boolean enabled) {
        faceRecognition = enabled;
    }
}
