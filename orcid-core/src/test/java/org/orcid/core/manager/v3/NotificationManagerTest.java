package org.orcid.core.manager.v3;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.orcid.core.adapter.v3.JpaJaxbNotificationAdapter;
import org.orcid.core.adapter.v3.impl.JpaJaxbNotificationAdapterImpl;
import org.orcid.core.api.OrcidApiConstants;
import org.orcid.core.manager.EncryptionManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.impl.MailGunManager;
import org.orcid.core.manager.v3.impl.NotificationManagerImpl;
import org.orcid.core.oauth.OrcidOauth2TokenDetailService;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.v3.dev1.common.Locale;
import org.orcid.jaxb.model.v3.dev1.common.Source;
import org.orcid.jaxb.model.v3.dev1.notification.Notification;
import org.orcid.jaxb.model.v3.dev1.notification.NotificationType;
import org.orcid.jaxb.model.v3.dev1.notification.amended.AmendedSection;
import org.orcid.jaxb.model.v3.dev1.notification.custom.NotificationCustom;
import org.orcid.jaxb.model.v3.dev1.notification.permission.NotificationPermission;
import org.orcid.jaxb.model.v3.dev1.notification.permission.NotificationPermissions;
import org.orcid.jaxb.model.v3.dev1.record.Email;
import org.orcid.model.v3.dev1.notification.institutional_sign_in.NotificationInstitutionalConnection;
import org.orcid.persistence.dao.ClientDetailsDao;
import org.orcid.persistence.dao.GenericDao;
import org.orcid.persistence.dao.NotificationDao;
import org.orcid.persistence.dao.ProfileDao;
import org.orcid.persistence.dao.impl.NotificationDaoImpl;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.orcid.persistence.jpa.entities.EmailEntity;
import org.orcid.persistence.jpa.entities.EmailEventEntity;
import org.orcid.persistence.jpa.entities.EmailEventType;
import org.orcid.persistence.jpa.entities.NotificationCustomEntity;
import org.orcid.persistence.jpa.entities.NotificationEntity;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.persistence.jpa.entities.ProfileEventEntity;
import org.orcid.persistence.jpa.entities.RecordNameEntity;
import org.orcid.persistence.jpa.entities.SecurityQuestionEntity;
import org.orcid.persistence.jpa.entities.SourceEntity;
import org.orcid.test.DBUnitTest;
import org.orcid.test.OrcidJUnit4ClassRunner;
import org.orcid.test.TargetProxyHelper;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OrcidJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-orcid-core-context.xml" })
@Transactional
public class NotificationManagerTest extends DBUnitTest {

    private static final List<String> DATA_FILES = Arrays.asList("/data/EmptyEntityData.xml", "/data/SecurityQuestionEntityData.xml",
            "/data/SourceClientDetailsEntityData.xml", "/data/ProfileEntityData.xml", "/data/ClientDetailsEntityData.xml", "/data/RecordNameEntityData.xml",
            "/data/BiographyEntityData.xml");

    public static final String ORCID_INTERNAL_FULL_XML = "/orcid-internal-full-message-latest.xml";

    private Unmarshaller unmarshaller;

    @Mock
    private GenericDao<SecurityQuestionEntity, Integer> securityQuestionDao;

    @Mock
    private GenericDao<ProfileEventEntity, Long> profileEventDao;

    @Mock
    private SourceManager sourceManager;

    @Mock
    private NotificationDao mockNotificationDao;

    @Mock
    private MailGunManager mockMailGunManager;
    
    @Mock
    private OrcidOauth2TokenDetailService mockOrcidOauth2TokenDetailService;

    @Mock
    private ProfileEntityCacheManager mockProfileEntityCacheManager;
    
    @Mock
    private EmailManager mockEmailManager;
    
    @Mock
    private ProfileDao mockProfileDao;
    
    @Mock
    private ProfileDao mockProfileDaoReadOnly;
    
    @Mock
    private JpaJaxbNotificationAdapter mockNotificationAdapter;
    
    @Mock
    public GenericDao<EmailEventEntity, Long> mockEmailEventDao;
    
    @Resource(name = "profileDao")
    private ProfileDao profileDao;

    @Resource(name = "profileDaoReadOnly")
    private ProfileDao profileDaoReadOnly;

    @Resource
    private EncryptionManager encryptionManager;

    @Resource(name = "notificationManagerV3")
    private NotificationManager notificationManager;

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private MailGunManager mailGunManager;

    @Resource
    private ClientDetailsDao clientDetailsDao;
    
    @Resource
    private ProfileEntityCacheManager profileEntityCacheManager;
    
    @Resource(name = "emailManagerV3")
    private EmailManager emailManager;
    
    @Resource
    public GenericDao<EmailEventEntity, Long> emailEventDao;

    @Resource(name = "jpaJaxbNotificationAdapterV3")
    private JpaJaxbNotificationAdapter notificationAdapter;
    
    @Resource(name = "profileEntityManagerV3")
    private ProfileEntityManager profileEntityManager;
    
    @BeforeClass
    public static void initDBUnitData() throws Exception {
        initDBUnitData(DATA_FILES);
    }

    @AfterClass
    public static void removeDBUnitData() throws Exception {
        Collections.reverse(DATA_FILES);
        removeDBUnitData(DATA_FILES);
    }

    @Before
    public void initJaxb() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(OrcidMessage.class);
        unmarshaller = context.createUnmarshaller();
    }

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);        
        TargetProxyHelper.injectIntoProxy(notificationManager, "encryptionManager", encryptionManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileEventDao", profileEventDao);
        TargetProxyHelper.injectIntoProxy(notificationManager, "sourceManager", sourceManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "orcidOauth2TokenDetailService", mockOrcidOauth2TokenDetailService);
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileDaoReadOnly", mockProfileDaoReadOnly);
        TargetProxyHelper.injectIntoProxy(notificationManager, "emailEventDao", mockEmailEventDao);
        when(mockOrcidOauth2TokenDetailService.doesClientKnowUser(Matchers.anyString(), Matchers.anyString())).thenReturn(true);        
    }
    
    @After
    public void resetMocks() {
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileEntityCacheManager", profileEntityCacheManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "emailManager", emailManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileDao", profileDao);        
        TargetProxyHelper.injectIntoProxy(notificationManager, "notificationDao", notificationDao);        
        TargetProxyHelper.injectIntoProxy(notificationManager, "notificationAdapter", notificationAdapter);
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileDaoReadOnly", profileDaoReadOnly);
        TargetProxyHelper.injectIntoProxy(notificationManager, "emailEventDao", emailEventDao);
    }

    protected <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
        return TargetProxyHelper.getTargetObject(proxy, targetClass);
    }

    @Test
    public void testSendWelcomeEmail() throws JAXBException, IOException, URISyntaxException {
        OrcidMessage orcidMessage = (OrcidMessage) unmarshaller.unmarshal(getClass().getResourceAsStream(ORCID_INTERNAL_FULL_XML));
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        notificationManager.sendWelcomeEmail(orcidProfile.getOrcidIdentifier().getPath(),
                orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue());
    }

    @Test
    public void testSendVerificationEmail() throws JAXBException, IOException, URISyntaxException {
        OrcidMessage orcidMessage = (OrcidMessage) unmarshaller.unmarshal(getClass().getResourceAsStream(ORCID_INTERNAL_FULL_XML));
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        notificationManager.sendVerificationEmail(orcidProfile.getOrcidIdentifier().getPath(), orcidProfile.getOrcidBio().getContactDetails().retrievePrimaryEmail().getValue());
    }

    @Test
    public void testResetEmail() throws Exception {
        String userOrcid = "0000-0000-0000-0003";
        String primaryEmail = "public_0000-0000-0000-0003@test.orcid.org";
        for (Locale locale : Locale.values()) {
            profileEntityManager.updateLocale(userOrcid, locale);
            EncryptionManager mockEncypter = mock(EncryptionManager.class);
            getTargetObject(notificationManager, NotificationManagerImpl.class).setEncryptionManager(mockEncypter);
            when(mockEncypter.encryptForExternalUse(any(String.class)))
                    .thenReturn("Ey+qsh7G2BFGEuqqkzlYRidL4NokGkIgDE+1KOv6aLTmIyrppdVA6WXFIaQ3KsQpKEb9FGUFRqiWorOfhbB2ww==");
            notificationManager.sendPasswordResetEmail(primaryEmail, userOrcid);
        }
    }

    @Test
    public void testAmendEmail() throws JAXBException, IOException, URISyntaxException {
        SourceEntity sourceEntity = new SourceEntity(new ClientDetailsEntity("APP-5555555555555555"));
        when(sourceManager.retrieveSourceEntity()).thenReturn(sourceEntity);
        when(sourceManager.retrieveSourceOrcid()).thenReturn("APP-5555555555555555");
        String testOrcid = "0000-0000-0000-0003";

        for (Locale locale : Locale.values()) {
            NotificationEntity previousNotification = notificationDao.findLatestByOrcid(testOrcid);
            long minNotificationId = previousNotification != null ? previousNotification.getId() : -1;
            profileEntityManager.updateLocale(testOrcid, locale);
            notificationManager.sendAmendEmail(testOrcid, AmendedSection.UNKNOWN, Collections.emptyList());
            // New notification entity should have been created
            NotificationEntity latestNotification = notificationDao.findLatestByOrcid(testOrcid);
            assertNotNull(latestNotification);
            assertTrue(latestNotification.getId() > minNotificationId);
            assertEquals(org.orcid.jaxb.model.notification_v2.NotificationType.AMENDED.name(), latestNotification.getNotificationType());
        }
    }

    @Test
    public void testAddedDelegatesSentCorrectEmail() throws JAXBException, IOException, URISyntaxException {
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileEntityCacheManager", mockProfileEntityCacheManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "emailManager", mockEmailManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileDao", mockProfileDao);        
        TargetProxyHelper.injectIntoProxy(notificationManager, "notificationDao", mockNotificationDao);        
        TargetProxyHelper.injectIntoProxy(notificationManager, "notificationAdapter", mockNotificationAdapter);                       
        
        final String orcid = "0000-0000-0000-0003";
        String delegateOrcid = "1234-5678-1234-5678";
        
        ProfileEntity profile = new ProfileEntity();
        RecordNameEntity recordName = new RecordNameEntity();
        recordName.setCreditName("My credit name");
        recordName.setVisibility(org.orcid.jaxb.model.common_v2.Visibility.PUBLIC.name());
        profile.setRecordNameEntity(recordName);
        profile.setSendAdministrativeChangeNotifications(true);
        profile.setSendChangeNotifications(true);
        profile.setSendMemberUpdateRequests(true);
        profile.setSendOrcidNews(true);
        EmailEntity emailEntity = new EmailEntity();
        emailEntity.setId("test@email.com");
        emailEntity.setPrimary(true);
        emailEntity.setCurrent(true);
        Set<EmailEntity> emails = new HashSet<EmailEntity>();
        emails.add(emailEntity);
        profile.setEmails(emails);
        
        SourceEntity sourceEntity = new SourceEntity(new ClientDetailsEntity("APP-5555555555555555"));
        when(sourceManager.retrieveSourceEntity()).thenReturn(sourceEntity);
        when(sourceManager.retrieveSourceOrcid()).thenReturn("APP-5555555555555555");
        when(mockNotificationAdapter.toNotificationEntity(Mockito.any(Notification.class))).thenReturn(new NotificationCustomEntity());
        
        Email email = new Email();
        email.setEmail("test@email.com");
        
        Email delegateEmail = new Email();
        delegateEmail.setEmail("delegate@email.com");
        
        when(mockProfileEntityCacheManager.retrieve(Mockito.anyString())).thenAnswer(new Answer<ProfileEntity>(){
            @Override
            public ProfileEntity answer(InvocationOnMock invocation) throws Throwable {
                profile.setId(invocation.getArgument(0));
                return profile;
            }
            
        });
        
        when(mockProfileDao.find(Mockito.anyString())).thenAnswer(new Answer<ProfileEntity>(){
            @Override
            public ProfileEntity answer(InvocationOnMock invocation) throws Throwable {
                profile.setId(invocation.getArgument(0));
                return profile;
            }
            
        });
        
        when(mockEmailManager.findPrimaryEmail(orcid)).thenReturn(email);        
        when(mockEmailManager.findPrimaryEmail(delegateOrcid)).thenReturn(delegateEmail);
        
        for(org.orcid.jaxb.model.common_v2.Locale locale : org.orcid.jaxb.model.common_v2.Locale.values()) {
            profile.setLocale(locale.name());
            notificationManager.sendNotificationToAddedDelegate("0000-0000-0000-0003", delegateOrcid);
        }
    }

    @Test
    public void testSendDeactivateEmail() throws JAXBException, IOException, URISyntaxException {
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileEntityCacheManager", mockProfileEntityCacheManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "emailManager", mockEmailManager);
        final String orcid = "0000-0000-0000-0003";
        
        ProfileEntity profile = new ProfileEntity(orcid);
        RecordNameEntity recordName = new RecordNameEntity();
        recordName.setCreditName("My credit name");
        recordName.setVisibility(org.orcid.jaxb.model.common_v2.Visibility.PUBLIC.name());
        profile.setRecordNameEntity(recordName);
        
        Email email = new Email();
        email.setEmail("test@email.com");
        
        when(mockProfileEntityCacheManager.retrieve(orcid)).thenReturn(profile);
        when(mockEmailManager.findPrimaryEmail(orcid)).thenReturn(email);
        
        for (org.orcid.jaxb.model.common_v2.Locale locale : org.orcid.jaxb.model.common_v2.Locale.values()) {
            profile.setLocale(locale.name());
            notificationManager.sendOrcidDeactivateEmail(orcid);
        }        
    }

    @Test
    public void testApiCreatedRecordEmail() throws JAXBException, IOException, URISyntaxException {
        String userOrcid = "0000-0000-0000-0003";
        String primaryEmail = "public_0000-0000-0000-0003@test.orcid.org";
        for (Locale locale : Locale.values()) {
            profileEntityManager.updateLocale(userOrcid, locale);
            notificationManager.sendApiRecordCreationEmail(primaryEmail, userOrcid);
        }
    }

    @Test
    public void testSendVerificationReminderEmail() throws JAXBException, IOException, URISyntaxException {
        String userOrcid = "0000-0000-0000-0003";
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileEntityCacheManager", mockProfileEntityCacheManager);
        ProfileEntity profile = new ProfileEntity(userOrcid);
        RecordNameEntity recordName = new RecordNameEntity();
        recordName.setCreditName("My credit name");
        recordName.setVisibility(org.orcid.jaxb.model.common_v2.Visibility.PUBLIC.name());
        profile.setRecordNameEntity(recordName);
        when(mockProfileEntityCacheManager.retrieve(userOrcid)).thenReturn(profile);
        String primaryEmail = "limited_0000-0000-0000-0003@test.orcid.org";
        for (Locale locale : Locale.values()) {
            profile.setLocale(locale.name());
            notificationManager.sendVerificationReminderEmail(userOrcid, primaryEmail);
        }
    }

    @Test
    public void testClaimReminderEmail() throws JAXBException, IOException, URISyntaxException {
        String userOrcid = "0000-0000-0000-0003";
        String primaryEmail = "public_0000-0000-0000-0003@test.orcid.org";
        for (Locale locale : Locale.values()) {
            profileEntityManager.updateLocale(userOrcid, locale);
            notificationManager.sendClaimReminderEmail(userOrcid, 2);
        }
    }

    @Test
    public void testChangeEmailAddress() throws Exception {
        TargetProxyHelper.injectIntoProxy(notificationManager, "profileEntityCacheManager", mockProfileEntityCacheManager);
        TargetProxyHelper.injectIntoProxy(notificationManager, "emailManager", mockEmailManager);
        final String orcid = "0000-0000-0000-0003";
        
        ProfileEntity profile = new ProfileEntity(orcid);
        RecordNameEntity recordName = new RecordNameEntity();
        recordName.setCreditName("My credit name");
        recordName.setVisibility(org.orcid.jaxb.model.common_v2.Visibility.PUBLIC.name());
        profile.setRecordNameEntity(recordName);
        
        Email email = new Email();
        email.setEmail("test@email.com");
        
        when(mockProfileEntityCacheManager.retrieve(orcid)).thenReturn(profile);
        when(mockEmailManager.findPrimaryEmail(orcid)).thenReturn(email);
        
        for (org.orcid.jaxb.model.common_v2.Locale locale : org.orcid.jaxb.model.common_v2.Locale.values()) {            
            profile.setLocale(locale.name());
            notificationManager.sendEmailAddressChangedNotification(orcid, "new@email.com", "original@email.com");
        }
    }

    @Test
    public void testSendReactivationEmail() throws Exception {
        String userOrcid = "0000-0000-0000-0003";
        String primaryEmail = "public_0000-0000-0000-0003@test.orcid.org";
        String email = "original@email.com";
        for (Locale locale : Locale.values()) {
            profileEntityManager.updateLocale(userOrcid, locale);
            notificationManager.sendReactivationEmail(email, userOrcid);
        }
    }

    @Test
    public void testAdminDelegateRequest() throws JAXBException, IOException, URISyntaxException {
        SourceEntity sourceEntity = new SourceEntity(new ClientDetailsEntity("APP-5555555555555555"));
        when(sourceManager.retrieveSourceEntity()).thenReturn(sourceEntity);
        when(sourceManager.retrieveSourceOrcid()).thenReturn("APP-5555555555555555");

        for (Locale locale : Locale.values()) {            
            notificationManager.sendDelegationRequestEmail("0000-0000-0000-0003", "0000-0000-0000-0003", "http://test.orcid.org");
        }
    }

    @Test
    public void testCreateCustomNotification() {
        SourceEntity sourceEntity = new SourceEntity(new ClientDetailsEntity("APP-5555555555555555"));
        when(sourceManager.retrieveSourceEntity()).thenReturn(sourceEntity);
        when(sourceManager.retrieveSourceOrcid()).thenReturn("APP-5555555555555555");
        String testOrcid = "0000-0000-0000-0003";
        NotificationCustom notification = new NotificationCustom();
        notification.setSubject("Test subject");
        notification.setLang("en-gb");
        Notification result = notificationManager.createNotification(testOrcid, notification);
        assertNotNull(result);
        assertTrue(result instanceof NotificationCustom);
        NotificationCustom customResult = (NotificationCustom) result;
        assertEquals("Test subject", customResult.getSubject());
        assertEquals("en-gb", customResult.getLang());
    }

    @Test
    public void deriveEmailFriendlyNameTest() {
        ProfileEntity testProfile = new ProfileEntity("0000-0000-0000-0003");
        assertEquals("ORCID Registry User", notificationManager.deriveEmailFriendlyName(testProfile));
        testProfile.setRecordNameEntity(new RecordNameEntity());
        assertEquals("ORCID Registry User", notificationManager.deriveEmailFriendlyName(testProfile));
        testProfile.getRecordNameEntity().setGivenNames("Given Name");
        assertEquals("Given Name", notificationManager.deriveEmailFriendlyName(testProfile));
        testProfile.getRecordNameEntity().setFamilyName("Family Name");
        assertEquals("Given Name Family Name", notificationManager.deriveEmailFriendlyName(testProfile));
        testProfile.getRecordNameEntity().setCreditName("Credit Name");
        assertEquals("Credit Name", notificationManager.deriveEmailFriendlyName(testProfile));
    }

    /**
     * 0000-0000-0000-0003 Must have notifications enabled
     */
    @Test
    public void sendAcknowledgeMessageToAccountWithNotificationsEnabledTest() throws Exception {
        String clientId = "APP-5555555555555555";
        String orcid = "0000-0000-0000-0003";
        // Mock the notification DAO
        NotificationManagerImpl notificationManagerImpl = getTargetObject(notificationManager, NotificationManagerImpl.class);
        notificationManagerImpl.setNotificationDao(mockNotificationDao);
        notificationManagerImpl.setMailGunManager(mockMailGunManager);
        notificationManagerImpl.sendAcknowledgeMessage(orcid, clientId);
        verify(mockNotificationDao, times(1)).persist(Matchers.any(NotificationEntity.class));
        verify(mockMailGunManager, never()).sendEmail(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());

        // Rollback mocked
        notificationManagerImpl.setNotificationDao(notificationDao);
        notificationManagerImpl.setMailGunManager(mailGunManager);
    }

    /**
     * 0000-0000-0000-0002 Must have notifications disabled
     */
    @Test
    public void sendAcknowledgeMessageToAccountWithNotificationsDisabledTest() throws Exception {
        String clientId = "APP-5555555555555555";
        String orcid = "0000-0000-0000-0002";
        // Mock the notification DAO
        NotificationManagerImpl notificationManagerImpl = getTargetObject(notificationManager, NotificationManagerImpl.class);
        notificationManagerImpl.setNotificationDao(mockNotificationDao);
        notificationManagerImpl.setMailGunManager(mockMailGunManager);
        notificationManagerImpl.sendAcknowledgeMessage(orcid, clientId);
        verify(mockNotificationDao, never()).persist(Matchers.any(NotificationEntity.class));
        verify(mockMailGunManager, times(1)).sendEmail(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());

        // Rollback mocked
        notificationManagerImpl.setNotificationDao(notificationDao);
        notificationManagerImpl.setMailGunManager(mailGunManager);
    }

    /**
     * Test independent of spring context, sets up NotificationManager with
     * mocked notifiation dao and notification adapter
     */
    @Test
    public void testFindPermissionsByOrcidAndClient() {
        List<Notification> notificationPermissions = IntStream.range(0, 10).mapToObj(i -> new NotificationPermission()).collect(Collectors.toList());

        NotificationDao notificationDao = mock(NotificationDaoImpl.class);
        JpaJaxbNotificationAdapter adapter = mock(JpaJaxbNotificationAdapterImpl.class);
        when(notificationDao.findPermissionsByOrcidAndClient(anyString(), anyString(), anyInt(), anyInt())).thenReturn(new ArrayList<NotificationEntity>());
        when(adapter.toNotification(Matchers.<ArrayList<NotificationEntity>> any())).thenReturn(notificationPermissions);

        NotificationManager notificationManager = new NotificationManagerImpl();
        ReflectionTestUtils.setField(notificationManager, "notificationAdapter", adapter);
        ReflectionTestUtils.setField(notificationManager, "notificationDao", notificationDao);

        NotificationPermissions notifications = notificationManager.findPermissionsByOrcidAndClient("some-orcid", "some-client", 0,
                OrcidApiConstants.MAX_NOTIFICATIONS_AVAILABLE);

        assertEquals(notificationPermissions.size(), notifications.getNotifications().size());
    }    
    
    @Test
    public void filterActionedNotificationAlertsTest() {
        TargetProxyHelper.injectIntoProxy(notificationManager, "notificationDao", mockNotificationDao);
        when(mockNotificationDao.findByOricdAndId(Matchers.anyString(), Matchers.anyLong())).thenReturn(null);
        List<Notification> notifications = IntStream.range(0, 10).mapToObj(new IntFunction<Notification> () {
            @Override
            public Notification apply(int value) {
                if(value % 3 == 0) {
                    NotificationInstitutionalConnection n = new NotificationInstitutionalConnection();
                    n.setSource(new Source("0000-0000-0000-0000"));
                    n.setPutCode(Long.valueOf(value));
                    return n;
                } else {
                    NotificationPermission n = new NotificationPermission();
                    n.setPutCode(Long.valueOf(value));
                    return n;
                }
            }            
        }).collect(Collectors.toList());                
        
        assertEquals(10, notifications.size());
        notifications = notificationManager.filterActionedNotificationAlerts(notifications, "some-orcid");
        assertEquals(6, notifications.size());
        for(Notification n : notifications) {
            assertEquals(NotificationType.PERMISSION, n.getNotificationType());
            assertNotNull(n.getPutCode());
            assertThat(n.getPutCode(), not(anyOf(is(Long.valueOf(0)), is(Long.valueOf(3)), is(Long.valueOf(6)), is(Long.valueOf(9)))));
        }                
    }
    
    @Test
    public void processUnverifiedEmails7DaysTest() {
        List<Pair<String, Date>> emails = new ArrayList<Pair<String, Date>>();
        Pair<String, Date> tooOld1 = Pair.of("tooOld1@test.orcid.org", LocalDateTime.now().minusDays(15).toDate());
        Pair<String, Date> tooOld2 = Pair.of("tooOld2@test.orcid.org", LocalDateTime.now().minusDays(20).toDate());
        Pair<String, Date> ok1 = Pair.of("michael@bentine.com", LocalDateTime.now().minusDays(7).toDate());
        Pair<String, Date> ok2 = Pair.of("spike@milligan.com", LocalDateTime.now().minusDays(14).toDate());
        emails.add(ok1);
        emails.add(ok2);
        emails.add(tooOld1);
        emails.add(tooOld2);
        when(mockProfileDaoReadOnly.findEmailsUnverfiedDays(Matchers.anyInt(), Matchers.anyInt(), Matchers.any())).thenReturn(emails)
                .thenReturn(new ArrayList<Pair<String, Date>>());
        
        notificationManager.processUnverifiedEmails7Days();
        
        verify(mockEmailEventDao, times(1)).persist(new EmailEventEntity("michael@bentine.com", EmailEventType.VERIFY_EMAIL_7_DAYS_SENT));
        verify(mockEmailEventDao, times(1)).persist(new EmailEventEntity("spike@milligan.com", EmailEventType.VERIFY_EMAIL_7_DAYS_SENT));
        verify(mockEmailEventDao, times(1)).persist(new EmailEventEntity("tooOld1@test.orcid.org", EmailEventType.VERIFY_EMAIL_TOO_OLD));
        verify(mockEmailEventDao, times(1)).persist(new EmailEventEntity("tooOld2@test.orcid.org", EmailEventType.VERIFY_EMAIL_TOO_OLD));
    }     
    
}
