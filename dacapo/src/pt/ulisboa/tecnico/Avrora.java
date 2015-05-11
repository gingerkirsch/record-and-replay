package pt.ulisboa.tecnico;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.dacapo.harness.CommandLineArgs;

/* Simulates a number of programs run on a grid of AVR microcontrollers */
public class Avrora {
	public static void main(String[] args) {
		try 
		{
			JarFile jarFile = new JarFile("lib//dacapo-9.12-bach.jar");
			Manifest manifest = jarFile.getManifest(); // warning: can be null
			Attributes attributes = manifest.getMainAttributes();
		
			Class<?> c = Class.forName(attributes
					.getValue(Attributes.Name.MAIN_CLASS));
			Class[] argTypes = new Class[] { String[].class };
			String[] myString = {"avrora"};
			Method main = c.getDeclaredMethod("main", argTypes);
			String[] mainArgs = myString;//Arrays.copyOfRange(args, 0, args.length);
			main.invoke(null, (Object)mainArgs);
		} catch (Exception x) {
		    x.printStackTrace();
		}
	}
}
