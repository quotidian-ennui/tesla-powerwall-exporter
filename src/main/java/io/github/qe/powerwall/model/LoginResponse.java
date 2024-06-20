package io.github.qe.powerwall.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// {
//  "email": "emailaddr",
//  "firstname": "Tesla",
//  "lastname": "Energy",
//  "roles": [
//    "Home_Owner"
//  ],
//  "token": "xxx",
//  "provider": "Basic",
//  "loginTime": "2024-06-06T19:05:03.523642069+01:00"
// }
@RegisterForReflection
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
  private String email;
  private String firstname;
  private String lastname;
  private List<String> roles;
  private String token;
  private String provider;
  private String loginTime;
}
