package edu.ist.symber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;


public class Util implements Cloneable{

	public static String getTracesDirectory() 
	{
		String tempdir = System.getProperty("user.dir");
		tempdir=tempdir.replace("replayer", "traces");
		tempdir=tempdir.replace("analyzer", "traces"); //** this is needed when a process Replayer is forked from cortex-analyzer
		
		if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
			tempdir = tempdir + System.getProperty("file.separator");
		}
		tempdir = tempdir+Parameters.OUTPUT_FOLDER+System.getProperty("file.separator")+"feedback"+System.getProperty("file.separator");
		
		File tempFile = new File(tempdir);
		if(!(tempFile.exists()))
			tempFile.mkdir();
			
		tempdir = tempdir+System.getProperty("file.separator");
		return tempdir;
	}
	
	 private static double roundThreeDecimals(double d) {
			DecimalFormat twoDForm = new DecimalFormat("#.###");
			return Double.valueOf(twoDForm.format(d));
		}
	
	 /**
     * Computes the elapsed time (in seconds) between to instants (in nanosecs).
     */
    public static double getElaspedTime(long start, long end)
    {
    	return roundThreeDecimals(((double)(end - start)/1000000000));
    }
    
    
    public static InputStream openFile(String filename)
	{
		InputStream ret = null;
		try {
			ret = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			System.err.println(">> File not found.");
			e.printStackTrace();
		}
		return ret;
	}
    
    public static Object loadObject(ObjectInputStream in)
    {
    	Object o =null;
    	try
    	{
    		o = in.readObject();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}finally
    	{
    		try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return o;
    	}
    }
}
