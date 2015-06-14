package pl.dp.rasbot.utils;

/**
 * Created by Project4You S.C. on 24.11.14.
 * Author: Dawid Podolak
 * Email: dawidpod1@project4you.pl
 * All rights reserved!
 */
public final class BusProvider {

    private static final MainThreadBus bus = new MainThreadBus();

    public static MainThreadBus getInstance(){
        return bus;
    }
}
