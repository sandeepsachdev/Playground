package au.com.sydneytv.guide.model;

import java.util.List;

public class Channel {

    private final String number;
    private final String name;
    private final String network;
    private final List<Program> programs;

    public Channel(String number, String name, String network, List<Program> programs) {
        this.number = number;
        this.name = name;
        this.network = network;
        this.programs = programs;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getNetwork() {
        return network;
    }

    public List<Program> getPrograms() {
        return programs;
    }
}
