package io.quarkus.ts.javaee.gettingstarted.data;

public class SeriousTalk {

    private String before;
    private final Start start;
    private final Middle middle;
    private final End end;
    private final String postscript = null;
    private final Object ignored = new Object();

    public SeriousTalk(String start, String middle, String end) {
        this.start = new Start(new Data(new NestedData(start)));
        this.middle = new Middle(new Data(new NestedData(middle)));
        this.end = new End(new Data(new NestedData(end)));
    }

    public Start getStart() {
        return start;
    }

    public Middle getMiddle() {
        return middle;
    }

    public End getEnd() {
        return end;
    }

    public String getBefore() {
        return before;
    }

    public String getPostscript() {
        return postscript;
    }

    public Object getIgnored() {
        return ignored;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public record Start(Data data) {
    }

    public record Middle(Data data) {
    }

    public record End(Data data) {
    }

    public record Data(NestedData nestedData) {
    }

    public record NestedData(String data) {
    }
}
