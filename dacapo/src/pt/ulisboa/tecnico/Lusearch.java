package pt.ulisboa.tecnico;

import java.lang.reflect.Method;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/* Uses lucene to do a text search of keywords over a corpus of data 
 * comprising the works of Shakespeare and the King James Bible */
public class Lusearch {
	public static void main(String[] args) {
		try 
		{
			JarFile jarFile = new JarFile("lib//dacapo-9.12-bach.jar");
			Manifest manifest = jarFile.getManifest(); // warning: can be null
			Attributes attributes = manifest.getMainAttributes();
		
			Class<?> c = Class.forName(attributes
					.getValue(Attributes.Name.MAIN_CLASS));
			Class[] argTypes = new Class[] { String[].class };
			String[] myString = {"lusearch"};
			Method main = c.getDeclaredMethod("main", argTypes);
			String[] mainArgs = myString;//Arrays.copyOfRange(args, 0, args.length);
			main.invoke(null, (Object)mainArgs);
		} catch (Exception x) {
		    x.printStackTrace();
		}
	}
}
