/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws2.mq;

import java.util.Map;

import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.mq.MqClient;
import software.amazon.awssdk.services.mq.MqClientBuilder;
import software.amazon.awssdk.services.mq.model.ListBrokersRequest;

public class MQ2ComponentVerifierExtension extends DefaultComponentVerifierExtension {

    public MQ2ComponentVerifierExtension() {
        this("aws2-mq");
    }

    public MQ2ComponentVerifierExtension(String scheme) {
        super(scheme);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {

        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS).error(ResultErrorHelper.requiresOption("accessKey", parameters))
            .error(ResultErrorHelper.requiresOption("secretKey", parameters)).error(ResultErrorHelper.requiresOption("region", parameters));

        // Validate using the catalog

        super.verifyParametersAgainstCatalog(builder, parameters);

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

        try {
            MQ2Configuration configuration = setProperties(new MQ2Configuration(), parameters);
            AwsBasicCredentials cred = AwsBasicCredentials.create(configuration.getAccessKey(), configuration.getSecretKey());
            MqClientBuilder clientBuilder = MqClient.builder();
            MqClient client = clientBuilder.credentialsProvider(StaticCredentialsProvider.create(cred)).region(Region.of(configuration.getRegion())).build();
            client.listBrokers(ListBrokersRequest.builder().build());
        } catch (SdkClientException e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .detail("aws_mq_exception_message", e.getMessage()).detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

            builder.error(errorBuilder.build());
        } catch (Exception e) {
            builder.error(ResultErrorBuilder.withException(e).build());
        }
        return builder.build();
    }
}
