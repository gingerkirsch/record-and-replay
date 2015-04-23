package edu.ist.symber.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MyMonitorInfer {

	static ArrayList<Integer> path = new ArrayList<Integer>();
	private static String logPath = "/Users/manuelbravo/Documents/eclipseWS/jpf-infer";
	
	public static void logDecision(int decision){
		path.add(decision);
		System.out.print("Path till now: ");
		Iterator<Integer> it= path.iterator();
		while (it.hasNext()){
			System.out.print(it.next()+" ");
		}
		System.out.println();
	}
	public static void writingLog(){
		/*try {

			File file = new File(logPath + "/log.manu.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}else{
				file.delete();
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			Iterator<Integer> it= path.iterator();
			while (it.hasNext()){
				bw.write(it.next()+" ");
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}
