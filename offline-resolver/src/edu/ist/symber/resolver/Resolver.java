package edu.ist.symber.resolver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.ist.symber.resolver.Parameters;
import edu.ist.symber.resolver.util.WriteEventComparator;
import edu.ist.symber.common.Event;
import edu.ist.symber.common.EventType;
import edu.ist.symber.common.Pair;

public class Resolver {
	private static Z3Connector z3 = new Z3Connector();
	//private static final String INPUT_DIR = "c:\\Users\\ASUS\\Desktop\\jars\\logs\\";
	private static final String INPUT_DIR = "logs";
	//private static final String INPUT_DIR = "D:\\record-and-replay\\symber-recorder\\logs";
	private static final String INPUT_FILE = "\\log";
	private static final String INPUT_EXT = ".json";
	private static final String OUTPUT_DIR = "output";
	//private static final String OUTPUT_DIR = "D:\\record-and-replay\\symber-replayer\\logs";
	private static final String OUTPUT_FILE = "schedule";
	private static final String OUTPUT_EXT = ".json";
	private static final String SOLUTION_DIR = "z3"; 
	private static final String SOLUTION_PATH = "z3Solution.txt";
	private static Map<Integer, ArrayList<Event>> logs = new HashMap<Integer, ArrayList<Event>>();
	private static Map<Integer, List<Event>> readDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> writeDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> lockDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> unlockDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> startDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> exitDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> forkDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> joinDomain = new HashMap<Integer, List<Event>>();
	
	private static Map<Pair<Integer, Object>, List<Event>> writeSetValue = new HashMap<Pair<Integer, Object>, List<Event>>();
	

	public static void main(String[] args) {
		if(args.length==0)
		{
			System.err.println("please specify: --threads=<>... ");
		}
		else 
		{
			
			for(int i = 0; i < args.length; i++){
				if(args[i].contains("--threads")){ 
					int numTh = Integer.valueOf(args[i].substring(args[i].indexOf("=")+1));
					Parameters.THREADS_AMOUNT = Integer.valueOf(numTh);
				}
			}
		}
		System.out.println("Parsing logs..");
		for (int i = 0; i <= Parameters.THREADS_AMOUNT; i++) {
			logs.put(i, parseLogs(INPUT_DIR + INPUT_FILE + i + INPUT_EXT));
		}
		initStructuresForAnalysis();
		//visualiseForkJoinMemoryConstraints(1);
		//visualiseReadOrderMemoryConstraints(1);
		//visualiseWriteOrderMemoryConstraints(1);


		z3.writeLineZ3("(set-option :produce-unsat-cores true)\n");
		long startConstraints, endConstraints, startSolve, endSolve;
		startConstraints = System.nanoTime(); 
		System.out.println("Create Memory Order Constraints..");
		createMemoryOrderConstraints();
		try {
			System.out.println("Create Read Write Constraints..");
			createrReadWriteConstraints();
		} catch (NoMatchFound e) {
			e.printStackTrace();
		}
		System.out.println("Create Write Versioning Constraints..");
		createWriteVersioningConstraints();
		System.out.println("Create Thread Constraints..");
		createThreadConstraints();
		endConstraints = System.nanoTime(); //** end timestamp
		double timeConstraints = (((double)(endConstraints - startConstraints)/1000000000));
		System.out.println("Finding solution with Z3....");
		startSolve = System.nanoTime();  
		z3.solve();
		System.out.println("Solution found");
		endSolve = System.nanoTime(); //** end timestamp
		double timeSolve = (((double)(endSolve - startSolve)/1000000000));
		z3.printModel();
		z3.writeLineZ3("(reset)\n");
		File folder = new File(SOLUTION_DIR);
		boolean fold = folder.mkdirs();
		produceLogForReplayer(SOLUTION_DIR + File.separator + SOLUTION_PATH);
		Writer writer;
		try {
			writer = new BufferedWriter(new FileWriter(
					"resolver-constraints-time.txt", true));
			writer.append(String.valueOf(timeConstraints));// + "\t");
			writer.append("\r\n");
			System.out.println("BUILDING CONSTRAINTS TIME: "+timeConstraints+"s");
			writer.close();// 
			writer = new BufferedWriter(new FileWriter("resolver-solution-time.txt", true));
			writer.append(String.valueOf(timeSolve));
			System.out.println("SOLVING TIME: "+timeSolve+"s");
			writer.append("\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void produceLogForReplayer(String file) {
		
		System.out.println();
		
		Map<Integer, Pair<String, String>> order = parseZ3results(file);
		Map<String, List<String>> schedule = new TreeMap<String, List<String>>();
		for (Entry <Integer, Pair<String, String>> entry : order.entrySet()){
			String fieldId = entry.getValue().getFirst();
			String threadId = entry.getValue().getSecond();
			if (!schedule.containsKey(fieldId)){
				List<String> accessList = new ArrayList<String>();
				accessList.add(threadId);
				schedule.put(fieldId, accessList);
			} else {
				schedule.get(fieldId).add(threadId);
			}
		}
		try {
			postScheduleJSON(schedule);
			for (String field : schedule.keySet()){
				System.out.println("Access vector for field " + field);
				System.out.println(schedule.get(field));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void postScheduleJSON(Map<String, List<String>> schedule) throws FileNotFoundException {
		File file = new File(OUTPUT_DIR);
		file.mkdirs();
		PrintWriter printWriter = new PrintWriter(OUTPUT_DIR + File.separator
				+ OUTPUT_FILE + OUTPUT_EXT);
		JSONArray jsList = new JSONArray();
		//ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> jsThreads = new ArrayList<String>();
		JSONObject jsObj = new JSONObject();
		Set s = schedule.entrySet();
		Iterator it = s.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			jsObj = new JSONObject();
			jsThreads = new JSONArray();
			jsObj.put("fieldId", entry.getKey());
			for (String thread : (List<String>) entry.getValue()) {
				jsThreads.add(thread);
			}
			jsObj.put("events", jsThreads);
			jsList.add(jsObj);
		}
		//System.out.println(jsList);
		JSONValue.toJSONString(jsList);
		try {
			jsList.writeJSONString(printWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		printWriter.close();
	}

	private static Map<Integer, Pair<String, String>> parseZ3results(String solution) {
		Map<Integer, Pair<String, String>> result = new TreeMap<Integer, Pair<String, String>>();
		StringBuilder sb = new StringBuilder();
		Scanner scanner = null;
		try {
			scanner = new Scanner(Paths.get(solution));
			scanner.useDelimiter(System.getProperty("line.separator"));
			while (scanner.hasNext()) {
				sb.append(scanner.next());
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		final Pattern rw = Pattern.compile("(\\(define-fun\\sO(R|W)-field_[0-9]+-v[0-9]+-T[0-9]+_[0-9]+@.+\\n\\s+[0-9]+\\))");
		final Pattern lu = Pattern.compile("(\\(define-fun\\sO(L|U)-monitor_[0-9]+-v[0-9]+-T[0-9]+_[0-9]+.+\\n\\s+[0-9]+\\))");
		//final Pattern se = Pattern.compile("(\\(define-fun\\sO-(START|EXIT)+-T[0-9]+_[0-9]+\\))");
		//final Pattern fj = Pattern.compile("(\\(define-fun\\sO-(FORK|JOIN)+-T[0-9]+-T[0-9]+_[0-9]+\\))");
		final Matcher m1 = rw.matcher(sb.toString());
		final Matcher m2 = lu.matcher(sb.toString());
		while (m1.find()) {
			Pair<Integer, Pair<String, String>> pair = parseDefineFunEntry(m1.group());
			result.put(pair.getFirst(), pair.getSecond());
		}
		while (m2.find()){
			Pair<Integer, Pair<String, String>> pair = parseDefineFunEntry(m2.group());
			result.put(pair.getFirst(), pair.getSecond());
		}
		return result;
	}

	private static Pair<Integer, Pair<String, String>> parseDefineFunEntry(String source) {
		final Pattern position = Pattern.compile("\\s+[0-9]+");
		final Pattern field = Pattern.compile("field_[0-9]+");
		final Pattern monitor = Pattern.compile("monitor_[0-9]+");
		final Pattern thread = Pattern.compile("-T[0-9]+");
		final Matcher pm = position.matcher(source);
		final Matcher fm = field.matcher(source);
		final Matcher mm = monitor.matcher(source);
		final Matcher tm = thread.matcher(source);
		String positionString = "";
		String fieldString = "";
		String monitorString = "";
		String threadString = "";
		boolean isLock = false;
		if (pm.find()) positionString = pm.group();
		if (fm.find()){
			fieldString = fm.group();
			isLock = false;
		} else if (mm.find()) {
			monitorString = mm.group();
			isLock = true;
		}
		if (tm.find()) threadString = tm.group();
		try {
			Integer positionInt = Integer.valueOf(positionString.trim().substring(0));
			//System.out.println(positionInt);
			String threadId = threadString.substring(2);
			//System.out.println(threadId);
			if (isLock){
				String monitorId = monitorString.substring(8);
				return new Pair<Integer, Pair<String,String>>(positionInt, new Pair(monitorId, threadId));
			}
			String fieldId = fieldString.substring(6);
			//System.out.println(fieldId);
			return new Pair<Integer, Pair<String,String>>(positionInt, new Pair(fieldId, threadId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	private static ArrayList<Event> parseLogs(String filePath) {
		ArrayList<Event> log = new ArrayList<Event>();
		JSONParser jsonParser = new JSONParser();
		try {
			// read JSON file
			FileReader reader = new FileReader(filePath);
			JSONArray events = (JSONArray) jsonParser.parse(reader);
			Iterator i = events.iterator();
			while (i.hasNext()) {
				JSONObject innerObj = (JSONObject) i.next();
				String threadId = String.valueOf((String) innerObj
						.get("threadId"));
				int eventId = Integer.valueOf(innerObj.get("eventId")
						.toString());
				EventType eventType = EventType.valueOf(String
						.valueOf((String) innerObj.get("eventType")));
				if (!(eventType.equals(EventType.START)
						|| eventType.equals(EventType.EXIT))) {
					if (eventType.equals(EventType.LOCK)
							|| eventType.equals(EventType.UNLOCK)) {
						int monitorId = Integer.valueOf(innerObj.get("monitorId")
								.toString());
						log.add(new Event(threadId, eventId, eventType, monitorId));
					} else if (eventType.equals(EventType.FORK)
							|| eventType.equals(EventType.JOIN)) {
						String value = (String) innerObj.get("value");
						log.add(new Event(threadId, eventId, value, eventType));
					} else {
						int version = Integer.valueOf(innerObj.get("version").toString());
						int fieldId = Integer.valueOf(innerObj.get("fieldId").toString());
						Object value = innerObj.get("value");
						log.add(new Event(threadId, eventId, eventType, fieldId, version, value));
					}
				} else {
					log.add(new Event(threadId, eventId, eventType));
				}
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
		return log;
	}
	
	private static void initStructuresForAnalysis() {
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				Event event = entry.getValue().get(i);
				switch (event.getEventType()) {
				case READ:
					if (!readDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						readDomain.put(event.getFieldId(), events);
					} else {
						readDomain.get(event.getFieldId()).add(event);
					}
					break;
				case WRITE:
					if (!writeDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						writeDomain.put(event.getFieldId(), events);
					} else {
						writeDomain.get(event.getFieldId()).add(event);
					}

					Pair<Integer, Object> key = new Pair<Integer, Object>(
							event.getFieldId(), event.getValue());
					if (!writeSetValue.containsKey(key)) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						writeSetValue.put(key, events);
					} else {
						writeSetValue.get(key).add(event);
					}
					break;
				case LOCK:
					if (!lockDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						lockDomain.put(event.getFieldId(), events);
					} else {
						lockDomain.get(event.getFieldId()).add(event);
					}
					break;
				case UNLOCK:
					if (!unlockDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						unlockDomain.put(event.getFieldId(), events);
					} else {
						unlockDomain.get(event.getFieldId()).add(event);
					}
					break;
				case START:
					if (!startDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						startDomain.put(event.getFieldId(), events);
					} else {
						startDomain.get(event.getFieldId()).add(event);
					}
					break;
				case EXIT:
					if (!exitDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						exitDomain.put(event.getFieldId(), events);
					} else {
						exitDomain.get(event.getFieldId()).add(event);
					}
					break;
				case FORK:
					if (!forkDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						forkDomain.put(event.getFieldId(), events);
					} else {
						forkDomain.get(event.getFieldId()).add(event);
					}
					break;
				case JOIN:
					if (!joinDomain.containsKey(event.getFieldId())) {
						ArrayList<Event> events = new ArrayList<Event>();
						events.add(event);
						joinDomain.put(event.getFieldId(), events);
					} else {
						joinDomain.get(event.getFieldId()).add(event);
					}
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
		}
	}
	
	private static void visualiseForkJoinMemoryConstraints(Integer threadId){
		for (Entry<Integer, List<Event>> starts : startDomain.entrySet()) {
			for (int i = 0; i < starts.getValue().size(); i++) {
				Event start = starts.getValue().get(i);
				for (Entry<Integer, List<Event>> exits : exitDomain.entrySet()) {
					for (int j = 0; j < exits.getValue().size(); j++) {
						Event exit = exits.getValue().get(j);
						if (start.getThreadId().equals(exit.getThreadId())){
							//z3.post(z3.lt(start.getOrderConstraintName(), exit.getOrderConstraintName()));
							System.out.println(start.getOrderConstraintName() + " < " + exit.getOrderConstraintName());
						}
					}
				}
			}
		}
		for (Entry<Integer, List<Event>> forks : forkDomain.entrySet()) {
			for (int i = 0; i < forks.getValue().size(); i++) {
				Event fork = forks.getValue().get(i);
				for (Entry<Integer, List<Event>> joins : joinDomain.entrySet()) {
					for (int j = 0; j < joins.getValue().size(); j++) {
						Event join = joins.getValue().get(j);
						if (fork.getThreadId().equals(join.getThreadId()) && fork.getValue().equals(join.getValue())){
							//z3.post(z3.lt(fork.getOrderConstraintName(), join.getOrderConstraintName()));
							System.out.println(fork.getOrderConstraintName() + " < " + join.getOrderConstraintName());
						}
					}
				}
			}
		}
		for (Entry<Integer, List<Event>> entry : joinDomain.entrySet()){
			for (int i = 0; i < entry.getValue().size(); i++){
				if (entry.getValue().get(i).getThreadId().equals(threadId.toString())){
					Event join = entry.getValue().get(i);
					//System.out.println(join);
					for (Entry<Integer, List<Event>> exits : exitDomain.entrySet()){
						for (int j = 0; j < exits.getValue().size(); j++){
							if (join.getValue().toString().equals(exits.getValue().get(j).getThreadId())){
								Event exit = exits.getValue().get(j);
								System.out.println(exit.getOrderConstraintName() + " < " + join.getOrderConstraintName());
								
							}
						}
					}
					
				}
			}
		}
	}
	
	private static void visualiseReadOrderMemoryConstraints(Integer threadId){
		for (Entry<Integer, List<Event>> entry : readDomain.entrySet()){
			for (int i = 0; i < entry.getValue().size(); i++){
				if (entry.getValue().get(i).getThreadId().equals(threadId.toString())){
					System.out.println(entry.getValue().get(i).getOrderConstraintName());
				}
			}
		}
	}

	private static void visualiseWriteOrderMemoryConstraints(Integer threadId) {
		for (Entry<Integer, List<Event>> entry : writeDomain.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				if (entry.getValue().get(i).getThreadId()
						.equals(threadId.toString())) {
					System.out.println(entry.getValue().get(i)
							.getOrderConstraintName());
				}
			}
		}
	}
	
	private static void createThreadConstraints() {
		z3.writeLineZ3("(echo \"THREAD CONSTRAINTS -----\")\n");

		for (Entry<Integer, List<Event>> starts : startDomain.entrySet()) {
			for (int i = 0; i < starts.getValue().size(); i++) {
				Event start = starts.getValue().get(i);
				for (Entry<Integer, List<Event>> exits : exitDomain.entrySet()) {
					for (int j = 0; j < exits.getValue().size(); j++) {
						Event exit = exits.getValue().get(j);
						if (start.getThreadId().equals(exit.getThreadId())){
							z3.post(z3.lt(start.getOrderConstraintName(), exit.getOrderConstraintName()));
							//System.out.println(start.getOrderConstraintName() + " < " + exit.getOrderConstraintName());
						}
					}
				}
			}
		}
		
		for (Entry<Integer, List<Event>> forks : forkDomain.entrySet()) {
			for (int i = 0; i < forks.getValue().size(); i++) {
				Event fork = forks.getValue().get(i);
				for (Entry<Integer, List<Event>> joins : joinDomain.entrySet()) {
					for (int j = 0; j < joins.getValue().size(); j++) {
						Event join = joins.getValue().get(j);
						if (fork.getThreadId().equals(join.getThreadId()) && fork.getValue().equals(join.getValue())){
							z3.post(z3.lt(fork.getOrderConstraintName(), join.getOrderConstraintName()));
							//System.out.println(fork.getOrderConstraintName() + " < " + join.getOrderConstraintName());
						}
					}
				}
			}
		}
		
		for (Entry<Integer, List<Event>> joins : joinDomain.entrySet()) {
			for (int i = 0; i < joins.getValue().size(); i++) {
				Event join = joins.getValue().get(i);
				// System.out.println(join);
				for (Entry<Integer, List<Event>> exits : exitDomain.entrySet()) {
					for (int j = 0; j < exits.getValue().size(); j++) {
						if (join.getValue().toString()
								.equals(exits.getValue().get(j).getThreadId())) {
							Event exit = exits.getValue().get(j);
							System.out.println(exit.getOrderConstraintName()
									+ " < " + join.getOrderConstraintName());
							z3.post(z3.lt(exit.getOrderConstraintName(), join.getOrderConstraintName()));

						}

					}

				}
			}
		}
		for (Entry<Integer, List<Event>> forks : forkDomain.entrySet()) {
			for (int i = 0; i < forks.getValue().size(); i++) {
				Event fork = forks.getValue().get(i);
				System.out.println(fork);
				for (Entry<Integer, List<Event>> starts : startDomain.entrySet()) {
					for (int j = 0; j < starts.getValue().size(); j++) {
						if (fork.getValue().toString()
								.equals(starts.getValue().get(j).getThreadId())) {
							Event start = starts.getValue().get(j);
							System.out.println(fork.getOrderConstraintName()
									+ " < " + start.getOrderConstraintName());
							z3.post(z3.lt(fork.getOrderConstraintName(), start.getOrderConstraintName()));

						}

					}

				}
			}
		}
	}
	
	private static void createrReadWriteConstraints() throws NoMatchFound {
        z3.writeLineZ3("(echo \"READ-WRITE CONSTRAINTS -----\")\n");
        int label = 0;
        for (Entry<Integer, List<Event>> readops : readDomain.entrySet()) {
                for (int i = 0; i < readops.getValue().size(); i++) {
                        StringBuilder orStr = new StringBuilder();
                        Event read = readops.getValue().get(i);
                        Pair<Integer, Object> key = new Pair<Integer, Object>(
                                        read.getFieldId(), read.getValue());
                       
                        if (writeSetValue.containsKey(key)) {
                                if (writeSetValue.get(key).size() == 1) {
                                        // exact match
                                        Event write = writeSetValue.get(key).get(0);
                                        z3.post(z3.lt(write.getOrderConstraintName(), read.getOrderConstraintName()));
                                }
                                else {
                                        for (int j = 0; j < writeSetValue.get(key).size(); j++) {
                                                String R = read.getOrderConstraintName();
                                                Event write1 = writeSetValue.get(key).get(j);
                                                String W1 = write1.getOrderConstraintName();
                                               
                                                List<Event> restW = new ArrayList<Event>();
                                                restW.addAll(writeSetValue.get(key));
                                                restW.remove(j);
                                               
                                                StringBuilder andStr = new StringBuilder();
                                                andStr.append(" "+z3.lt(W1, R)); //const: Owi < Or
                                               
                                                for (Event write2 : restW){
                                                        String W2 = write2.getOrderConstraintName();
                                                        //const: Owj < Oi || Owj > Or
                            andStr.append(" "+z3.or(z3.lt(W2,W1), z3.lt(R,W2)));
                                                }
                                                //const: for all wi \in W
                            orStr.append("\n "+z3.and(andStr.toString()));
                               
                                        }
                                        z3.post(z3.name(z3.or(orStr.toString()), "RW"+label++));
                                }
                        } else {
                               continue;// throw new NoMatchFound();
                        }
                }
        }
}

	private static void createMemoryOrderConstraints() {
		int min = 0;
		int max = 0;
		List<Object> distinctEvents = new ArrayList<Object>();
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()) {
			max = max + entry.getValue().size();
			for (Event e : entry.getValue())
				distinctEvents.add(e.getOrderConstraintName());
		}
		
		z3.writeLineZ3("(echo \"MEMORY-ORDER CONSTRAINTS -----\")\n");
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()) {
			List<Object> events = new ArrayList<Object>();
			for (Event event : entry.getValue()){
				z3.makeIntVar(event.getOrderConstraintName(), min, max);
				events.add(event.getOrderConstraintName());
			}
			z3.post(z3.name(z3.lt(events), "MOth"+entry.getKey()));
		}
		z3.post(z3.distinct(distinctEvents));
	}
	
	private static void createWriteVersioningConstraints()
    {
            z3.writeLineZ3("(echo \"WRITE VERSIONING CONSTRAINTS -----\")\n");
            int label = 0;
            for(Map.Entry<Integer, List<Event>> entry : writeDomain.entrySet()){
                    //Event[] sortWrites = (Event[]) entry.getValue().toArray(); //Java does not accept casts from Object[] to Event[]
                    Event[] sortWrites = new Event[entry.getValue().size()];
                    int i = 0;
                    for(Event w : entry.getValue()){
                            sortWrites[i] = w;
                            i++;
                    }
                    Arrays.sort(sortWrites, new WriteEventComparator());
                    /*System.out.print("-- Field "+entry.getKey()+":");
                    for(Event w : sortWrites){
                            System.out.print(w.getOrderConstraintName()+" ");
                    }
                    System.out.println("");*/
                    for(i = 0; i < sortWrites.length; i++){
                            if(i+1 >= sortWrites.length)
                                    break;
                            int j = i+1;
                            Event wi = sortWrites[i];
                            Event wj = sortWrites[j];
                            while(wi.getVersion() == wj.getVersion()){
                                    j++;
                            }
                            z3.post(z3.name(z3.lt(wi.getOrderConstraintName(),wj.getOrderConstraintName()),("WC"+label++)));
                    }
                    //System.out.println("");
            }
    }

}
