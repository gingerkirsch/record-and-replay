package edu.ist.symber.resolver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.ist.symber.common.Event;
import edu.ist.symber.common.EventType;
import edu.ist.symber.common.Pair;

public class ConstraintsCreator {
	private static Z3Connector z3 = new Z3Connector();
	private static final int THREADS = 2;	//has to be the same as in the benchmark
	private static final String LOG_DIR = "d:\\record-and-replay\\symber-recorder\\logs";
	private static Map<Integer, ArrayList<Event>> logs = new HashMap<Integer, ArrayList<Event>>();
	private static Map<Integer, List<Event>> readDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> writeDomain = new HashMap<Integer, List<Event>>();
	private static Map<Integer, List<Event>> lockDomain = new HashMap<Integer, List<Event>>();
	private static Map<Pair<Integer, Object>, List<Event>> writeSetValue = new HashMap<Pair<Integer, Object>, List<Event>>();
	

	public static void main(String[] args) {
		for (int i = 0; i <= THREADS; i++) {
			logs.put(i, parse(LOG_DIR + "\\log" + i + ".json"));
		}
		initStructuresForAnalysis();
		//visualiseReadOrderMemoryConstraints(2);
		//visualiseWriteOrderMemoryConstraints(2);
		z3.writeLineZ3("(set-option :produce-unsat-cores true)\n");
		createMemoryOrderConstraints();
		try {
			createrReadWriteConstraints();
		} catch (NoMatchFound e) {
			e.printStackTrace();
		}

		z3.solve();
		z3.printModel();

	}
	
	private static ArrayList<Event> parse(String filePath) {
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
