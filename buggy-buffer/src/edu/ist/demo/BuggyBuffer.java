package edu.ist.demo;
import java.util.Vector;


public class BuggyBuffer {

	static Vector<Integer> buffer = new Vector<Integer>();
	static int pos = 0;
	static int size = 10;
	static int countR = 0;
	static int countW = 0;
	
	static class Writer extends Thread
	{
		public void run()
		{
			try{
			int val = (int) Thread.currentThread().getId();
			if(pos < size)
			{
				buffer.add(pos, val);
				System.out.println("Writer[T1] buffer["+pos+"] = "+val);
				pos++;
				System.out.println("Writer[T1] pos++");
				sleep(10);
				countW++;
				System.out.println("Writer[T1] countW++");
			}
			}
			catch(Exception e){}
		}
	}
	
	static class Reader extends Thread
	{
		public void run()
		{
			try{
			int val = (int) Thread.currentThread().getId();
			if(pos < size)
			{
				buffer.add(pos, val);
				System.out.println("Reader[T3] buffer["+pos+"] = "+val);
				pos++;
				System.out.println("Reader[T3] pos++");
				//sleep(10);
				countR++;
				System.out.println("Reader[T3] countW++");
			}
			}
			catch(Exception e){}
		}
	}
	
	static class Cleaner extends Thread
	{
		public void run()
		{
			try{
				pos = 0;
				System.out.println("Cleaner[T2] pos = 0");
				countR = 0;
				System.out.println("Cleaner[T2] countR = 0");
				countW = 0;
				System.out.println("Cleaner[T2] countW = 0");
				buffer.clear();
			}
			catch(Exception e){}
		}
	}
	
	
	public static void main(String[] args)
	{
		Writer w1 = new Writer();
		Cleaner w2 = new Cleaner();
		Reader w3 = new Reader();
		buffer.add(1);
		try {
			w1.start();
			w2.start();
			w3.start();
			w3.join();
			w1.join();
			w2.join();
			
			if(pos != (countR+countW))
				System.out.println("Bug: pos "+pos+"\tcountW "+countW);
			else
				System.out.println("None: pos "+pos+"\tcountW "+countW);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
}
