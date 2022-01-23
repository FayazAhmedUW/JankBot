package FayazJank;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;
import java.awt.color.*;
import java.util.*;
import java.lang.*;

public class JankBot extends AdvancedRobot {
	public boolean ramMode = false;
	public double enemyEnergy = 100.0;
	public double moveDirection = 1;
	private double wallDist = 100;
	
	public void run() {
		setColors(Color.magenta,Color.orange,Color.green);
		setScanColor(Color.red);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		turnRadarRight(Double.POSITIVE_INFINITY);
		
		
		// Robot main loop
		while(true) {
			
			while(ramMode){
				setAhead(Double.POSITIVE_INFINITY);
				scan();
			}
			while(!ramMode){
				if(moveDirection == 1){
					setAhead(100);
				}
				else{
					setBack(100);
				}
				/*
				double moveSwitch;
				if(getTime() % 25 == 0){
					moveSwitch = Math.random();
					if(moveSwitch > 0.5){
						moveDirection = -1 * moveDirection;
					}
				}
				if((getX() < wallDist) || (getX() > getBattleFieldWidth() - wallDist) || (getY() < wallDist) || (getY() > getBattleFieldHeight() - wallDist)){
					moveDirection = -1 * moveDirection;
				}
				*/
				
				scan();
			}
			
		}
	}

	public void onScannedRobot(ScannedRobotEvent e){
		
		//Constantly centres radar on opponent
		double radarTurn = getHeading() + e.getBearing() - getRadarHeading();
		//Makes sure -180 < radarTurn < 180, and that radar beam narrows in on opponent
		setTurnRadarRight(1.9 * Utils.normalRelativeAngleDegrees(radarTurn));
		
		double gunTurn = getHeading() + e.getBearing() - getGunHeading();
		setTurnGunRight(Utils.normalRelativeAngleDegrees(gunTurn));
		
		double bodyTurn;
		
		if(!(ramMode)){
			bodyTurn = e.getBearing() + 90 - getHeading();
			setTurnRight(Utils.normalRelativeAngleDegrees(bodyTurn));
			execute();
			
			//if(!(Utils.normalRelativeAngleDegrees(e.getBearing()) == Utils.normalRelativeAngleDegrees(e.getHeading()+180))){
				double shotTurn = predictiveTargeting(e);
				
				if(shotTurn<0){
					turnGunLeft(shotTurn);
				}
				else{
					turnGunRight(shotTurn);
				}
				
				fire(2);
			//}
			
		}
		else{
			bodyTurn = Utils.normalRelativeAngleDegrees(e.getBearing() - getHeading());
			if(bodyTurn < 0){
				turnLeft(-1* bodyTurn);
			}
			else{
				turnRight(bodyTurn);
			}
		}
		
		
		//Keeps track of opponent health
		enemyEnergy = e.getEnergy();	
	}
	
	public double predictiveTargeting(ScannedRobotEvent e){
		double bulletPower = 2; //2 is a placeholder
		double bulletVelocity = 20 - 3 * bulletPower;
		double timeToOpp = e.getDistance()/bulletVelocity;
		
		double oppDistTravelled = e.getVelocity() * timeToOpp;
		double oppAngle = e.getHeading() + 90;
		double ODTy = Math.sin(e.getHeading()) * oppDistTravelled;
		double ODTx = Math.cos(e.getHeading()) * oppDistTravelled;
		
		double oppDistFinal = Math.pow(e.getDistance(), 2) + Math.pow(oppDistTravelled, 2) - 2*e.getDistance()*oppDistTravelled*Math.cos(oppAngle);
		double oppAngleFinal = Math.asin(oppDistTravelled * (Math.sin(oppAngle)/oppDistFinal));
		
		double gunTurn = Utils.normalRelativeAngleDegrees(oppAngleFinal);
		
		return gunTurn;
		
	}
	
	public void onHitRobot(HitRobotEvent e){
		if(e.getEnergy() < getEnergy()){
			ramMode = true;
		}
		if(ramMode){
			fire(3);
		}
		else{
			double bodyTurn = e.getBearing() + 90;
			turnRight(Utils.normalRelativeAngleDegrees(bodyTurn));
			ahead(200);
		}
	}
}
