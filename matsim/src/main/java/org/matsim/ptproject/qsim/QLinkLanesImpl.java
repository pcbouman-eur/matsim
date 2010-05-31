/* *********************************************************************** *
 * project: org.matsim.*
 * QueueLink.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.ptproject.qsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.events.AgentStuckEventImpl;
import org.matsim.core.events.AgentWait2LinkEventImpl;
import org.matsim.core.events.LinkEnterEventImpl;
import org.matsim.core.mobsim.framework.PersonAgent;
import org.matsim.lanes.Lane;
import org.matsim.lanes.LaneMeterFromLinkEndComparator;
import org.matsim.signalsystems.systems.SignalGroupDefinition;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.VisData;

/**
 * @author dstrippgen
 * @author dgrether
 * @author mrieser
 *
 * A QueueLink can consist of one or more QueueLanes, which may have the following layout
 * (Dashes stand for the borders of the QueueLink, equal signs (===) depict one lane,
 * plus signs (+) symbolize a decision point where one lane splits into several lanes) :
 * <pre>
 * ----------------
 * ================
 * ----------------
 *</pre>
 *
 *<pre>
 * ----------------
 *          =======
 * ========+
 *          =======
 * ----------------
 *</pre>
 *
 *<pre>
 * ----------------
 *         ========
 * =======+========
 *         ========
 * ----------------
 * </pre>
 *
 *
 * The following layouts are not allowed:
 * <pre>
 * ----------------
 * ================
 * ================
 * ----------------
 *</pre>
 *
 *<pre>
 * ----------------
 * =======
 *        +========
 * =======
 * ----------------
 * </pre>
 *
 *agent
 * Queue Model Link implementation with the following properties:
 * <ul>
 *   <li>The queue behavior itself is simulated by one or more instances of QueueLane</li>
 *   <li>Each QueueLink has at least one QueueLane. All QueueVehicles added to the QueueLink
 *   are placed on this always existing instance.
 *    </li>
 *   <li>All QueueLane instances which are connected to the ToQueueNode are
 *   held in the attribute toNodeQueueLanes</li>
 *   <li>QueueLink is active as long as at least one
 * of its QueueLanes is active.</li>
 * </ul>
 */
public class QLinkLanesImpl implements QLink {

	final private static Logger log = Logger.getLogger(QLinkLanesImpl.class);

	final private static QLane.FromLinkEndComparator fromLinkEndComparator = new QLane.FromLinkEndComparator();

	/**
	 * The Link instance containing the data
	 */
	private final Link link;
	/**
	 * Reference to the QueueNode which is at the end of each QueueLink instance
	 */
	private final QNode toQueueNode;
	/**
	 * The QueueLane instance which always exists.
	 */
	private QLane originalLane;
	/**
	 * A List holding all QueueLane instances of the QueueLink
	 */
	private List<QLane> queueLanes;

	/**
	 * If more than one QueueLane exists this list holds all QueueLanes connected to
	 * the (To)QueueNode of the QueueLink
	 */
	private List<QLane> toNodeQueueLanes = null;

	private boolean active = false;

	private final Map<Id, QVehicle> parkedVehicles = new LinkedHashMap<Id, QVehicle>(10);

	private final Map<Id, PersonAgent> agentsInActivities = new LinkedHashMap<Id, PersonAgent>();

	private VisData visdata = this.new VisDataImpl();

	private QSimEngine qsimEngine = null;

	/**
	 * All vehicles from parkingList move to the waitingList as soon as their time
	 * has come. They are then filled into the vehQueue, depending on free space
	 * in the vehQueue
	 */
	/*package*/ final Queue<QVehicle> waitingList = new LinkedList<QVehicle>();

	/**
	 * Initializes a QueueLink with one QueueLane.
	 * @param link2
	 * @param queueNetwork
	 * @param toNode
	 * @see QLink#createLanes(List)
	 */
	public QLinkLanesImpl(final Link link2, QSimEngine engine, 
			final QNode toNode, Map<Id, Lane> laneMap) {
		this.link = link2;
		this.toQueueNode = toNode;
		this.qsimEngine = engine;
		this.queueLanes = new ArrayList<QLane>();
		this.toNodeQueueLanes = new ArrayList<QLane>();
		this.createLanes(laneMap);
	}
	
	
	/**
	 * Initialize the QueueLink with more than one QueueLane
	 * @param map
	 */
	private void createLanes(Map<Id, Lane> map) {
		List<Lane> sortedLanes =  new ArrayList<Lane>(map.values());
		Collections.sort(sortedLanes, new LaneMeterFromLinkEndComparator());
		Collections.reverse(sortedLanes);
		
		List<QLane> laneList = new LinkedList<QLane>();
		Lane firstLane = sortedLanes.remove(0);
		if (firstLane.getStartsAtMeterFromLinkEnd() != this.link.getLength()) {
			throw new IllegalStateException("First Lane Id " + firstLane.getId() + " on Link Id " + this.link.getId() + 
					"isn't starting at the beginning of the link!");
		}
		this.originalLane = new QLane(this, firstLane, true);
		laneList.add(this.originalLane);
		Stack<QLane> laneStack = new Stack<QLane>();
		
		while (!laneList.isEmpty()){
			QLane lastQLane = laneList.remove(0);
			laneStack.push(lastQLane);
			lastQLane.setFireLaneEvents(true);
			this.queueLanes.add(lastQLane);
			
			//if existing create the subsequent lanes
			List<Id> toLaneIds = lastQLane.getLaneData().getToLaneIds();
			double nextMetersFromLinkEnd = 0.0;
			double laneLength = 0.0;
			if (toLaneIds != null 	&& (!toLaneIds.isEmpty())) {
				for (Id toLaneId : toLaneIds){
					Lane currentLane = map.get(toLaneId);
					nextMetersFromLinkEnd = currentLane.getStartsAtMeterFromLinkEnd();
					QLane currentQLane = new QLane(this, currentLane, false);
					laneList.add(currentQLane);
					lastQLane.addToLane(currentQLane);
				}
				laneLength = (link.getLength() - nextMetersFromLinkEnd) - (link.getLength() -  lastQLane.getLaneData().getStartsAtMeterFromLinkEnd());
				laneLength = lastQLane.getLaneData().getStartsAtMeterFromLinkEnd() - nextMetersFromLinkEnd;
				lastQLane.setEndsAtMetersFromLinkEnd(nextMetersFromLinkEnd);
			}
			//there are no subsequent lanes 
			else {
				laneLength = lastQLane.getLaneData().getStartsAtMeterFromLinkEnd();
				lastQLane.setEndsAtMetersFromLinkEnd(0.0);
				this.toNodeQueueLanes.add(lastQLane);
			}
			lastQLane.setLaneLength(laneLength);
		}		
		
		//fill toLinks
		while (!laneStack.isEmpty()){
			QLane qLane = laneStack.pop();
			qLane.calculateCapacities();
			if (qLane.getToLanes() == null || (qLane.getToLanes().isEmpty())) {
				for (Id toLinkId : qLane.getLaneData().getToLinkIds()){
					qLane.addDestinationLink(toLinkId);
				}
			}
			else {
				for (QLane subsequentLane : qLane.getToLanes()){
					for (Id toLinkId : subsequentLane.getDestinationLinkIds()){
						qLane.addDestinationLink(toLinkId);
					}
				}
			}
		}

		Collections.sort(this.queueLanes, QLinkLanesImpl.fromLinkEndComparator);
	}

	public List<QLane> getToNodeQueueLanes() {
		return this.toNodeQueueLanes;
	}

	protected void addSignalGroupDefinition(SignalGroupDefinition signalGroupDefinition) {
		for (QLane lane : this.toNodeQueueLanes) {
			lane.addSignalGroupDefinition(signalGroupDefinition);
		}
	}

	public void activateLink() {
		if (!this.active) {
			this.qsimEngine.activateLink(this);
			this.active = true;
		}
	}

	/**
	 * Adds a vehicle to the link, called by
	 * {@link QNode#moveVehicleOverNode(QVehicle, QLane, double)}.
	 *
	 * @param veh
	 *          the vehicle
	 */
	public void addFromIntersection(final QVehicle veh) {
		double now = this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();
		activateLink();
		this.originalLane.addToVehicleQueue(veh, now);
		veh.setCurrentLink(this.getLink());
		this.getQSimEngine().getQSim().getEventsManager().processEvent(
				new LinkEnterEventImpl(now, veh.getDriver().getPerson().getId(),
						this.getLink().getId()));
	}

	public void clearVehicles() {
		double now = this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();
		this.parkedVehicles.clear();
		for (QVehicle veh : this.waitingList) {
			this.getQSimEngine().getQSim().getEventsManager().processEvent(
					new AgentStuckEventImpl(now, veh.getDriver().getPerson().getId(), veh.getCurrentLink().getId(), veh.getDriver().getCurrentLeg().getMode()));
		}
		this.getQSimEngine().getQSim().getAgentCounter().decLiving(this.waitingList.size());
		this.getQSimEngine().getQSim().getAgentCounter().incLost(this.waitingList.size());
		this.waitingList.clear();

		for (QLane lane : this.queueLanes){
			lane.clearVehicles(now);
		}
	}

	public void addParkedVehicle(QVehicle vehicle) {
		this.parkedVehicles.put(vehicle.getId(), vehicle);
		vehicle.setCurrentLink(this.link);
	}

	/*package*/ QVehicle getParkedVehicle(Id vehicleId) {
		return this.parkedVehicles.get(vehicleId);
	}

	public QVehicle removeParkedVehicle(Id vehicleId) {
		return this.parkedVehicles.remove(vehicleId);
	}

	public void addDepartingVehicle(QVehicle vehicle) {
		this.waitingList.add(vehicle);
		this.activateLink();
	}

	public boolean moveLink(double now) {
		boolean ret = false;
		for (QLane lane : this.queueLanes){
			if (lane.moveLane(now)){
				ret = true;
			}
		}
		this.moveWaitToBuffer(now);
		this.active = (ret || (!this.waitingList.isEmpty()));
		return this.active;
}

	/**
	 * Move as many waiting cars to the link as it is possible
	 *
	 * @param now
	 *          the current time
	 */
	private void moveWaitToBuffer(final double now) {
		while (this.originalLane.hasBufferSpace()) {
			QVehicle veh = this.waitingList.poll();
			if (veh == null) {
				return;
			}

			this.getQSimEngine().getQSim().getEventsManager().processEvent(
			new AgentWait2LinkEventImpl(now, veh.getDriver().getPerson().getId(), this.getLink().getId(),
					veh.getDriver().getCurrentLeg().getMode()));
			boolean handled = this.originalLane.transitQueueLaneFeature.handleMoveWaitToBuffer(now, veh);
			if (!handled) {
				this.originalLane.addToBuffer(veh, now);
			}
		}
	}


	public boolean bufferIsEmpty() {
		//if there is only one lane...
		if (this.toNodeQueueLanes == null){
			return this.originalLane.bufferIsEmpty();
		}
		//otherwise we have to do a bit more work
		for (QLane lane : this.toNodeQueueLanes){
			if (!lane.bufferIsEmpty()){
				return false;
			}
		}
		return true;
	}

	public boolean hasSpace() {
		return this.originalLane.hasSpace();
	}

	public void recalcTimeVariantAttributes(double time) {
		for (QLane ql : this.queueLanes){
			ql.recalcTimeVariantAttributes(time);
		}
	}

	public QVehicle getVehicle(Id vehicleId) {
		QVehicle ret = getParkedVehicle(vehicleId);
		if (ret != null) {
			return ret;
		}
		for (QVehicle veh : this.waitingList) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		for (QLane lane : this.queueLanes){
			ret = lane.getVehicle(vehicleId);
			if (ret != null) {
				return ret;
			}
		}
		return ret;
	}

	public Collection<QVehicle> getAllVehicles() {
		Collection<QVehicle> ret = new ArrayList<QVehicle>(this.parkedVehicles.values());
		ret.addAll(this.waitingList);
		for  (QLane lane : this.queueLanes){
			ret.addAll(lane.getAllVehicles());
		}
		return ret;
	}

	/**
	 * @return the total space capacity available on that link (includes the space on lanes if available)
	 */
	public double getSpaceCap() {
		double total = 0.0;
		for (QLane ql : this.getQueueLanes()) {
				total += ql.getStorageCapacity();
		}
		return total;
	}

	/**
	 * One should think about the need for this method
	 * because it is only called by one testcase
	 * @return
	 */
	protected int vehOnLinkCount() {
		int count = 0;
		for (QLane ql : this.queueLanes){
			count += ql.vehOnLinkCount();
		}
		return count;
	}

	public Link getLink() {
		return this.link;
	}

	public QNode getToQueueNode() {
		return this.toQueueNode;
	}

	/**
	 * This method returns the normalized capacity of the link, i.e. the capacity
	 * of vehicles per second. It is considering the capacity reduction factors
	 * set in the config and the simulation's tick time.
	 *
	 * @return the flow capacity of this link per second, scaled by the config
	 *         values and in relation to the SimulationTimer's simticktime.
	 */
	public double getSimulatedFlowCapacity() {
		return this.originalLane.getSimulatedFlowCapacity();
	}

	public QSimEngine getQSimEngine(){
    return this.qsimEngine;
  }

	@Override
	public void setQSimEngine(QSimEngine qsimEngine) {
		this.qsimEngine = qsimEngine;
	}

	/**
	 * @return the QLanes of this QueueLink
	 */
	public List<QLane> getQueueLanes(){
		return this.queueLanes;
	}

	public VisData getVisData() {
		return this.visdata;
	}

	public QLane getOriginalLane(){
		return this.originalLane;
	}

  @Override
  public LinkedList<QVehicle> getVehQueue() {
    LinkedList<QVehicle> ll = this.originalLane.getVehQueue();
    for (QLane l : this.getToNodeQueueLanes()){
      ll.addAll(l.getVehQueue());
    }
    return ll;
  }

	/**
	 * Inner class to capsulate visualization methods
	 * @author dgrether
	 *
	 */
	class VisDataImpl implements VisData {

//		public double getDisplayableTimeCapValue(double time) {
//			return originalLane.visdata.getDisplayableTimeCapValue(time);
//		}

		public Collection<AgentSnapshotInfo> getVehiclePositions( final Collection<AgentSnapshotInfo> positions) {
//			double time = QLinkLanesImpl.this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay() ;


			AgentSnapshotInfoBuilder agentSnapshotInfoBuilder = QLinkLanesImpl.this.getQSimEngine().getAgentSnapshotInfoBuilder();

		  for (QLane lane : QLinkLanesImpl.this.getQueueLanes()) {
		    lane.visdata.getVehiclePositions(positions);
		  }

		  int cnt2 = 0;
  		// treat vehicles from waiting list:
		  agentSnapshotInfoBuilder.positionVehiclesFromWaitingList(positions, QLinkLanesImpl.this.link, cnt2,
					QLinkLanesImpl.this.waitingList, null);
		  cnt2 = QLinkLanesImpl.this.waitingList.size();
		  agentSnapshotInfoBuilder.positionAgentsInActivities(positions, QLinkLanesImpl.this.link,
		  		QLinkLanesImpl.this.agentsInActivities.values(), cnt2);

		  return positions;
		}
	}

	@Override
	public void addAgentInActivity(PersonAgent agent) {
		agentsInActivities.put(agent.getPerson().getId(), agent);
	}

	@Override
	public void removeAgentInActivity(PersonAgent agent) {
		agentsInActivities.remove(agent.getPerson().getId());
	}


  @Override
  public double getBufferLastMovedTime() {
    throw new UnsupportedOperationException();
  }


  @Override
  public QVehicle popFirstFromBuffer() {
    throw new UnsupportedOperationException();
  }

}
