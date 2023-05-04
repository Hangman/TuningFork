package de.pottgames.tuningfork;

public class AlEvent {
    private final int  eventType;
    private final int  object;
    private final int  param;
    private final int  length;
    private final long message;
    private final long userParam;


    public AlEvent(int eventType, int object, int param, int length, long message, long userParam) {
        this.eventType = eventType;
        this.object = object;
        this.param = param;
        this.length = length;
        this.message = message;
        this.userParam = userParam;
    }


    public int getEventType() {
        return this.eventType;
    }


    public int getObject() {
        return this.object;
    }


    public int getParam() {
        return this.param;
    }


    public int getLength() {
        return this.length;
    }


    public long getMessage() {
        return this.message;
    }


    public long getUserParam() {
        return this.userParam;
    }

}
