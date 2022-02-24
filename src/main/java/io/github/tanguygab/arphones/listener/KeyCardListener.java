package io.github.tanguygab.arphones.listener;

import io.github.tanguygab.arphones.phone.Phone;
import io.github.tanguygab.arphones.utils.Utils;
import io.github.tanguygab.keycard.events.KeyCardCheckEvent;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class KeyCardListener implements Listener {

    @EventHandler
    public void onScannerUse(KeyCardCheckEvent e) {
        Phone phone = Utils.getPhone(e.getCard(),null);
        if (phone == null) return;
        for (ItemStack card : phone.getKeycards()) {
            if (e.getScanner().canUse(card)) {
                e.setResult(Event.Result.ALLOW);
                return;
            }
        }
    }

}
