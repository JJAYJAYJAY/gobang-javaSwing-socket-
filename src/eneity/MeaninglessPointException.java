package eneity;

public class MeaninglessPointException extends Exception {
    public MeaninglessPointException() {
        super("此点击位置无效");
    }
}
