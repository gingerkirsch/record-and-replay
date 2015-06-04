package edu.ist.symber;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;


public class ArgParserReplayer {
	private final Map<Option, String> argsValues;
	private final static NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(); 

	public ArgParserReplayer()
	{
		argsValues = new EnumMap<Option, String>(Option.class);

		for(Option opt : Option.values())
		{
			argsValues.put(opt, opt.defaultVal);
		}
	}
	
	
	public void loadDefaultsFromFile(String filePath) {
		//** set the default values
		Properties config = new Properties();
		InputStream in = Util.openFile(filePath);

		if(in == null){
			System.err.println("[OREO-Replayer] No config file found. File passed as input: "+filePath);	//** no config file found, fill with the default values
			return;
		}
		else
		{
			System.out.println("[OREO-Replayer] Loading config file: "+filePath);
		}

		try {
			config.load(in);
			for(Option opt : Option.values())
			{
				if(opt.configParamName == null)
					argsValues.put(opt,opt.defaultVal);
				else
					argsValues.put(opt, config.getProperty(opt.configParamName,opt.defaultVal));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void parse(String[] args) {
		int idx = 0;

		if (args.length == 0) {
			throw new IllegalArgumentException(
					"[OREO-Replayer] No parameters introduced. Please indicate the program's main class and the log file as follows: "
							+ Option.MAIN_CLASS
							+ " [main-class-name] [params]  "
							+ Option.LOG_PATH
							+ " [path-to-recorded-log]");
		} 
		
		boolean isMain = false; //** used to parse main class' parameters
		Option mainOpt = null; 	//** used to parse main class' parameters
		while (idx < args.length) {
			Option option = Option.fromString(args[idx]);
			if (option == null) {
				if(isMain) {
					String prev = argsValues.get(mainOpt);
					prev = prev +" " + args[idx];
					argsValues.put(mainOpt,prev);
					idx++;
					continue;
				}
				else {
					throw new IllegalArgumentException("[OREO-Replayer] Unkown option: " + args[idx] + ". Possible options are: " +
							Arrays.asList(Option.values()));
				}
			}
			idx++;
			if (option.isBoolean()) {
				isMain = false;
				argsValues.put(option, "true");
				continue;
			}
			if (idx >= args.length) {
				throw new IllegalArgumentException("expected a value for option " + option);
			}
			if(option == Option.MAIN_CLASS){
				isMain = true;
				mainOpt = option;
			}
			else
				isMain = false;
			
			argsValues.put(option, args[idx++]);
		}
	}
	
	public void validate() {

		if(getValue(Option.MAIN_CLASS)==null || getValue(Option.MAIN_CLASS).isEmpty()) 
		{
			throw new IllegalArgumentException("[OREO-Replayer] No main class introduced. Please indicate the program's main class as follows: "+Option.MAIN_CLASS+" [path-to-main-class] [params]");
		}
		
		if(getValue(Option.LOG_PATH)==null || getValue(Option.LOG_PATH).isEmpty())
		{
			throw new IllegalArgumentException("[OREO-Replayer] No log file introduced. Please indicate the log file as follows: "+Option.LOG_PATH+" [path-to-recorded-log]");
		}
		if(getValueAsNumber(Option.NUM_SHARED) == null)
		{
			throw new IllegalArgumentException("[OREO-Replayer] No number of shared variables introduced. Please indicate the number of shared variables as follows: "+Option.NUM_SHARED+" [num-shared-vars]");
		}
		if(getValueAsNumber(Option.NUM_SYNC) == null)
		{
			throw new IllegalArgumentException("[OREO-Replayer] No number of synchronization variables introduced. Please indicate the number of synchronization variables as follows: "+Option.NUM_SYNC+" [num-sync-vars]");
		}
	}
	
	public String printOptions() {
		return argsValues.toString();
	}

	public final String getValue(Option option) {
		return argsValues.get(option);
	}

	public final boolean getValueAsBoolean(Option option) {
		return Boolean.parseBoolean(argsValues.get(option));
	}

	public final Number getValueAsNumber(Option option) {
		try {
			return NUMBER_FORMAT.parse(argsValues.get(option));
		} catch(Exception e) {
			return null;
		}
	}

	public int getValueAsInt(Option option) {
		Number number = getValueAsNumber(option);
		if (number == null) {
			return -1;
		}
		return number.intValue();
	}

	public final boolean hasOption(Option option) {
		return argsValues.containsKey(option);
	}


	public final String toString() {
		return "\t" + argsValues;
	}
	
	/*
	 * Class Option - defines all possible input options for this component
	 */
	public static enum Option {	
		//** NAME_OPTION( flag, is only flag?, description, config parameter name, default value) 
		MAIN_CLASS("--main-class",false,"usage: --main-class [path-to-main-class] [parameters] | Program's main class, previoulsy instrumented, along with the corresponding parameters.","mainClass",null),
		LOG_PATH("-log",false,"usage: -log [path-to-recorded-log] | Path to the log recorded at runtime.","log",null),
		NUM_SHARED("--num-shared",false,"usage: --num-shared [num-of-shared-vars] | Number of shared variables of the program.","numShared",null),
		NUM_SYNC("--num-sync",false,"usage: --num-sync [num-of-sync-vars] | Number of synchronization variables of the program.","numSync",null),
		;

		private final String arg;
		private final boolean isBoolean;
		private final String description;
		private final String configParamName;
		private final String defaultVal;

		Option(String arg, boolean isBoolean, String description, String configParamName, String defaultVal) {
			if (arg == null) {
				throw new IllegalArgumentException("Null not allowed in Option name");
			}
			this.arg = arg;
			this.isBoolean = isBoolean;
			this.description = description;
			this.configParamName = configParamName;
			this.defaultVal = defaultVal;
		}

		public final String getArgName() {
			return arg;
		}

		public final boolean isBoolean() {
			return isBoolean;
		}

		public final String toString() {
			return arg;
		}

		public final String getDescription(){
			return this.description;
		}

		public static Option fromString(String optionName) {
			for (Option option : values()) {
				if (option.getArgName().equalsIgnoreCase(optionName)) {
					return option;
				}
			}
			return null;
		}
	}
	
}
