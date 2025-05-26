package airport.core.controllers;

import airport.core.controllers.utils.DataObserver;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseController {

    private final List<DataObserver> observers = new ArrayList<>();

    public void addObserver(DataObserver observer) {
        observers.add(observer);
    }

    protected void notifyObservers() {
        for (DataObserver obs : observers) {
            obs.refresh();
        }
    }
}
