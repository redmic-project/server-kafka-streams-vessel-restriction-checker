package es.redmic.vesselrestrictionchecker.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import es.redmic.vesselrestrictionchecker.utils.StreamsApplicationUtils;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public abstract class StreamsApplicationBase {

	protected String schemaRegistryUrl;

	protected SchemaRegistryClient schemaRegistryClient;

	public StreamsApplicationBase(SchemaRegistryClient schemaRegistryClient, String schemaRegistryUrl) {

		this.schemaRegistryUrl = schemaRegistryUrl;

		this.schemaRegistryClient = schemaRegistryClient;
	}

	// @formatter:off

	protected static final String APP_ID = "APP_ID",
			BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS",
			SCHEMA_REGISTRY = "SCHEMA_REGISTRY",
			AUTO_OFFSET_RESET = "AUTO_OFFSET_RESET";
	// @formatter:on

	@SuppressWarnings("serial")
	protected static HashMap<String, String> requiredVariablesBase = new HashMap<String, String>() {
		{
			put(APP_ID, "Stream application identifier");
			put(BOOTSTRAP_SERVERS, "Kafka servers");
			put(SCHEMA_REGISTRY, "Schema registry server");
			put(AUTO_OFFSET_RESET, "auto.offset.reset consumer property");
		}
	};

	public void startStreams(Topology topology, Properties props) {

		KafkaStreams streams = new KafkaStreams(topology, props);

		streams.setUncaughtExceptionHandler(
				(Thread thread, Throwable throwable) -> uncaughtException(thread, throwable, streams));

		streams.start();

		addShutdownHookAndBlock(streams);
	}

	public Properties getKafkaProperties(String appId, String bootstrapServers, String autoOffsetReset) {

		// Sobrescribir método o añadir aquí properties específicas si fuera necesario
		return StreamsApplicationUtils.getStreamConfig(appId, bootstrapServers, schemaRegistryUrl, autoOffsetReset);
	}

	protected void addShutdownHookAndBlock(KafkaStreams streams) {

		Thread.currentThread().setUncaughtExceptionHandler((t, e) -> uncaughtException(t, e, streams));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Stopping stream. SIGTERM signal");
				streams.close();
			}
		}));
	}

	protected void uncaughtException(Thread thread, Throwable throwable, KafkaStreams streams) {

		System.err.println("Error. The stream will stop working " + throwable.getLocalizedMessage());
		throwable.printStackTrace();
		streams.close();
	}

	protected static Map<String, Object> getEnvVariables(HashMap<String, String> variablesRequired) {

		Map<String, Object> envVariables = new HashMap<>();

		for (String key : variablesRequired.keySet()) {

			String value = System.getenv(key);

			if (value == null) {
				System.err.println("Error=Enviroment variable " + key + " not assigned. Description: "
						+ variablesRequired.get(key));
				System.exit(1);
			}
			envVariables.put(key, value);
		}

		return envVariables;
	}

	protected <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde() {

		final SpecificAvroSerde<T> valueSerde = new SpecificAvroSerde<>(schemaRegistryClient);
		valueSerde.configure(getSerdeConfig(), false);
		return valueSerde;
	}

	protected GenericAvroSerde getGenericAvroSerde() {

		final GenericAvroSerde valueSerde = new GenericAvroSerde(schemaRegistryClient);
		valueSerde.configure(getSerdeConfig(), false);
		return valueSerde;
	}

	private Map<String, String> getSerdeConfig() {
		return Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
	}
}
