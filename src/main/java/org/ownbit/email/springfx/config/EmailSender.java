package org.ownbit.email.springfx.config;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class EmailSender {

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private EmailConfigExternal emailConfigExternal;

  public void sendEmail(String emailBodyText, InternetAddress toRecipient, String fromEmail, String subject, List<File> attachedFiles) throws MessagingException, Exception {

    getRightJavaMailSender(); //get java mail based on external config - if exists, else get internal config based

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
    messageHelper.setFrom(toInternetAddress(fromEmail, null));
    messageHelper.setTo(toRecipient);
    messageHelper.setText(emailBodyText, true);
    messageHelper.setSubject(subject);
    messageHelper.setSentDate(new Date());
    messageHelper.setPriority(1);

    if (CollectionUtils.isNotEmpty(attachedFiles)) {
      attachedFiles.forEach(file -> {
        try {
          messageHelper.addAttachment(file.getName(), file);
        } catch (MessagingException e) {
          log.error("The attached files can't be parsed: " + file.getAbsolutePath(), e);
        }
      });
    }

    this.mailSender.send(message);
  }

  private void getRightJavaMailSender() {
    if (emailConfigExternal != null && StringUtils.isNotBlank(emailConfigExternal.getHost()) && StringUtils.isNotBlank(emailConfigExternal.getPort()) &&
      StringUtils.isNotBlank(emailConfigExternal.getUsername()) && StringUtils.isNotBlank(emailConfigExternal.getPassword())) {

      JavaMailSenderImpl externalMailSender = new JavaMailSenderImpl();
      externalMailSender.setHost(emailConfigExternal.getHost());
      externalMailSender.setPort(Integer.valueOf(emailConfigExternal.getPort()));
      externalMailSender.setUsername(emailConfigExternal.getUsername());
      externalMailSender.setPassword(emailConfigExternal.getPassword());

      Properties props = externalMailSender.getJavaMailProperties();
      props.put("mail.transport.protocol", emailConfigExternal.getTransportProtocol());
      props.put("mail.smtp.auth", emailConfigExternal.getSmtpAuth());
      props.put("mail.smtp.starttls.enable", emailConfigExternal.getStartTLSEnabled());
      props.put("mail.smtp.starttls.required", BooleanUtils.isTrue(emailConfigExternal.getStartTLSRequired()));
      props.put("mail.smtp.ehlo", BooleanUtils.isTrue(emailConfigExternal.getEhloEnabled()));
      props.put("mail.mime.charset", "UTF-8");
      
      this.mailSender = externalMailSender;
    }
  }

  public Optional<InternetAddress[]> getValidRecipients(String allRecipients) {

    Optional<InternetAddress[]> validRecipients = Optional.empty();

    if (StringUtils.isNotBlank(allRecipients)) {

      List<String> recipientsSplittedByNewLinen = Arrays.asList(allRecipients.trim().split("\n"));
      if (CollectionUtils.isNotEmpty(recipientsSplittedByNewLinen)) {

        InternetAddress[] internetAddresses = new InternetAddress[recipientsSplittedByNewLinen.size()];

        for (int i = 0; i < recipientsSplittedByNewLinen.size(); i++) {
          try {
            InternetAddress emailAddr = new InternetAddress(recipientsSplittedByNewLinen.get(i));
            emailAddr.validate();

            internetAddresses[i] = emailAddr; //add valid email
          } catch (AddressException ex) {
            break;
          }
        }
        validRecipients = Optional.of(internetAddresses);
      }
    }
    return validRecipients;
  }

  public boolean isValidEmail(String email) {
    try {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return true;
    } catch (AddressException ex) {
      return false;
    }
  }

  protected InternetAddress toInternetAddress(String email, String displayName) throws Exception {
    if (email == null || "".equals(email.trim())) {
      throw new Exception("Please provide a valid address", null);
    }
    if (displayName == null || "".equals(displayName.trim())) {
      return new InternetAddress(email);
    }
    return new InternetAddress(email, displayName, "utf-8");
  }
}
