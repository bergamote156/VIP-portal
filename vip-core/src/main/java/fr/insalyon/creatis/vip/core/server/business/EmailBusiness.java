package fr.insalyon.creatis.vip.core.server.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insalyon.creatis.sma.client.SMAClient;
import fr.insalyon.creatis.sma.client.SMAClientException;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.core.server.dao.UserDAO;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

@Service
public class EmailBusiness {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Server server;
    private final SMAClient smaClient;
    private final UserDAO userDAO;
    private final EmailTemplateUtils emailTemplateUtils;

    @Autowired
    public EmailBusiness(Server server, SMAClient smaClient, UserDAO userDAO, EmailTemplateUtils emailTemplateUtils) {
        this.server = server;
        this.smaClient = smaClient;
        this.userDAO = userDAO;
        this.emailTemplateUtils = emailTemplateUtils;
    }

    public void sendEmail(String subject, String content, String[] recipients,
            boolean direct, String username) throws VipException {
        if (server.useSMA()) {
            sendWithSMA(subject, content, recipients, direct, username);
        } else {
            logger.info("SMA disabled, not sending email and logging it");
            logger.info("subject : {}", subject);
            logger.info("recipients : {}", (Object[]) recipients);
            logger.info("content : {}", content);
        }
    }

    private void sendWithSMA(String subject, String content, String[] recipients, boolean direct, String username)
            throws VipException {
        try {
            // with the SMA client-server protocol, the content MUST NOT contain any new line as they are considered
            // as parameters separators
            content = content.replaceAll("[\\n\\r]", "");
            smaClient.sendEmail(subject, content, recipients, direct, username);
        } catch (SMAClientException ex) {
            logger.error("Error sending {} email to {}", subject, Arrays.toString(recipients), ex);
            throw new VipException(ex);
        }
    }

    public void sendEmailToAdmins(String subject, String content, boolean direct, String userEmail)
            throws VipException {
        try {
            for (String adminEmail : getAdministratorsEmails()) {
                sendEmail(subject, content, new String[] { adminEmail }, direct, userEmail);
            }
        } catch (DAOException e) {
            logger.error("Error sending {} to admins !", subject, e);
            throw new VipException(e);
        }
    }

    /**
     * Gets an array of administrator's e-mails
     */
    public String[] getAdministratorsEmails() throws DAOException {
        List<String> emails = new ArrayList<>();
        for (User admin : userDAO.getAdministrators()) {
            emails.add(admin.getEmail());
        }
        return emails.toArray(new String[0]);
    }

    public void verifyEmail(String email) throws VipException {
        // verify email format
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            logger.error("The email {} is invalid", email);
            throw new VipException("The email " + email + " is invalid");
        }

        // Build log message
        StringBuilder message = new StringBuilder("verifying ");
        message.append(email);
        message.append(". List of undesired mail domains: ");
        for (String s : server.getUndesiredMailDomains()) {
            if (!s.trim().isEmpty()) {
                message.append(" ");
                message.append(s);
            }
        }
        message.append(". ");
        logger.info(message.toString());

        // Check if email domain is undesired
        for (String udm : server.getUndesiredMailDomains()) {
            if (udm.trim().isEmpty()) {
                // An empty config file entry gets here as an empty or
                // whitespace-only string, skip it
                continue;
            }
            String[] useremail = email.split("@");
            if (useremail.length != 2) {
                logger.info("User Mail address is incorrect : " + email);
                throw new VipException("Error");
            }
            // Only check against the domain part of the user's email address
            if (useremail[1].endsWith(udm)) {
                logger.error("Undesired Mail Domain for " + email);
                throw new VipException("Error");
            }
        }
    }

    public void sendErrorEmailToAdmins(String errorMessage, Exception exception, String userEmail) {
        try {
            StringBuilder emailContent = new StringBuilder("<html><head></head><body>");
            emailContent.append("<p>Dear Administrator,</p>");

            emailContent.append("<p>An error has been encountered in VIP with the following user:");
            emailContent.append("<b>" + userEmail + "</b></p>");

            emailContent.append("<p><b>" + errorMessage + "</b></p>");

            if (exception != null) {
                emailContent.append("The exception was:");
                emailContent.append(exception);
            }

            emailContent.append("<p>Please check the logs for more information</p>");
            emailContent.append("<p>&nbsp;</p>");
            emailContent.append("<p>Best Regards,</p><p>VIP Team</p>");
            emailContent.append("</body></html>");

            sendEmailToAdmins("[VIP Admin] VIP error", emailContent.toString(),
                    true, userEmail);
        } catch (VipException e) {
            logger.error("Cannot sent mail to admin. Ignoring", e);
        }
    }

    public void sendContactMail(User user, String category, String subject, String comment) throws VipException {
        String emailContent = emailTemplateUtils.sendContactMail(user, category, subject, comment);

        sendEmailToAdmins("[VIP Contact] " + category, emailContent,
                true, user.getEmail());
    }

    public void sendActivationCode(String email) throws VipException {
        try {
            User user = userDAO.get(email);

            if (userDAO.isLocked(email)) {
                logger.error("Cannot send activation code to {} : account locked", email);
                throw new VipException("User is locked.");
            }

            String emailContent = emailTemplateUtils.sendActivationCode(user);

            sendEmail("VIP activation code (reminder)", emailContent,
                    new String[] { user.getEmail() }, true, user.getEmail());

        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void sendResetCode(String email) throws VipException {
        try {
            User user = userDAO.get(email);

            if (userDAO.isLocked(email)) {
                logger.error("Cannot send reset code to {} : account locked", email);
                throw new VipException("User is locked.");
            }

            String code = UUID.randomUUID().toString();
            userDAO.updateCode(email, code);

            String emailContent = emailTemplateUtils.sendResetCode(user, code);

            sendEmail("Code to reset your VIP password", emailContent,
                    new String[] { user.getEmail() }, true, user.getEmail());

        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void requestNewEmail(User user, String newEmail)
            throws VipException {

        try {
            String code = UUID.randomUUID().toString();
            userDAO.updateCode(user.getEmail(), code);
            userDAO.updateNextEmail(user.getEmail(), newEmail);

            String emailContent = emailTemplateUtils.requestNewEmail(user, code);

            sendEmail("Code to confirm your VIP email address", emailContent,
                    new String[] { newEmail }, true, newEmail);

        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }
}
