package com.frederikam.piprint.print;

public class ServoMotor {

    /**
     * Time it takes to move into position
     */
    static final int DELAY = 1000;

    private boolean lowered = false;

    public boolean isLowered() {
        return lowered;
    }

    public void setLowered(boolean bool) throws InterruptedException {
        if (bool != lowered) {
            // TODO
            lowered = bool;
            Thread.sleep(DELAY);
        }
    }

    public void reset() throws InterruptedException {
        lowered = false;
        Thread.sleep(DELAY);
    }

}
