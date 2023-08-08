package org.ownbit.email.springfx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class EmailConfigExternal {

  /** The host. */
  @Value("${mail.host:#{null}}")
  private String host;

  /** The port. */
  @Value("${mail.port:#{null}}")
  private String port;

  /** The username. */
  @Value("${mail.username:#{null}}")
  private String username;

  /** The password. */
  @Value("${mail.password:#{null}}")
  private String password;
  
  /** The transport protocol. */
  @Value("${mail.properties.mail.protocol:#{null}}")
  private String transportProtocol;

  /** The smtp auth. */
  @Value("${mail.properties.mail.smtp.auth:#{null}}")
  private Boolean smtpAuth;

  /** The start TLS enabled. */
  @Value("${mail.properties.mail.smtp.starttls.enable:#{null}}")
  private Boolean startTLSEnabled;

  /** The start TLS required. */
  @Value("${mail.properties.mail.smtp.starttls.required:#{null}}")
  private Boolean startTLSRequired;
  
  /** The ehlo enabled. */
  @Value("${mail.properties.mail.smtp.ehlo:#{null}}")
  private Boolean ehloEnabled;
  
  /** The ssl enabled. */
  @Value("${mail.properties.mail.smtp.ssl.enable:#{null}}")
  private Boolean sslEnabled;
}