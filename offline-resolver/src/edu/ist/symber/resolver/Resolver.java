package edu.ist.symber.resolver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import edu.ist.symber.common.Event;
import edu.ist.symber.common.EventType;
import edu.ist.symber.common.Pair;

public class Resolver {
	private static Z3Connector z3 = new Z3Connector();
	private static final int THREADS = 1;	//has to be the same as in the benchmark
	private static final String LOG_DIR = "d:\\record-and-replay\\symber-recorder\\logs";
	private static final String OUTPUT_DIR = "d:\\record-and-replay\\symber-replayer\\logs";
	private static final String NAME = "\\log";
	private static final String EXTENSION = ".json";
	private static final String SCHEDULE_NAME = "schedule";
	private static final String SOLUTION_PATH = ".\\z3output\\z3Solution.txt";
	private static Map<Integer, ArrayList<Event>> logs = new HashMap<Integer, ArrayList<Event>>();
	private static Map<Integer, List<Event>> readDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> writeDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> lockDomain = new HashMap<Integer, List<Event>>();
	private static Map<Pair<Integer, Object>, List<Event>> writeSetValue = new HashMap<Pair<Integer, Object>, List<Event>>();
	

	public static void main(String[] args) {
		System.out.println("Parsing logs..");
		for (int i = 0; i <= THREADS; i++) {
			logs.put(i, parseLogs(LOG_DIR + NAME + i + EXTENSION));
		}
		initStructuresForAnalysis();
		//visualiseReadOrderMemoryConstraints(2);
		//visualiseWriteOrderMemoryConstraints(2);
		z3.writeLineZ3("(set-option :produce-unsat-cores true)\n");
		System.out.println("Create Memory Order Constraints..");
		createMemoryOrderConstraints();
		try {
			System.out.println("Create Read Write Constraints..");
			createrReadWriteConstraints();
		} catch (NoMatchFound e) {
			e.printStackTrace();
		}
		System.out.println("Finding solution with Z3..");
		z3.solve();
		z3.printModel();
		produceLogForReplayer(SOLUTION_PATH);

	}
	
	private static void produceLogForReplayer(String file) {
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
				+ SCHEDULE_NAME + EXTENSION);
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
		final Pattern pattern = Pattern
				.compile("(\\(define-fun\\sO(R|W|L)-field_[0-9]+-v[0-9]+.[0-9]+-th[0-9]+.[0-9]+@.+\\n\\s+[0-9]+\\))");
		final Matcher m = pattern.matcher(sb.toString());
		while (m.find()) {
			Pair<Integer, Pair<String, String>> pair = parseDefineFunEntry(m.group());
			result.put(pair.getFirst(), pair.getSecond());
			
		}

		return result;
	}

	private static Pair<Integer, Pair<String, String>> parseDefineFunEntry(String source) {
		final Pattern position = Pattern.compile("\\s+[0-9]+");
		final Pattern field = Pattern.compile("field_[0-9]+");
		final Pattern thread = Pattern.compile("-th[0-9]+");
		final Matcher pm = position.matcher(source);
		final Matcher fm = field.matcher(source);
		final Matcher tm = thread.matcher(source);
		String positionString = "";
		String fieldString = "";
		String threadString = "";
		if (pm.find()) positionString = pm.group();
		if (fm.find()) fieldString = fm.group();
		if (tm.find()) threadString = tm.group();
		try {
			Integer positionInt = Integer.valueOf(positionString.trim().substring(0));
			//System.out.println(positionInt);
			String fieldId = fieldString.substring(6);
			//System.out.println(fieldId);
			String threadId = threadString.substring(3);
			//System.out.println(threadId);
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
				int version = Integer.valueOf(innerObj.get("version")
						.toString());
				int subversion = Integer.valueOf(innerObj.get("subversion")
						.toString());
				if (eventType.equals(EventType.LOCK)) {
					int monitorId = Integer.valueOf(innerObj.get("monitorId")
							.toString());
					log.add(new Event(threadId, eventId, eventType, monitorId,
							version));
				} else {
					int fieldId = Integer.valueOf(innerObj.get("fieldId")
							.toString());
					Object value = innerObj.get("value");
					log.add(new Event(threadId, eventId, eventType, fieldId,
							version, value));
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
				default:
					throw new IllegalArgumentException();
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
	
	private static void createrReadWriteConstraints() throws NoMatchFound {
		z3.writeLineZ3("(echo \"READ-WRITE CONSTRAINTS -----\")\n");

		for (Entry<Integer, List<Event>> readops : readDomain.entrySet()) {
			for (int i = 0; i < readops.getValue().size(); i++) {
				Event read = readops.getValue().get(i);
				Pair<Integer, Object> key = new Pair<Integer, Object>(
						read.getFieldId(), read.getValue());
				if (writeSetValue.containsKey(key)) {
					if (writeSetValue.get(key).size() == 1) {
						// exact match
						Event write = writeSetValue.get(key).get(0);
						z3.post(z3.lt(write.getOrderConstraintName(), read.getOrderConstraintName()));
					} else {
						StringBuilder constraint = new StringBuilder();
						for (int j = 0; j < writeSetValue.get(key).size(); j++) {
							String R = read.getOrderConstraintName();
							Event write1 = writeSetValue.get(key).get(j);
							String W1 = write1.getOrderConstraintName();
							List<Event> restW = new ArrayList<Event>();
							restW.addAll(writeSetValue.get(key));
							restW.remove(j);
							for (Event write2 : restW){
								String W2 = write2.getOrderConstraintName();
								constraint.append(z3.or(z3.and(z3.lt(R, W1),z3.or(z3.lt(W2, W1), z3.lt(R, W2))), z3.and(z3.lt(R, W2),z3.or(z3.lt(W1, W2), z3.lt(R, W1)))));
							}
						}
						z3.post(z3.name(z3.and(constraint.toString()), "RW"+i));
					}
				} else {
					throw new NoMatchFound();
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

}
