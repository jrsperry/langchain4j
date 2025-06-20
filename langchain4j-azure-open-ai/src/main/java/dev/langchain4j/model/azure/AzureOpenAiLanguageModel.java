package dev.langchain4j.model.azure;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.model.azure.InternalAzureOpenAiHelper.*;
import static dev.langchain4j.spi.ServiceHelper.loadFactories;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.ProxyOptions;
import dev.langchain4j.model.azure.spi.AzureOpenAiLanguageModelBuilderFactory;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.output.Response;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an OpenAI language model, hosted on Azure, such as gpt-3.5-turbo-instruct.
 * However, it's recommended to use {@link AzureOpenAiChatModel} instead,
 * as it offers more advanced features like function calling, multi-turn conversations, etc.
 * <p>
 * Mandatory parameters for initialization are: endpoint and apikey (or an alternate authentication method, see below for more information).
 * Optionally you can set serviceVersion (if not, the latest version is used) and deploymentName (if not, a default name is used).
 * You can also provide your own OpenAIClient instance, if you need more flexibility.
 * <p>
 * There are 3 authentication methods:
 * <p>
 * 1. Azure OpenAI API Key Authentication: this is the most common method, using an Azure OpenAI API key.
 * You need to provide the OpenAI API Key as a parameter, using the apiKey() method in the Builder, or the apiKey parameter in the constructor:
 * For example, you would use `builder.apiKey("{key}")`.
 * <p>
 * 2. non-Azure OpenAI API Key Authentication: this method allows to use the OpenAI service, instead of Azure OpenAI.
 * You can use the nonAzureApiKey() method in the Builder, which will also automatically set the endpoint to "https://api.openai.com/v1".
 * For example, you would use `builder.nonAzureApiKey("{key}")`.
 * The constructor requires a KeyCredential instance, which can be created using `new AzureKeyCredential("{key}")`, and doesn't set up the endpoint.
 * <p>
 * 3. Azure OpenAI client with Microsoft Entra ID (formerly Azure Active Directory) credentials.
 * - This requires to add the `com.azure:azure-identity` dependency to your project, which is an optional dependency to this library.
 * - You need to provide a TokenCredential instance, using the tokenCredential() method in the Builder, or the tokenCredential parameter in the constructor.
 * As an example, DefaultAzureCredential can be used to authenticate the client: Set the values of the client ID, tenant ID, and
 * client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.
 * Then, provide the DefaultAzureCredential instance to the builder: `builder.tokenCredential(new DefaultAzureCredentialBuilder().build())`.
 */
public class AzureOpenAiLanguageModel implements LanguageModel {

    private static final Logger logger = LoggerFactory.getLogger(AzureOpenAiLanguageModel.class);

    private OpenAIClient client;
    private final String deploymentName;
    private final Integer maxTokens;
    private final Double temperature;
    private final Double topP;
    private final Map<String, Integer> logitBias;
    private final String user;
    private final Integer logprobs;
    private final Boolean echo;
    private final List<String> stop;
    private final Double presencePenalty;
    private final Double frequencyPenalty;
    private final Integer bestOf;

    public AzureOpenAiLanguageModel(
            OpenAIClient client,
            String deploymentName,
            Integer maxTokens,
            Double temperature,
            Double topP,
            Map<String, Integer> logitBias,
            String user,
            Integer logprobs,
            Boolean echo,
            List<String> stop,
            Double presencePenalty,
            Double frequencyPenalty,
            Integer bestOf) {

        this(
                deploymentName,
                maxTokens,
                temperature,
                topP,
                logitBias,
                user,
                logprobs,
                echo,
                stop,
                presencePenalty,
                frequencyPenalty,
                bestOf);
        this.client = client;
    }

    public AzureOpenAiLanguageModel(
            String endpoint,
            String serviceVersion,
            String apiKey,
            HttpClientProvider httpClientProvider,
            String deploymentName,
            Integer maxTokens,
            Double temperature,
            Double topP,
            Map<String, Integer> logitBias,
            String user,
            Integer logprobs,
            Boolean echo,
            List<String> stop,
            Double presencePenalty,
            Double frequencyPenalty,
            Integer bestOf,
            Duration timeout,
            Integer maxRetries,
            ProxyOptions proxyOptions,
            boolean logRequestsAndResponses,
            String userAgentSuffix,
            Map<String, String> customHeaders) {

        this(
                deploymentName,
                maxTokens,
                temperature,
                topP,
                logitBias,
                user,
                logprobs,
                echo,
                stop,
                presencePenalty,
                frequencyPenalty,
                bestOf);
        this.client = setupSyncClient(
                endpoint,
                serviceVersion,
                apiKey,
                timeout,
                maxRetries,
                httpClientProvider,
                proxyOptions,
                logRequestsAndResponses,
                userAgentSuffix,
                customHeaders);
    }

    public AzureOpenAiLanguageModel(
            String endpoint,
            String serviceVersion,
            KeyCredential keyCredential,
            HttpClientProvider httpClientProvider,
            String deploymentName,
            Integer maxTokens,
            Double temperature,
            Double topP,
            Map<String, Integer> logitBias,
            String user,
            Integer logprobs,
            Boolean echo,
            List<String> stop,
            Double presencePenalty,
            Double frequencyPenalty,
            Integer bestOf,
            Duration timeout,
            Integer maxRetries,
            ProxyOptions proxyOptions,
            boolean logRequestsAndResponses,
            String userAgentSuffix,
            Map<String, String> customHeaders) {

        this(
                deploymentName,
                maxTokens,
                temperature,
                topP,
                logitBias,
                user,
                logprobs,
                echo,
                stop,
                presencePenalty,
                frequencyPenalty,
                bestOf);
        this.client = setupSyncClient(
                endpoint,
                serviceVersion,
                keyCredential,
                timeout,
                maxRetries,
                httpClientProvider,
                proxyOptions,
                logRequestsAndResponses,
                userAgentSuffix,
                customHeaders);
    }

    public AzureOpenAiLanguageModel(
            String endpoint,
            String serviceVersion,
            TokenCredential tokenCredential,
            HttpClientProvider httpClientProvider,
            String deploymentName,
            Integer maxTokens,
            Double temperature,
            Double topP,
            Map<String, Integer> logitBias,
            String user,
            Integer logprobs,
            Boolean echo,
            List<String> stop,
            Double presencePenalty,
            Double frequencyPenalty,
            Integer bestOf,
            Duration timeout,
            Integer maxRetries,
            ProxyOptions proxyOptions,
            boolean logRequestsAndResponses,
            String userAgentSuffix,
            Map<String, String> customHeaders) {

        this(
                deploymentName,
                maxTokens,
                temperature,
                topP,
                logitBias,
                user,
                logprobs,
                echo,
                stop,
                presencePenalty,
                frequencyPenalty,
                bestOf);
        this.client = setupSyncClient(
                endpoint,
                serviceVersion,
                tokenCredential,
                timeout,
                maxRetries,
                httpClientProvider,
                proxyOptions,
                logRequestsAndResponses,
                userAgentSuffix,
                customHeaders);
    }

    private AzureOpenAiLanguageModel(
            String deploymentName,
            Integer maxTokens,
            Double temperature,
            Double topP,
            Map<String, Integer> logitBias,
            String user,
            Integer logprobs,
            Boolean echo,
            List<String> stop,
            Double presencePenalty,
            Double frequencyPenalty,
            Integer bestOf) {

        this.deploymentName = getOrDefault(deploymentName, "gpt-35-turbo-instruct");
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.topP = topP;
        this.logitBias = logitBias;
        this.user = user;
        this.logprobs = logprobs;
        this.echo = echo;
        this.stop = stop;
        this.presencePenalty = presencePenalty;
        this.frequencyPenalty = frequencyPenalty;
        this.bestOf = bestOf;
    }

    @Override
    public Response<String> generate(String prompt) {

        CompletionsOptions options = new CompletionsOptions(Collections.singletonList(prompt))
                .setModel(deploymentName)
                .setMaxTokens(maxTokens)
                .setTemperature(temperature)
                .setTopP(topP)
                .setLogitBias(logitBias)
                .setUser(user)
                .setLogprobs(logprobs)
                .setEcho(echo)
                .setStop(stop)
                .setPresencePenalty(presencePenalty)
                .setFrequencyPenalty(frequencyPenalty)
                .setBestOf(bestOf);

        Completions completions = client.getCompletions(deploymentName, options);
        return Response.from(
                completions.getChoices().get(0).getText(),
                tokenUsageFrom(completions.getUsage()),
                finishReasonFrom(completions.getChoices().get(0).getFinishReason()));
    }

    public static Builder builder() {
        for (AzureOpenAiLanguageModelBuilderFactory factory :
                loadFactories(AzureOpenAiLanguageModelBuilderFactory.class)) {
            return factory.get();
        }
        return new Builder();
    }

    public static class Builder {

        private String endpoint;
        private String serviceVersion;
        private String apiKey;
        private KeyCredential keyCredential;
        private TokenCredential tokenCredential;
        private HttpClientProvider httpClientProvider;
        private String deploymentName;
        private Integer maxTokens;
        private Double temperature;
        private Double topP;
        private Map<String, Integer> logitBias;
        private String user;
        private Integer logprobs;
        private Boolean echo;
        private List<String> stop;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Integer bestOf;
        private Duration timeout;
        private Integer maxRetries;
        private ProxyOptions proxyOptions;
        private boolean logRequestsAndResponses;
        private OpenAIClient openAIClient;
        private String userAgentSuffix;
        private Map<String, String> customHeaders;

        /**
         * Sets the Azure OpenAI endpoint. This is a mandatory parameter.
         *
         * @param endpoint The Azure OpenAI endpoint in the format: https://{resource}.openai.azure.com/
         * @return builder
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the Azure OpenAI API service version. This is a mandatory parameter.
         *
         * @param serviceVersion The Azure OpenAI API service version in the format: 2023-05-15
         * @return builder
         */
        public Builder serviceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        /**
         * Sets the Azure OpenAI API key.
         *
         * @param apiKey The Azure OpenAI API key.
         * @return builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Used to authenticate with the OpenAI service, instead of Azure OpenAI.
         * This automatically sets the endpoint to https://api.openai.com/v1.
         *
         * @param nonAzureApiKey The non-Azure OpenAI API key
         * @return builder
         */
        public Builder nonAzureApiKey(String nonAzureApiKey) {
            this.keyCredential = new KeyCredential(nonAzureApiKey);
            this.endpoint = "https://api.openai.com/v1";
            return this;
        }

        /**
         * Used to authenticate to Azure OpenAI with Azure Active Directory credentials.
         * @param tokenCredential the credentials to authenticate with Azure Active Directory
         * @return builder
         */
        public Builder tokenCredential(TokenCredential tokenCredential) {
            this.tokenCredential = tokenCredential;
            return this;
        }

        /**
         * Sets the {@code HttpClientProvider} to use for creating the HTTP client to communicate with the OpenAI api.
         *
         * @param httpClientProvider The {@code HttpClientProvider} to use
         * @return builder
         */
        public Builder httpClientProvider(HttpClientProvider httpClientProvider) {
            this.httpClientProvider = httpClientProvider;
            return this;
        }

        /**
         * Sets the deployment name in Azure OpenAI. This is a mandatory parameter.
         *
         * @param deploymentName The Deployment name.
         * @return builder
         */
        public Builder deploymentName(String deploymentName) {
            this.deploymentName = deploymentName;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder logitBias(Map<String, Integer> logitBias) {
            this.logitBias = logitBias;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder logprobs(Integer logprobs) {
            this.logprobs = logprobs;
            return this;
        }

        public Builder echo(Boolean echo) {
            this.echo = echo;
            return this;
        }

        public Builder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder bestOf(Integer bestOf) {
            this.bestOf = bestOf;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder proxyOptions(ProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;
            return this;
        }

        public Builder logRequestsAndResponses(boolean logRequestsAndResponses) {
            this.logRequestsAndResponses = logRequestsAndResponses;
            return this;
        }

        /**
         * Sets the Azure OpenAI client. This is an optional parameter, if you need more flexibility than using the endpoint, serviceVersion, apiKey, deploymentName parameters.
         *
         * @param openAIClient The Azure OpenAI client.
         * @return builder
         */
        public Builder openAIClient(OpenAIClient openAIClient) {
            this.openAIClient = openAIClient;
            return this;
        }

        public Builder userAgentSuffix(String userAgentSuffix) {
            this.userAgentSuffix = userAgentSuffix;
            return this;
        }

        public Builder customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public AzureOpenAiLanguageModel build() {
            if (openAIClient == null) {
                if (tokenCredential != null) {
                    return new AzureOpenAiLanguageModel(
                            endpoint,
                            serviceVersion,
                            tokenCredential,
                            httpClientProvider,
                            deploymentName,
                            maxTokens,
                            temperature,
                            topP,
                            logitBias,
                            user,
                            logprobs,
                            echo,
                            stop,
                            presencePenalty,
                            frequencyPenalty,
                            bestOf,
                            timeout,
                            maxRetries,
                            proxyOptions,
                            logRequestsAndResponses,
                            userAgentSuffix,
                            customHeaders);
                } else if (keyCredential != null) {
                    return new AzureOpenAiLanguageModel(
                            endpoint,
                            serviceVersion,
                            keyCredential,
                            httpClientProvider,
                            deploymentName,
                            maxTokens,
                            temperature,
                            topP,
                            logitBias,
                            user,
                            logprobs,
                            echo,
                            stop,
                            presencePenalty,
                            frequencyPenalty,
                            bestOf,
                            timeout,
                            maxRetries,
                            proxyOptions,
                            logRequestsAndResponses,
                            userAgentSuffix,
                            customHeaders);
                }
                return new AzureOpenAiLanguageModel(
                        endpoint,
                        serviceVersion,
                        apiKey,
                        httpClientProvider,
                        deploymentName,
                        maxTokens,
                        temperature,
                        topP,
                        logitBias,
                        user,
                        logprobs,
                        echo,
                        stop,
                        presencePenalty,
                        frequencyPenalty,
                        bestOf,
                        timeout,
                        maxRetries,
                        proxyOptions,
                        logRequestsAndResponses,
                        userAgentSuffix,
                        customHeaders);
            } else {
                return new AzureOpenAiLanguageModel(
                        openAIClient,
                        deploymentName,
                        maxTokens,
                        temperature,
                        topP,
                        logitBias,
                        user,
                        logprobs,
                        echo,
                        stop,
                        presencePenalty,
                        frequencyPenalty,
                        bestOf);
            }
        }
    }
}
