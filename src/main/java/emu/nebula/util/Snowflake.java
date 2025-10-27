package emu.nebula.util;

public class Snowflake {
    private static final long EPOCH = 1735689600000L; // Wednesday, January 1, 2025 12:00:00 AM (GMT)
    private static int cachedTimestamp;
    private static byte sequence;

    public synchronized static int newUid() {
        int timestamp = (int) ((System.currentTimeMillis() - EPOCH) / 1000);

        if (cachedTimestamp != timestamp) {
            sequence = 0;
            cachedTimestamp = timestamp;
        } else {
            sequence++;
        }

        return (cachedTimestamp << 4) + sequence;
    }

    public synchronized static int toTimestamp(int snowflake) {
        return (snowflake >> 4) + (int) (EPOCH / 1000);
    }
}
