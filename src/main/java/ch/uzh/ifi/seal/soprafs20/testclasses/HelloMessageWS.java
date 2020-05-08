package ch.uzh.ifi.seal.soprafs20.testclasses;

public class HelloMessageWS {
    private String name;

    public HelloMessageWS() {
    }

    public HelloMessageWS(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
