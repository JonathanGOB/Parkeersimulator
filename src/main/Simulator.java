package main;

import java.awt.Dimension;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import controller.Controller;
import logic.*;
import view.*;

import javax.swing.JProgressBar;

public class Simulator implements Runnable {

	private static final String AD_HOC = "1";
	private static final String PASS = "2";
	private static final String RES = "3";

	private CarQueue entranceCarQueue;
	private CarQueue entranceAbQueue;
	private CarQueue entranceResQueue;
	private CarQueue paymentCarQueue;
	private CarQueue exitCarQueue;
	public SimulatorView simulatorView;
	private Thread sim;

	private int day = 0; // gives the day the simulator is on
	private int hour = 0; // gives the hour the simulator is on
	private int minute = 0; // gives the minute the simulator is on


	public static int tickPause = 100;

	int weekDayArrivals = 100; // average number of arriving cars per hour
	int weekendArrivals = 50; // average number of arriving cars per hour
	int weekDayPassArrivals = 100; // average number of arriving cars per hour
	int weekendPassArrivals = 50; // average number of arriving cars per hour
	int weekDayReservationArrivals = 100; // average number of reservations cars per hour
	int weekendReservationArrivals = 12; // average number of reservations cars per hour

	int enterSpeed = 2; // number of cars that can enter per minute
	int paymentSpeed = 3; // number of cars that can pay per minute
	int exitSpeed = 2; // number of cars that can leave per minute

	public static boolean started = true;// says if the application is started or not

	public Simulator() {
		entranceCarQueue = new CarQueue();
		entranceAbQueue = new CarQueue();
		entranceResQueue = new CarQueue();
		paymentCarQueue = new CarQueue();
		exitCarQueue = new CarQueue();
		simulatorView = new SimulatorView(3, 6, 30);
		Controller controller = new Controller(simulatorView);
		simulatorView.pack();
		settingSimulatorView();

	}

	public void start() {
		if (sim == null) {
			sim = new Thread(this, "simulator");
			sim.start();
		}
	}

	// setups the simulatorView
	public void settingSimulatorView() {
		simulatorView.setSize(1980, 1080);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		simulatorView.setLocation(dim.width / 2 - simulatorView.getSize().width / 2,
				dim.height / 2 - simulatorView.getSize().height / 2);
		simulatorView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// runs the program in a while loop
	public void run() {

		while (started == true) {
			tick();
			while (started == false) {
				System.out.println("stopped");
				if(Model.addHour == true) {
					for(int i = 0; i < 60; i++) {
						tick();
					}
					Model.addHour = false;
					tickPause = Model.TempTickPause;
				}
				if(Model.addMinute == true) {
					tick();
					Model.addMinute = false;
					tickPause = Model.TempTickPause;
				}
			}

		}

	}

	// updates the clock and handles the Simulator
	private void tick() {
		SimulatorView.clock.setText("  Minutes " + String.valueOf(minute) + ":" + " Hours " + String.valueOf(hour) + ":"
				+ " Days " + String.valueOf(day));
		ChangeValues();
		advanceTime();
		handleExit();
		updateViews();
		updateCharts();
		// Pause.
		try {
			Thread.sleep(tickPause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handleEntrance();

	}

	// advances the time when tick is used
	private void advanceTime() {
		// Advance the time by one minute.
		minute++;
		while (minute > 59) {
			minute -= 60;
			hour++;
		}
		while (hour > 23) {
			hour -= 24;
			day++;
		}
		while (day > 6) {
			day -= 7;
		}

	}
	
	private void ChangeValues() {
		if (day == 4 && hour > 16 && hour < 23){
			weekDayArrivals = 500;
			weekDayPassArrivals = 500;
			weekDayReservationArrivals = 500;
			//weekend
			weekendPassArrivals = 50; 
			weekendArrivals = 50; 
			weekendReservationArrivals = 30;
		}
		
		else if (day == 5 && hour > 18) {
			weekDayArrivals = 500;
			weekDayPassArrivals = 500;
			weekDayReservationArrivals = 500;
			//weekend
			weekendPassArrivals = 50; 
			weekendArrivals = 50; 
			weekendReservationArrivals = 30;
		}
		
		else if (day == 6 && hour > 18) {
			weekDayArrivals = 100;
			weekDayPassArrivals = 100;
			weekDayReservationArrivals = 4;
			//weekend
			weekendPassArrivals = 500;
			weekDayReservationArrivals = 500;
			weekendReservationArrivals = 500;
		}
		
		else if (day == 7 && hour > 12 && hour < 17) {
			weekDayArrivals = 100;
			weekDayPassArrivals = 100;
			weekDayReservationArrivals = 4;
			//weekend
			weekendPassArrivals = 500;
			weekendArrivals = 500;
			weekendReservationArrivals = 500;
		}
		
		else {
			weekDayArrivals = 100; 
			weekendArrivals = 50; 
			weekDayPassArrivals = 100;
			weekendPassArrivals = 50; 
			weekDayReservationArrivals = 50;
			weekendReservationArrivals = 30;

		}
	}
	
	private void updateCharts() {
		int length = SimulatorView.ArrivalHistogram.size();
		int position = 0;
		if (minute == 1) {
			/*
			 * for(int i = 0; i < length; i++) { if(SimulatorView.ArrivalHistogram.get(i) !=
			 * 0) { position++; } }
			 */
			SimulatorView.ArrivalHistogram.add(SimulatorView.ArrivalCurrent);
			SimulatorView.ArrivalCurrent = 0;

		}
		
		if(hour == 0 && minute == 0) {
			SimulatorView.Profit.add(SimulatorView.Profitday);
			SimulatorView.Profitday = 0;
		}

	}

	private void handleEntrance() {
		carsArriving();
		carsEntering(entranceAbQueue);
		carsEntering(entranceResQueue);
		carsEntering(entranceCarQueue);
	}

	private void handleExit() {
		carsReadyToLeave();
		carsPaying();
		carsLeaving();
	}

	private void updateViews() {
		simulatorView.tick();
		// Update the car park view.
		simulatorView.updateView();
	}

	private void carsArriving() {
		int numberOfCars = getNumberOfCars(weekDayArrivals, weekendArrivals);
		addArrivingCars(numberOfCars, AD_HOC);
		numberOfCars = getNumberOfCars(weekDayPassArrivals, weekendPassArrivals);
		addArrivingCars(numberOfCars, PASS);
		numberOfCars = getNumberOfCars(weekDayReservationArrivals, weekendReservationArrivals);
		addArrivingCars(numberOfCars, RES);
	}

	private void carsEntering(CarQueue queue) {
		int i = 0;
		// Remove car from the front of the queue and assign to a parking space.
		while (queue.carsInQueue() > 0 && simulatorView.getNumberOfOpenSpots() > 0 && i < enterSpeed) {
			Car car = queue.removeCar();
			Location freeLocation = simulatorView.getFirstFreeLocation(car);
			simulatorView.setCarAt(freeLocation, car);
			i++;

			if(car.gethasReserved() == true && car.getHasToPay() == true && freeLocation != null) {
				SimulatorView.YellowQueue--;
			}
			if(car.gethasReserved() == false && car.getHasToPay() == true && freeLocation != null) {
				SimulatorView.RedQueue--;
			}
			if(car.gethasReserved() == false && car.getHasToPay() == false && freeLocation != null) {
				SimulatorView.BlueQueue--;
			}

		}

	}

	private void carsReadyToLeave() {
		// Add leaving cars to the payment queue.
		Car car = simulatorView.getFirstLeavingCar();
		while (car != null) {
			if (car.getHasToPay()) {
				car.setIsPaying(true);
				paymentCarQueue.addCar(car);
			} else {
				carLeavesSpot(car);
			}
			car = simulatorView.getFirstLeavingCar();
		}
	}




	private void carsPaying() {
		// Let cars pay.
		int i = 0;
		while (paymentCarQueue.carsInQueue() > 0 && i < paymentSpeed) {
			Car car = paymentCarQueue.removeCar();
			// TODO Handle payment.
			carLeavesSpot(car);
			i++;
			if(car.getHasToPay() == true && car.gethasReserved() == true) {
				double carpay = car.getTotalHours() * 3.00;
				SimulatorView.Profitday = SimulatorView.Profitday + carpay;
			}
			
			if(car.getHasToPay() == true && car.gethasReserved() == false) {
				double carpay = car.getTotalHours() * 3.50;
				SimulatorView.Profitday = SimulatorView.Profitday + carpay;
			}
		}
	}

	private void carsLeaving() {
		// Let cars leave.
		int i = 0;
		while (exitCarQueue.carsInQueue() > 0 && i < exitSpeed) {
			exitCarQueue.removeCar();
			i++;
		}
	}

	private int getNumberOfCars(int weekDay, int weekend) {
		Random random = new Random();

		// Get the average number of cars that arrive per hour.
		int averageNumberOfCarsPerHour = day < 5 ? weekDay : weekend;

		// Calculate the number of cars that arrive this minute.
		double standardDeviation = averageNumberOfCarsPerHour * 0.3;
		double numberOfCarsPerHour = averageNumberOfCarsPerHour + random.nextGaussian() * standardDeviation;
		return (int) Math.round(numberOfCarsPerHour / 60);
	}

	private void addArrivingCars(int numberOfCars, String type) {
		// Add the cars to the back of the queue.
		switch (type) {
			case AD_HOC:
				for (int i = 0; i < numberOfCars; i++) {
					if (entranceCarQueue.carsInQueue() < 21) {
						entranceCarQueue.addCar(new AdHocCar());
					SimulatorView.RedQueue++;
					SimulatorView.ArrivalCurrent++;
					}
			
					
				}
				break;
						
				case PASS:
					for (int i = 0; i < numberOfCars; i++) {
						if (entranceAbQueue.carsInQueue() < 11) {
							entranceAbQueue.addCar(new ParkingPassCar());
							SimulatorView.BlueQueue++;
							SimulatorView.ArrivalCurrent++;
						}
				
				    
						
					
					}
					break;
				
				
				case RES:
					for (int i = 0; i < numberOfCars; i++) {
						if (entranceResQueue.carsInQueue() < 6) {
							entranceResQueue.addCar(new ReservationCar());
							SimulatorView.YellowQueue++;
							SimulatorView.ArrivalCurrent++;
						}
				
					
					}
					break;
		}
	}
				
			
			
	


	private void carLeavesSpot(Car car) {
		simulatorView.removeCarAt(car.getLocation());
		exitCarQueue.addCar(car);
	}

	public boolean isStarted() {
		return started;
	}

	public boolean setStarted(boolean started) {
		this.started = started;
		return started;
	}

}