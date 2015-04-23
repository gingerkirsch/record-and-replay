package edu.ist.symber;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Main 
{
	private static String trans_classname = "edu.ist.symber.transformer.SymberTransform";

	public static void main(String[] args) {
		long tStart = System.currentTimeMillis();
		if(args.length==0)
		{
			System.err.println("please specify the main class ... ");
		}
		else 
		{
			transform(args);
		}
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println("Overall time of transforming: " + elapsedSeconds + "sec");
	}
	private static void transform(String[] args) 
	{	
		run(args);
	}
	private static void run(String[] args)
	{
		try 
		{
			Class<?> c = Class.forName(trans_classname);
		    Class[] argTypes = new Class[] { String[].class };
		    Method main = c.getDeclaredMethod("main", argTypes);
		    String[] mainArgs = Arrays.copyOfRange(args, 0, args.length);
		    main.invoke(null, (Object)mainArgs);
			} catch (Exception x) {
			    x.printStackTrace();
			}
	}

}
