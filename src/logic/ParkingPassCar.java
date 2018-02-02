package logic;

import java.util.Random;
import java.awt.*;

public class ParkingPassCar extends Car {
	private static final Color COLOR=Color.blue;
	
	
    public ParkingPassCar() {
    	int i = 0; 
    	
    	if (i < 51) {
    	Random random = new Random();
    	this.sethasReserved(false);
    	int stayMinutes = (int) (15 + random.nextFloat() * 3 * 60);
    	totalHours = Math.round(stayMinutes/60);
        this.setMinutesLeft(stayMinutes);
        this.setHasToPay(false);
        i ++;
    	}
    	
    	
    }
    
    public Color getColor(){
    	return COLOR;
    }
}
