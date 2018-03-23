package ua.bikeshow;

/**
 * Created by ricar on 22/03/2018.
 */

public class Session {

    public Double velMed;
    public Double bpmMed;
    public int time;

    public Session(){

    }

    public Session(Double velMed, Double bpmMed, int time){
        this.velMed=velMed;
        this.bpmMed=bpmMed;
        this.time=time;
    }

}
