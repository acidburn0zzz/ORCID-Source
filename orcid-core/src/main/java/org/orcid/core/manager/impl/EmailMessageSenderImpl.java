package org.orcid.core.manager.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.LocaleUtils;
import org.orcid.core.locale.LocaleManager;
import org.orcid.core.manager.EmailMessage;
import org.orcid.core.manager.EmailMessageSender;
import org.orcid.core.manager.EncryptionManager;
import org.orcid.core.manager.NotificationManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.TemplateManager;
import org.orcid.core.manager.v3.read_only.EmailManagerReadOnly;
import org.orcid.jaxb.model.common_v2.SourceClientId;
import org.orcid.jaxb.model.notification.amended_v2.NotificationAmended;
import org.orcid.jaxb.model.notification.permission_v2.NotificationPermission;
import org.orcid.jaxb.model.notification_v2.Notification;
import org.orcid.model.notification.institutional_sign_in_v2.NotificationInstitutionalConnection;
import org.orcid.persistence.dao.EmailDao;
import org.orcid.persistence.dao.NotificationDao;
import org.orcid.persistence.jpa.entities.EmailEntity;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.pojo.DigestEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

/**
 * 
 * @author Will Simpson
 * 
 */
public class EmailMessageSenderImpl implements EmailMessageSender {

    private static final String DIGEST_FROM_ADDRESS = "update@notify.orcid.org";
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailMessageSenderImpl.class);

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private NotificationDao notificationDaoReadOnly;

    @Resource
    private NotificationManager notificationManager;

    @Resource
    private MailGunManager mailGunManager;

    @Resource
    private EmailDao emailDao;
    
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private TemplateManager templateManager;

    @Resource
    private LocaleManager localeManager;

    @Resource(name = "messageSource")
    private MessageSource messages;

    @Resource
    private OrcidUrlManager orcidUrlManager;

    @Resource
    private EncryptionManager encryptionManager;

    @Resource
    private ProfileEntityCacheManager profileEntityCacheManager;
    
    @Resource(name = "emailManagerReadOnlyV3")
    private EmailManagerReadOnly emailManagerReadOnly;
    
    @Override
    public EmailMessage createDigest(String orcid, Collection<Notification> notifications) {
        ProfileEntity record = profileEntityCacheManager.retrieve(orcid);                
        Locale locale = getUserLocaleFromProfileEntity(record);
        int totalMessageCount = 0;
        int orcidMessageCount = 0;
        int addActivitiesMessageCount = 0;
        int amendedMessageCount = 0;
        int activityCount = 0;
        Set<String> memberIds = new HashSet<>();
        DigestEmail digestEmail = new DigestEmail();
        for (Notification notification : notifications) {
            digestEmail.addNotification(notification);
            totalMessageCount++;
            if (notification.getSource() == null) {
                orcidMessageCount++;
            } else {
                SourceClientId clientId = notification.getSource().getSourceClientId();
                if (clientId != null) {
                    memberIds.add(clientId.getPath());
                }
            }
            if (notification instanceof NotificationPermission) {
                addActivitiesMessageCount++;
                NotificationPermission permissionNotification = (NotificationPermission) notification;
                activityCount += permissionNotification.getItems().getItems().size();
                permissionNotification.setEncryptedPutCode(encryptAndEncodePutCode(permissionNotification.getPutCode()));
            } else if (notification instanceof NotificationInstitutionalConnection) {
                notification.setEncryptedPutCode(encryptAndEncodePutCode(notification.getPutCode()));
            } else if (notification instanceof NotificationAmended) {
                amendedMessageCount++;
            }
        }
        String emailName = notificationManager.deriveEmailFriendlyName(record);
        String subject = messages.getMessage("email.subject.digest", new String[] { emailName, String.valueOf(totalMessageCount) }, locale);
        Map<String, Object> params = new HashMap<>();
        params.put("locale", locale);
        params.put("messages", messages);
        params.put("messageArgs", new Object[0]);        
        params.put("emailName", emailName);
        params.put("digestEmail", digestEmail);
        params.put("emailFrequencyString", String.valueOf(record.getSendEmailFrequencyDays()));
        params.put("totalMessageCount", String.valueOf(totalMessageCount));
        params.put("orcidMessageCount", orcidMessageCount);
        params.put("addActivitiesMessageCount", addActivitiesMessageCount);
        params.put("activityCount", activityCount);
        params.put("amendedMessageCount", amendedMessageCount);
        params.put("memberIdsCount", memberIds.size());
        params.put("baseUri", orcidUrlManager.getBaseUrl());
        params.put("subject", subject);
        String bodyText = templateManager.processTemplate("digest_email.ftl", params, locale);
        String bodyHtml = templateManager.processTemplate("digest_email_html.ftl", params, locale);

        EmailMessage emailMessage = new EmailMessage();

        emailMessage.setSubject(subject);
        emailMessage.setBodyText(bodyText);
        emailMessage.setBodyHtml(bodyHtml);
        return emailMessage;
    }

    private Locale getUserLocaleFromProfileEntity(ProfileEntity profile) {
        String locale = profile.getLocale();
        if (locale != null) {
            org.orcid.jaxb.model.common_v2.Locale loc = org.orcid.jaxb.model.common_v2.Locale.valueOf(locale);
            return LocaleUtils.toLocale(loc.value());
        }
        
        return LocaleUtils.toLocale("en");
    }
    
    private String encryptAndEncodePutCode(Long putCode) {
        String encryptedPutCode = encryptionManager.encryptForExternalUse(String.valueOf(putCode));
        try {
            return Base64.encodeBase64URLSafeString(encryptedPutCode.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Problem base 64 encoding notification put code for notification id = " + putCode, e);
        }
    }

    @Override
    public void sendEmailMessages() {
        LOGGER.info("About to send email messages");
        List<Object[]>orcidsWithUnsentNotifications = notificationDaoReadOnly.findRecordsWithUnsentNotifications();
        for (final Object[] element : orcidsWithUnsentNotifications) {
            String orcid = (String) element[0];                        
            try {
                Float emailFrequencyDays = Float.valueOf((float) element[1]);
                Date recordActiveDate = (Date) element[2];
                LOGGER.info("Sending messages for orcid: {}", orcid);
                List<Notification> notifications = notificationManager.findNotificationsToSend(orcid, emailFrequencyDays, recordActiveDate);
                if(!notifications.isEmpty()) {
                    LOGGER.info("Found {} messages to send for orcid: {}", notifications.size(), orcid);
                    EmailMessage digestMessage = createDigest(orcid, notifications);
                    digestMessage.setFrom(DIGEST_FROM_ADDRESS);
                    EmailEntity primaryEmail = emailDao.findPrimaryEmail(orcid);
                    if (primaryEmail == null) {
                        LOGGER.info("No primary email for orcid: " + orcid);
                        return;
                    }
                    digestMessage.setTo(primaryEmail.getId());
                    boolean successfullySent = mailGunManager.sendEmail(digestMessage.getFrom(), digestMessage.getTo(), digestMessage.getSubject(),
                            digestMessage.getBodyText(), digestMessage.getBodyHtml());
                    if (successfullySent) {
                        flagAsSent(notifications);
                    }
                } else {
                    LOGGER.info("There are no notifications to send for orcid: {}", orcid);
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Problem sending email message to user: " + orcid, e);
            }
        }
        LOGGER.info("Finished sending email messages");
    }

    private void flagAsSent(List<Notification> notifications) {
        List<Long> notificationIds = new ArrayList<>();
        for (Notification notification : notifications) {
            notificationIds.add(notification.getPutCode());
        }
        List<List<Long>> batches = Lists.partition(notificationIds, 30000);
        for (List<Long> batch : batches) {
            notificationDao.flagAsSent(batch);
        }
        notificationDao.flush();
    }

}
