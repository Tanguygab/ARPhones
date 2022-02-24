package io.github.tanguygab.arphones.phone;

public enum PhonePage {

    MAIN,
    CONTACTS,
    PLAYERS,
    CONTACT_INFO,
    KEYCARDS,
    LOCK_SCREEN;

    PhonePage() {}

    public static PhonePage pageFromStr(String str) {
        for (PhonePage page : values()) {
            if (page.toString().equalsIgnoreCase(str))
                return page;
        }
        return null;
    }

}
