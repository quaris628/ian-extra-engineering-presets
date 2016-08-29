package com.walkertribe.ian.iface;

import java.util.Collections;
import java.util.List;

import com.walkertribe.ian.protocol.ArtemisPacket;
import com.walkertribe.ian.protocol.core.world.ObjectUpdatePacket;
import com.walkertribe.ian.world.ArtemisObject;

/**
 * Object which reports the results of a packet parsing attempt.
 * @author rjwut
 */
public class ParseResult {
	private ArtemisPacket packet;
	private List<ListenerMethod> interestedPacketListeners = Collections.EMPTY_LIST;
	private List<ListenerMethod> interestedObjectListeners = Collections.EMPTY_LIST;

	ParseResult() {
		// make constructor accessible only to the package
	}

	/**
	 * Returns the packet object generated by the parse attempt.
	 */
	public ArtemisPacket getPacket() {
		return packet;
	}

	/**
	 * Sets the packet object generated by the parse attempt.
	 */
	void setPacket(ArtemisPacket packet) {
		this.packet = packet;
	}

	/**
	 * Adds a ListenerMethod that is interested in the packet.
	 */
	void setPacketListeners(List<ListenerMethod> listeners) {
		interestedPacketListeners = listeners;
	}

	/**
	 * Adds a ListenerMethod that is interested in the objects in the packet.
	 * (Applies only to ObjectUpdatePackets.)
	 */
	void setObjectListeners(List<ListenerMethod> listeners) {
		interestedObjectListeners = listeners;
	}

	/**
	 * Convenience method for isInterestingPacket() || isInterestingObject().
	 */
	public boolean isInteresting() {
		return !(interestedPacketListeners.isEmpty() && interestedObjectListeners.isEmpty());
	}

	/**
	 * Returns true if the packet was of interest to any listeners. Note that in
	 * the case of an ObjectUpdatePacket, there may be listeners that aren't
	 * interested in the packet itself, but are interested in certain types of
	 * objects the packet may contain. Thus, it's entirely possible for
	 * isInterestingPacket() to return false while isContainsInterestingObject()
	 * returns true.
	 */
	public boolean isInterestingPacket() {
		return !interestedPacketListeners.isEmpty();
	}

	/**
	 * Returns true if the packet is an ObjectUpdatePacket and it contains an
	 * object of interest to one or more listeners.
	 */
	public boolean isContainsInterestingObject() {
		return !interestedObjectListeners.isEmpty();
	}

	/**
	 * Fire the listeners that were interested in this packet or its contents.
	 */
	public void fireListeners() {
		for (ListenerMethod method : interestedPacketListeners) {
			method.offer(packet);
		}

		if (packet instanceof ObjectUpdatePacket) {
			ObjectUpdatePacket ouPacket = (ObjectUpdatePacket) packet;

			for (ArtemisObject obj : ouPacket.getObjects()) {
				for (ListenerMethod method : interestedObjectListeners) {
					method.offer(obj);
				}
			}
		}
	}
}
