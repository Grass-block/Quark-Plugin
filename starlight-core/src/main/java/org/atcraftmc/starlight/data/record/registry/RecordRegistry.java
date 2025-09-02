package org.atcraftmc.starlight.data.record.registry;

public class RecordRegistry {
    private final String id;
    private final RecordField<?>[] fields;

    public RecordRegistry(String id, RecordField<?>... fields) {
        this.id = id;
        this.fields = fields;
    }

    public RecordData record(Object... args) {
        if (args.length != fields.length) {
            throw new IllegalArgumentException("Number of arguments does not match number of fields");
        }

        return new RecordData(this.id, System.currentTimeMillis(), args);
    }

    public static final class A1<T1> extends RecordRegistry {
        public A1(String id, RecordField<T1> f1) {
            super(id, f1);
        }

        public RecordData render(T1 arg) {
            return super.record(arg);
        }
    }

    public static final class A2<T1, T2> extends RecordRegistry {
        public A2(String id, RecordField<T1> f1, RecordField<T2> f2) {
            super(id, f1, f2);
        }

        public RecordData render(T1 arg1, T2 arg2) {
            return super.record(arg1, arg2);
        }
    }

    public static final class A3<T1, T2, T3> extends RecordRegistry {
        public A3(String id, RecordField<T1> f1, RecordField<T2> f2, RecordField<T3> f3) {
            super(id, f1, f2, f3);
        }

        public RecordData render(T1 arg1, T2 arg2, T3 arg3) {
            return super.record(arg1, arg2, arg3);
        }
    }

    public static final class A5<T1, T2, T3, T4, T5> extends RecordRegistry {
        public A5(String id, RecordField<T1> f1, RecordField<T2> f2, RecordField<T3> f3, RecordField<T4> f4, RecordField<T5> f5) {
            super(id, f1, f2, f3, f4, f5);
        }

        public RecordData render(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5) {
            return super.record(arg1, arg2, arg3, arg4, arg5);
        }
    }

    public static final class A6<T1, T2, T3, T4, T5, T6> extends RecordRegistry {
        public A6(String id, RecordField<T1> f1, RecordField<T2> f2, RecordField<T3> f3, RecordField<T4> f4, RecordField<T5> f5, RecordField<T6> f6) {
            super(id, f1, f2, f3, f4, f5, f6);
        }

        public RecordData render(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6) {
            return super.record(arg1, arg2, arg3, arg4, arg5, arg6);
        }
    }
}
