/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.rcd.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.rcd.ICMTS;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock CMTS that can be used for testing. startServer() is called to start required threads after instantiation.
 */
public class CMTS extends AbstractPCMMServer implements ICMTS {

	/**
	 * Receives messages from the COPS client
	 */
	private final Map<String, IPCMMClientHandler> handlerMap;

	/**
	 * The configured gates
	 */
	private final Map<Direction, Set<String>> gateConfig;

	/**
	 * The connected CMTSs and whether or not they are up
	 */
	private final Map<String, Boolean> cmStatus;

	/**
	 * Constructor for having the server port automatically assigned
	 * Call getPort() after startServer() is called to determine the port number of the server
	 */
	public CMTS(final Map<Direction, Set<String>> gateConfig, final Map<String, Boolean> cmStatus) {
		this(0, gateConfig, cmStatus);
	}

	/**
	 * Constructor for starting the server to a pre-defined port number
	 * @param port - the port number on which to start the server.
	 */
	public CMTS(final int port, final Map<Direction, Set<String>> gateConfig, final Map<String, Boolean> cmStatus) {
		super(port);
		if (gateConfig == null || cmStatus == null) throw new IllegalArgumentException("Config must not be null");
		this.gateConfig = Collections.unmodifiableMap(gateConfig);
		this.cmStatus = Collections.unmodifiableMap(cmStatus);
		handlerMap = new ConcurrentHashMap<>();
	}

	@Override
	public void stopServer() {
		for (final IPCMMClientHandler handler : handlerMap.values()) {
			handler.stop();
		}
		super.stopServer();
	}

	@Override
	protected IPCMMClientHandler getPCMMClientHandler(final Socket socket) throws IOException {
		final String key = socket.getLocalAddress().getHostName() + ':' + socket.getPort();
		if (handlerMap.get(key) == null) {
			final IPCMMClientHandler handler = new CmtsPcmmClientHandler(socket, gateConfig, cmStatus);
			handler.connect();
			handlerMap.put(key, handler);
			return handler;
		} else return handlerMap.get(key);
	}

	/**
	 * To start a CMTS
	 * @param args - the arguments which will contain configuration information
	 * @throws IOException - should the server fail to start for reasons such as port contention.
	 */
	public static void main(final String[] args) throws IOException {
		final CmtsYaml config = getConfig(args[0]);
		final CMTS cmts = new CMTS(config.port, config.getGates(), config.getCmStatus());
		cmts.startServer();
	}

	/**
	 * Returns the object that represents the YAML file
	 * @param uri - the location of the YAML file
	 * @return - the config object
	 * @throws IOException - when the URI does not contain the proper YAML file
	 */
	private static CmtsYaml getConfig(final String uri) throws IOException {
		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.readValue(new FileInputStream(uri), CmtsYaml.class);
	}

	/**
	 * Class to hold configuration settings in a YAML file
	 */
	public static class CmtsYaml {
		@JsonProperty("port")
		private int port;

		@JsonProperty("gates")
		private Collection<GateConfigYaml> gateConfigs;

		@JsonProperty("cmStatuses")
		private Collection<CmStatusYaml> cmStatuses;

		public Map<Direction, Set<String>> getGates() {
			final Map<Direction, Set<String>> out = new HashMap<>();

			for (final GateConfigYaml gateConfig : gateConfigs) {
				final Direction direction;
				if (gateConfig.gateType.equalsIgnoreCase("UPSTREAM")) {
					direction = Direction.UPSTREAM;
				} else if (gateConfig.gateType.equalsIgnoreCase("DOWNSTREAM")) {
					direction = Direction.DOWNSTREAM;
				} else direction = null;

				if (direction != null) {
					out.put(direction, gateConfig.gateNames);
				}
			}
			return out;
		}

		public Map<String, Boolean> getCmStatus() {
			final Map<String, Boolean> out = new HashMap<>();

			for (final CmStatusYaml cmStatus : cmStatuses) {
				out.put(cmStatus.hostIp, cmStatus.status);
			}
			return out;
		}
	}

	/**
	 * Class to hold the YAML gate configuration values
	 */
	public static class GateConfigYaml {
		@JsonProperty("type")
		private String gateType;

		@JsonProperty("names")
		private Set<String> gateNames;
	}

	/**
	 * Class to hold the YAML Cable Modem configuration values
	 */
	public static class CmStatusYaml {
		@JsonProperty("host")
		private String hostIp;

		@JsonProperty("status")
		private boolean status;
	}

}
