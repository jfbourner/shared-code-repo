public class IsoMessageAccessor {

    private static final Map<Integer, IsoType> FIELD_TYPES = Map.ofEntries(
            Map.entry(2,  IsoType.LLVAR),
            Map.entry(3,  IsoType.NUMERIC),
            Map.entry(4,  IsoType.NUMERIC),
            Map.entry(11, IsoType.NUMERIC),
            Map.entry(41, IsoType.ALPHA),
            Map.entry(48, IsoType.LLLVAR)
            // ...
    );

    private final IsoMessage message;

    public IsoMessageAccessor(IsoMessage message) {
        this.message = message;
    }

    public String getString(int field) {
        IsoValue<?> value = message.getField(field);
        if (value == null) return null;
        String raw = value.toString();
        return value.getType() == IsoType.ALPHA ? raw.trim() : raw;
    }

    public void setString(int field, String value) {
        IsoType type = FIELD_TYPES.get(field);
        if (type == null) {
            throw new IllegalArgumentException("No type configured for field " + field);
        }
        int length = type.needsLength() ? value.length() : type.getLength();
        message.setValue(field, value, type, length);
    }

    public IsoMessage getMessage() {
        return message;
    }
}