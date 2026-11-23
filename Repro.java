import static java.lang.foreign.ValueLayout.JAVA_FLOAT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Repro {
    static final int N = 1024;
    static final float[] ARRAY = new float[N];
    static final MemorySegment NATIVE = Arena.global().allocate(4L * N, 4);
    static final VarHandle ARRAY_VH = MethodHandles.arrayElementVarHandle(float[].class);

    static float plainArray() {
        float[] a = ARRAY;
        float acc = 0f;
        for (int i = 0; i < N; i++) acc += a[i];
        return acc;
    }

    static float arrayVarHandle() {
        float[] a = ARRAY;
        float acc = 0f;
        for (int i = 0; i < N; i++) acc += (float) ARRAY_VH.get(a, i); // explicit VarHandle: not stored
        return acc;
    }

    static float segmentGetNative() {
        MemorySegment s = NATIVE;
        float acc = 0f;
        for (int i = 0; i < N; i++) acc += s.get(JAVA_FLOAT, 4L * i); // VarHandle inside get(): not stored
        return acc;
    }

    public static void main(String[] args) {
        double total = 0;
        for (int r = 0; r < 30_000; r++) total += plainArray() + arrayVarHandle() + segmentGetNative();
        System.out.println(total);
    }
}
