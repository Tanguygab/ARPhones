package io.github.tanguygab.arphones.phone;

public enum PhonePage {

    MAIN,
    CONTACTS,
    PLAYERS,
    CONTACT_INFO,
    LOCK_SCREEN,
    LOCK_SCREEN_INFO,
    PIN,
    KEYCARDS;

    public static PhonePage pageFromStr(String str) {
        for (PhonePage page : values()) {
            if (page.toString().equalsIgnoreCase(str))
                return page;
        }
        return null;
    }

}
