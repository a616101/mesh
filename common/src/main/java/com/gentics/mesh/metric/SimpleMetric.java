package com.gentics.mesh.metric;

/**
 * Metrics which are used across Gentics Mesh. 
 */
public enum SimpleMetric implements Metric {

	TX("tx_created", "Meter which measures the rate of created transactions over time."),

	NO_TX("notx_created", "Meter which measures the rate of created noTx transactions over time."),

	TX_TIME("tx_time", "Timer which tracks transaction durations."),

	TX_RETRY("tx_retry", "Amount of transaction retries which happen if a conflict has been encountered."),

	GRAPH_ELEMENT_RELOAD("graph_element_reload", "Meter which tracks the reload operations on used vertices."),

	NODE_MIGRATION_PENDING("node_migration_pending", "Pending contents which need to be processed by the node migration.");

	private String key;

	private String description;

	private SimpleMetric(String key, String description) {
		this.key = key;
		this.description = description;
	}

	public String key() {
		return "mesh_" + key;
	}

	public String description() {
		return description;
	}
}
