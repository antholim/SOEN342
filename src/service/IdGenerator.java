package service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {
    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
    private static final Random RAND = new SecureRandom();
    private static final AtomicLong TICKET_SEQ = new AtomicLong(100000000L);

    private IdGenerator() {}

    public static String newTripId(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ALPHANUM.charAt(RAND.nextInt(ALPHANUM.length())));
        return sb.toString();
    }

    public static long nextTicket() {
        return TICKET_SEQ.getAndIncrement();
    }
}
