package io.github.tanguygab.arphones.phone;

public enum PhonePage {

    MAIN,
    CONTACTS,
    PLAYERS,
    CONTACT_INFO,
    LOCK_SCREEN;

    private String arg;
    PhonePage() {}

    public String getArg() {
        return arg;
    }
    public void setArg(String arg) {
        this.arg = arg;
    }

    public static PhonePage pageFromStr(String str) {
        String[] arr = str.split("-");
        for (PhonePage page : values()) {
            if (page.toString().equalsIgnoreCase(arr[0])) continue;
            if (arr.length > 1) page.arg = arr[1];
            return page;
        }
        return null;
    }


    @Override
    public String toString() {
        return super.toString()+(arg == null ? "" : "-"+arg);
    }
}
