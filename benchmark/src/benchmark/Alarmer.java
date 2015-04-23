package benchmark;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Alarmer {
	 public Timer alarm;
	 public boolean STOP = false;
	 
	 public Alarmer()
	 {
		 alarm = new Timer();
		 long delay;
         Random r = new Random();
         int t = r.nextInt(4) + 1;         
         delay = (long)(t*100); 
         System.out.println("delay: "+delay);
         try{
        	 System.out.println("Alarm com delay "+delay);
         alarm.schedule(new AlarmTask(), delay); 
         }catch(Exception e){e.printStackTrace();}
        
	 }
	 
	 
	 class AlarmTask extends TimerTask {
		
		 public AlarmTask(){}
		 
		 public void run()
		 {
			 System.out.println("STOP thread!");
			 STOP = true;
			 alarm.cancel();
		 }
	 }
	 
}
