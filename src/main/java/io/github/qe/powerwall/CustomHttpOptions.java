package io.github.qe.powerwall;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.http.HttpClientOptions;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import lombok.NoArgsConstructor;

@Provider
@NoArgsConstructor
@RegisterForReflection
public class CustomHttpOptions implements ContextResolver<HttpClientOptions> {

  @Override
  public HttpClientOptions getContext(Class<?> aClass) {
    HttpClientOptions options = new HttpClientOptions();
    options.setTrustAll(true);
    options.setVerifyHost(false);
    return options;
  }
}
