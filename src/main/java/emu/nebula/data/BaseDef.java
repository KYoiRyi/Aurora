package emu.nebula.data;

public abstract class BaseDef implements Comparable<BaseDef> {

    public abstract int getId();

    public void onLoad() {

    }

    @Override
    public int compareTo(BaseDef o) {
        return this.getId() - o.getId();
    }
}
