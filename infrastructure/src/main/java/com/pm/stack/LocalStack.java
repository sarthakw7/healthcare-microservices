package com.pm.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalStack extends Stack {

        private final Vpc vpc;
        private final Cluster ecsCluster;

        public LocalStack(final App scope, final String id, final StackProps props) {
                super(scope, id, props);

                this.vpc = createVpc();

                DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
                DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");

                CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");
                CfnHealthCheck patientDbHealthCheck = createDbHealthCheck(patientServiceDb,
                                "PatientServiceDBHealthCheck");

                CfnCluster mskCluster = createMskCluster();

                this.ecsCluster = createEcsCluster();

                FargateService authService = createFargateService("AuthService",
                                "auth-service:latest", List.of(4005),
                                authServiceDb, "auth-service-db",
                                Map.of("JWT_SECRET", "UxGiBTtF2GNLy6VRvufXxdvZqB2neaBMuGY1MvrV3rI="));

                authService.getNode().addDependency(authDbHealthCheck);
                authService.getNode().addDependency(authServiceDb);

                FargateService billingService = createFargateService("BillingService",
                                "billing-service:latest",
                                List.of(4001, 9001),
                                null, null,
                                null);

                FargateService analyticsService = createFargateService("AnalyticsService",
                                "analytics-service:latest",
                                List.of(4002),
                                null, null,
                                null);

                analyticsService.getNode().addDependency(mskCluster);

                FargateService patientService = createFargateService("PatientService",
                                "patient-service:latest",
                                List.of(4000),
                                patientServiceDb, "patient-service-db",
                                Map.of(
                                                "BILLING_SERVICE_ADDRESS", "host.docker.internal",
                                                "BILLING_SERVICE_GRPC_PORT", "9001"));

                patientService.getNode().addDependency(patientServiceDb);
                patientService.getNode().addDependency(patientDbHealthCheck);
                patientService.getNode().addDependency(billingService);
                patientService.getNode().addDependency(mskCluster);

                createApiGatewayService();

        }

        private Vpc createVpc() {
                return new Vpc(this, "PatientManagementVPC", VpcProps.builder()
                                .vpcName("PatientManagementVPC")
                                .maxAzs(2)
                                .build());
        }

        private DatabaseInstance createDatabase(String id, String dbName) {
                return new DatabaseInstance(this, id, DatabaseInstanceProps.builder()
                                .engine(DatabaseInstanceEngine.postgres(
                                                PostgresInstanceEngineProps.builder()
                                                                .version(PostgresEngineVersion.VER_15_4)
                                                                .build()))
                                .vpc(vpc)
                                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                                .allocatedStorage(20)
                                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                                .databaseName(dbName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .build());
        }

        private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
                return CfnHealthCheck.Builder.create(this, id)
                                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                                                .type("TCP")
                                                .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                                                .requestInterval(30)
                                                .failureThreshold(3)
                                                .build())
                                .build();
        }

        private CfnCluster createMskCluster() {
                return CfnCluster.Builder.create(this, "MskCluster")
                                .clusterName("kafka-cluster")
                                .kafkaVersion("2.8.0")
                                .numberOfBrokerNodes(2) // ✅ Must be multiple of AZ count (2)
                                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                                                .instanceType("kafka.m5.xlarge")
                                                .clientSubnets(vpc.getPrivateSubnets().stream()
                                                                .map(ISubnet::getSubnetId)
                                                                .collect(Collectors.toList()))
                                                .brokerAzDistribution("DEFAULT").build())
                                .build();
        }

        private Cluster createEcsCluster() {
                return Cluster.Builder.create(this, "PatientManagementCluster")
                                .vpc(vpc)
                                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                                                .name("patient-management.local")
                                                .build())
                                .build();

        }

        private FargateService createFargateService(String id, String imageName, List<Integer> ports,
                        DatabaseInstance db, String dbName, Map<String, String> additionalEnvVars) {

                FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
                                .cpu(256)
                                .memoryLimitMiB(512)
                                .build();

                ContainerDefinitionOptions.Builder containerOptions = ContainerDefinitionOptions.builder()
                                .image(ContainerImage.fromRegistry(imageName))
                                .portMappings(ports.stream()
                                                .map(port -> PortMapping.builder()
                                                                .containerPort(port)
                                                                .hostPort(port)
                                                                .protocol(Protocol.TCP)
                                                                .build())
                                                .toList())
                                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                                .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                                                .logGroupName("/ecs/" + imageName)
                                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                                .retention(RetentionDays.ONE_DAY)
                                                                .build())
                                                .streamPrefix(imageName)
                                                .build()));

                Map<String, String> envVars = new HashMap<>();
                envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS",
                                "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

                if (additionalEnvVars != null) {
                        envVars.putAll(additionalEnvVars);
                }

                if (db != null) {
                        envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s".formatted(
                                        db.getDbInstanceEndpointAddress(),
                                        db.getDbInstanceEndpointPort(),
                                        dbName));

                        envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
                        envVars.put("SPRING_DATASOURCE_PASSWORD",
                                        db.getSecret().secretValueFromJson("password").toString());
                        envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
                        envVars.put("SPRING_SQL_INIT_MODE", "always");
                        envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
                }

                containerOptions.environment(envVars);
                taskDefinition.addContainer(imageName.replace(":", "-") + "Container", containerOptions.build());

                return FargateService.Builder.create(this, id)
                                .cluster(ecsCluster)
                                .taskDefinition(taskDefinition)
                                .assignPublicIp(false)
                                .serviceName(imageName)
                                .build();

        }

        private void createApiGatewayService() {

                FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                                .create(this, "APIGatewayTaskDefinition")
                                .cpu(256)
                                .memoryLimitMiB(512)
                                .build();

                ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions.builder()
                                .image(ContainerImage.fromRegistry("api-gateway-service:latest"))
                                .environment(Map.of(
                                                "SPRING_PROFILES_ACTIVE", "prod",
                                                "AUTH_SERVICE_URL", "http://host.docker.internal:4005"))
                                .portMappings(List.of(4004).stream()
                                                .map(port -> PortMapping.builder()
                                                                .containerPort(port)
                                                                .hostPort(port)
                                                                .protocol(Protocol.TCP)
                                                                .build())
                                                .toList())
                                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                                .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                                                .logGroupName("/ecs/api-gateway")
                                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                                .retention(RetentionDays.ONE_DAY)
                                                                .build())
                                                .streamPrefix("api-gateway")
                                                .build()))
                                .build();

                taskDefinition.addContainer("APIGatewayContainer", containerOptions);

                ApplicationLoadBalancedFargateService apiGateway = ApplicationLoadBalancedFargateService.Builder
                                .create(this, "APIGatewayService")
                                .cluster(ecsCluster)
                                .serviceName("api-gateway")
                                .taskDefinition(taskDefinition)
                                .desiredCount(1)
                                .healthCheckGracePeriod(Duration.seconds(60))
                                .build();
        }

        public static void main(String[] args) {
                App app = new App(AppProps.builder().outdir("./cdk.out").build());

                StackProps props = StackProps.builder()
                                .synthesizer(new BootstraplessSynthesizer())
                                .build();

                new LocalStack(app, "localstack", props);
                app.synth();
                System.out.println("App synthesizing in progress...");
        }
}
