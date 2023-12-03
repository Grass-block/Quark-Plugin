package org.tbstcraft.quark.util.container;

/**
 * lower the accuracy number(0-1) for a better storage
 *
 * <p>well what is the reason of this?</p>
 *
 * @author GrassBlock2022
 */
public class Double2ByteArray {
    public byte[] array;

    public Double2ByteArray(int size) {
        this.array = new byte[size];
    }

    /**
     * set value in current position.
     * @param index index
     * @param d double value.
     */
    public void set(int index, double d) {
        this.array[index] = (byte) (d * 256 - 128);
    }

    /**
     * get origin data here.
     * @param index index
     * @return the origin double value.
     */
    public double get(int index) {
        return (this.array[index] + 128) / 256f;
    }

    /**
     * set raw array,cover all original data,
     * @param raw data
     */
    public void setArr(double[] raw) {
        for (int i = 0; i < this.array.length; i++) {
            this.set(i, raw[i]);
        }
    }

    /**
     * set array,cover all original data,
     * @param arr data
     */
    public void setData(byte[] arr){
        this.array=arr;
    }

    /**
     * export data (encoded) tro a byte array.
     * @return data
     */
    public byte[] getData(){
        return this.array;
    }
}
