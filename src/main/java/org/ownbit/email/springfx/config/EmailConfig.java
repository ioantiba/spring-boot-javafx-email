package org.ownbit.email.springfx.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Configuration
@Getter
@Setter
@Log4j2
public class EmailConfig {

  /** The host. */
  @Value("${spring.mail.host}")
  private String host;

  /** The port. */
  @Value("${spring.mail.port}")
  private Integer port;

  /** The username. */
  @Value("${spring.mail.username}")
  private String username;

  /** The password. */
  @Value("${spring.mail.password}")
  private String password;
  
  /** The transport protocol. */
  @Value("${spring.mail.properties.mail.protocol}")
  private String transportProtocol;

  /** The smtp auth. */
  @Value("${spring.mail.properties.mail.smtp.auth}")
  private Boolean smtpAuth;

  /** The start TLS enabled. */
  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private Boolean startTLSEnabled;

  /** The start TLS required. */
  @Value("${spring.mail.properties.mail.smtp.starttls.required}")
  private Boolean startTLSRequired;

  /** The ssl enabled. */
  @Value("${spring.mail.properties.mail.smtp.ssl.enable}")
  private Boolean sslEnabled;
  
  /** The ehlo enabled. */
  @Value("${spring.mail.properties.mail.smtp.ehlo}")
  private Boolean ehloEnabled;

  /** The email debug. */
  @Value("${spring.mail.properties.mail.debug}")
  private Boolean emailDebug;

  @Bean
  public JavaMailSender javaMailSender() {

    log.debug("Creating bean: JavaMailSender");
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(this.host);
    mailSender.setPort(this.port);
    mailSender.setUsername(this.username);
    mailSender.setPassword(this.password);
    
    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", this.transportProtocol);
    props.put("mail.smtp.auth", this.smtpAuth);
    props.put("mail.smtp.starttls.enable", this.startTLSEnabled);
    props.put("mail.smtp.starttls.required", this.startTLSRequired);
    props.put("mail.debug", this.emailDebug);
    props.put("mail.smtp.ehlo", this.ehloEnabled);
    props.put("mail.mime.charset", "UTF-8");
    
    return mailSender;
  }
}