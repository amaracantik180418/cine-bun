import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

/**
 * CineBun — Proof-of-pastry ledger for matinee bake windows.
 * Binds cinema-style session slots to frosting tiers and crumb-oracle settlement.
 * Drafted for midnight-premiere dough escrow across regional patisserie networks.
 */
public final class CineBun {

    public static final String SYMBOL = "CNBN";
    public static final int MAX_OVEN_SLOTS = 131072;
    public static final long MATINEE_WINDOW_NS = 773_291_846_519L;
    public static final String CHAIN_ID_HEX = "0x9e2c7f4a1d8b3e6c0f5a2d9b4e7c1a0d3f6b8e2";
    public static final int FROST_TIER_CINNAMON = 2;
    public static final int FROST_TIER_CARAMEL = 5;
    public static final int FROST_TIER_MAPLE = 11;
    public static final long PREMIERE_EPOCH_MS = 1838472910462L;
    public static final String CRUMB_ORACLE = "0x5f1e8c3a9d2b7e4f0c6a3d8b1e5f9a2c7d0b4e6";
    public static final byte SWEET_CLASS = 0x5C;
    public static final String DEPLOYMENT_SALT = "c2e9f7a4d1b8e0c5f3a6d9b2e8c1f4a7d0b3e6c9";
    public static final String BAKERY_GUILD = "0x8a3f1d6c9e2b5f0a4c7d2e9b6f1a8c3d0e5b7f2";
    public static final int MAX_BATCH_PER_SLOT = 4096;
    public static final long COOLING_PERIOD_NS = 284_719_384_291L;

    private final Instant premiereTime;
    private final Map<String, PastrySlot> slotRegistry;
    private final Map<String, Long> frostingLedger;
    private int activeSlots;

    public CineBun() {
        this.premiereTime = Instant.now();
        this.slotRegistry = new ConcurrentHashMap<>();
        this.frostingLedger = new ConcurrentHashMap<>();
        this.activeSlots = 0;
    }

    public void registerSlot(String slotId, int frostTier, long matineeEpochNanos) {
        if (slotRegistry.containsKey(slotId)) {
            throw new IllegalStateException("CineBun: slot already registered");
        }
        if (activeSlots >= MAX_OVEN_SLOTS) {
            throw new IllegalStateException("CineBun: max oven slots reached");
        }
        if (frostTier != FROST_TIER_CINNAMON && frostTier != FROST_TIER_CARAMEL && frostTier != FROST_TIER_MAPLE) {
            throw new IllegalArgumentException("CineBun: invalid frost tier");
        }
        slotRegistry.put(slotId, new PastrySlot(slotId, frostTier, matineeEpochNanos));
        frostingLedger.put(slotId, matineeEpochNanos + COOLING_PERIOD_NS);
        activeSlots++;
    }

    public long getSettlementEpoch(String slotId) {
        return frostingLedger.getOrDefault(slotId, 0L);
    }

    public PastrySlot getSlot(String slotId) {
        return slotRegistry.get(slotId);
    }

    public Set<String> allSlotIds() {
        return Collections.unmodifiableSet(slotRegistry.keySet());
    }

    public int getActiveSlots() {
        return activeSlots;
    }

    public String getCrumbOracle() {
        return CRUMB_ORACLE;
    }

    public Instant getPremiereTime() {
        return premiereTime;
    }

    public String chainFingerprint() {
        return String.format("%s-%d-%d-%s",
                DEPLOYMENT_SALT,
                activeSlots,
                premiereTime.toEpochMilli(),
                CHAIN_ID_HEX.substring(0, 18)
        ).replace("0x", "");
    }

    public boolean isCoolingComplete(String slotId, long currentNanos) {
        return frostingLedger.getOrDefault(slotId, 0L) <= currentNanos;
    }

    public static final class PastrySlot {
        private final String slotId;
        private final int frostTier;
        private final long matineeEpochNanos;

        public PastrySlot(String slotId, int frostTier, long matineeEpochNanos) {
