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
        return eventType;
    }


    public int getObject() {
        return object;
    }


    public int getParam() {
        return param;
    }


    public int getLength() {
        return length;
    }


    public long getMessage() {
        return message;
    }


    public long getUserParam() {
        return userParam;
    }

}
