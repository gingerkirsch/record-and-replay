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
	private static final int THREADS = 1;
	private static final String LOG_DIR = "d:\\record-and-replay\\symber-recorder\\logs";
	private static Map<Integer, ArrayList<Event>> logs = new HashMap<Integer, ArrayList<Event>>();

	public static void main(String[] args) {
		// parse logs to array of events per thread
		for (int i = 0; i <= THREADS; i++) {
			logs.put(i, parse(LOG_DIR + "\\log" + i + ".json"));
		}
		System.out.println(logs.size());
		z3.writeLineZ3("(set-option :produce-unsat-cores true)\n");
		try {
			createrReadWriteConstraints();
			createMemoryOrderConstraints();
		} catch (NoMatchFound e) {
			e.printStackTrace();
		}

	}
	
	private static void createrReadWriteConstraints() throws NoMatchFound {
		z3.writeLineZ3("(echo \"READ-WRITE CONSTRAINTS -----\")\n");
		int min = 0;
		int max = 0;
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()) {
			max = max + entry.getValue().size();
		}
		Map<Integer, List<Event>> readDomain = new HashMap<Integer, List<Event>>();
		// Map<Integer, List<Event>> writeDomain = new HashMap<Integer, List<Event>>();
		Map<Integer, List<Event>> lockDomain = new HashMap<Integer, List<Event>>();
		Map<Pair<Integer, Object>, List<Event>> writeSetValue = new HashMap<Pair<Integer, Object>, List<Event>>();

		/*----------------------Filling up the collections-------------------------*/
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				Event event = entry.getValue().get(i);
				System.out.println(event.getOrderConstraintName());
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
					/*
					 * if (!writeDomain.containsKey(event.getFieldId())){
					 * ArrayList<Event> events = new ArrayList<Event>();
					 * events.add(event); writeDomain.put(event.getFieldId(),
					 * events); } else {
					 * writeDomain.get(event.getFieldId()).add(event); }
					 */
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
			/*----------------------Creating the constraints-------------------------*/
			for (Entry<Integer, List<Event>> entry1 : readDomain.entrySet()) {
				for (int i = 0; i < entry1.getValue().size(); i++) {
					Event event = entry1.getValue().get(i);
					z3.makeIntVar(event.getOrderConstraintName(), min, max);
					Pair<Integer, Object> key = new Pair<Integer, Object>(
							event.getFieldId(), event.getValue());
					if (writeSetValue.containsKey(key)) {
						if (writeSetValue.get(key).size() == 1) {
							// exact match
							z3.post(z3.lt(writeSetValue.get(key).get(0)
									.getOrderConstraintName(),
									event.getOrderConstraintName()));
						} else {
							StringBuilder sb = new StringBuilder();
							for (int j = 0; j < writeSetValue.get(key).size(); j++) {
								List<Event> restW = new ArrayList<Event>();
								restW.addAll(writeSetValue.get(key));
								restW.remove(j);
								sb.append(z3.or(
										z3.and(
										/* read happens before write */
										z3.lt(writeSetValue.get(key).get(j)
												.getOrderConstraintName(),
												event.getOrderConstraintName()),
										/* other writes after */
										z3.lt(writeSetValue.get(key).get(j)
												.getOrderConstraintName(),
												restW)),
										z3.and(
										/* read happens before write */
										z3.lt(writeSetValue.get(key).get(j)
												.getOrderConstraintName(),
												event.getOrderConstraintName()),
										/* other writes before */
										z3.gt(writeSetValue.get(key).get(j)
												.getOrderConstraintName(),
												restW))));
							}
							z3.post(z3.and(sb.toString()));
						}
					} else {
						throw new NoMatchFound();
					}
				}
			}
		}

	}

	private static void createMemoryOrderConstraints() {
		z3.writeLineZ3("(echo \"MEMORY-ORDER CONSTRAINTS -----\")\n");
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()) {
			List<Object> events = new ArrayList<Object>();
			for (Event event : entry.getValue()){
				events.add(event.getOrderConstraintName());
			}
			z3.post(z3.lt(events));
		}
	}
	
/*	private static void linkageInfer() {
		for (Entry<Integer, ArrayList<Event>> entry : readLog.entrySet()) {
			ArrayList<Event> version = entry.getValue();
			if (version.size() > 1) {
				// means we have more than one read operation with the same version
				int amountOfReadOps = version.size() - 1;
				Collections.reverse(version);
				// so we assign them subversion according to their order in log
				for (Event event: version) {
					event.setSubversion(amountOfReadOps--);
				}
				Collections.reverse(version);
				// we search for exact linkage starting from least subversion
				for (Event readOp : version) {
					//searchForMatch(readOp, readOp.getVersion());
				}

			} else {
				//searchForMatch(version.get(0), version.get(0).getVersion());
			}
		}
	}*/
/*	private static void searchForMatch(Event readOp, int boundedLinkage) {
		bl_up: for (int k = boundedLinkage; k > 0; k--) {
			// if we have an entry with the same version in the write log, we search for linkage there
			// otherwise we look in an entry with smaller version until we reach the end of log
			if (writeLog.containsKey(boundedLinkage)){
				ArrayList<Event> version = writeLog.get(boundedLinkage);
				// if there are varioius write operations with the same version, we resolve conflict
				// if conflict did not get resolved, we look for linkage in earlier write operations
				if (version.size() > 1 && resolveConflict(readOp, version)) {
					// conflict is resolved, no need to refer to other entries
					break bl_up;
				} else if (readOp.getValue().equals(version.get(0).getValue())) {
					version.get(0).setSubversion(readOp.getSubversion());
					//readOp.setIsRead(true);
					System.out.println("Found the exact linkage: R"
							+ readOp.getVersion() + "."
							+ readOp.getSubversion() + "(" + readOp.getValue()
							+ ") - W" + version.get(0).getVersion() + "."
							+ version.get(0).getSubversion() + "("
							+ version.get(0).getValue() + ")");
					schedule.add(version.get(0));
					schedule.add(readOp);
					break bl_up;
				}
			}
		}
	}*/
/*	private static boolean resolveConflict(Event readOp,
			ArrayList<Event> conflictingWrites) {
		for (Event writeOp : conflictingWrites) {
			// among write operations with same version, we look for match by value
			// we do not add to the schedule blind writes 
			// for the writes with the same value we pick the earliest
			if (String.valueOf(readOp.getValue()).equals(String.valueOf(writeOp.getValue()))) {
				writeOp.setSubversion(readOp.getSubversion());
				//readOp.setIsRead(true);
				System.out.println("Found the exact linkage after conflict resolution: R"
						+ readOp.getVersion() + "." + readOp.getSubversion()
						+ "(" + readOp.getValue() + ") - W"
						+ writeOp.getVersion() + "." + writeOp.getSubversion()
						+ "(" + writeOp.getValue() + ")");
				schedule.add(writeOp);
				schedule.add(readOp);
				return true;
			}
		}
		return false;
	}
*/
/*	private static String createWriteDomain() {
		Map<Integer, List<Event>> writeDomain = new HashMap<Integer, List<Event>>();
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()){
			for (int i = 0; i < entry.getValue().size(); i++){
				Event event = entry.getValue().get(i);
				if (!writeDomain.containsKey(event.getFieldId())){
					ArrayList<Event> events = new  ArrayList<Event>();
					events.add(event);
					writeDomain.put(event.getFieldId(), events);
					//z3.makeIntVarAndStore(name, min, max)
				} else {
					writeDomain.get(event.getFieldId()).add(event);
				}
				
			}
		}
		return null;
	}*/
/*	private static String createReadDomain() {
		Map<Integer, List<Event>> readDomain = new HashMap<Integer, List<Event>>();
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()){
			for (int i = 0; i < entry.getValue().size(); i++){
				Event event = entry.getValue().get(i);
				if (!readDomain.containsKey(event.getFieldId())){
					ArrayList<Event> events = new  ArrayList<Event>();
					events.add(event);
					readDomain.put(event.getFieldId(), events);
					//z3.makeIntVarAndStore(name, min, max)
				} else {
					readDomain.get(event.getFieldId()).add(event);
				}
			}
		}
		return null;
	}*/
/*	private static List<String> createPartialHappensBefore() {
		z3.writeLineZ3("(echo \"READ-WRITE CONSTRAINTS -----\")");
		ArrayList<String> result = new ArrayList<String>();
		for (Entry<Integer, ArrayList<Event>> entry : logs.entrySet()){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < entry.getValue().size(); i++){
				Event event = entry.getValue().get(i);
				switch (event.getEventType()){
					case READ: sb.append("R"); break;
					case WRITE: sb.append("W"); break;
					case LOCK: sb.append("L"); break;
				}
				
				//z3.lt(value, exp)
				sb.append(event.getFieldId()+":"+event.getVersion());
				sb.append(" ["+event.getValue().toString()+"]");
				sb.append("by T"+event.getThreadId());
				//happends before
				sb.append(" < ");
	//			System.out.println(sb.toString().trim());
			}
			System.out.println(sb.toString().trim());
			System.out.println();
			result.add(sb.toString().trim());
			
		}
 		return result;
	}*/

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
}
