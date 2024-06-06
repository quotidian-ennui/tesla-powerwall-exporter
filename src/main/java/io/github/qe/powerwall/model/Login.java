package io.github.qe.powerwall.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
{
   "email": "{email}",
   "password": "{password}",
   "username": "customer",
   "clientInfo": {
     "timezone": "UTC"
   }
 }
*/
@RegisterForReflection
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Login {
  private String email;
  private String password;
  @Default private String username = "customer";
  @Default private Map<String, String> clientInfo = Map.of("timezone", "UTC");
}
