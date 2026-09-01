package br.com.itau.challenge.hello.adapter.output.dynamodb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

	@Bean
	public DynamoDbClient dynamoDbClient(
			@Value("${dynamodb.endpoint}") String endpoint,
			@Value("${dynamodb.region}") String region) {
		return DynamoDbClient
				.builder()
				.endpointOverride(URI.create(endpoint))
				.region(Region.of(region))
				.credentialsProvider(
						StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
				.build();
	}
}
