package fr.insalyon.creatis.vip.social.server.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insalyon.creatis.vip.core.client.DefaultError;
import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.User;
import fr.insalyon.creatis.vip.core.server.business.EmailBusiness;
import fr.insalyon.creatis.vip.core.server.business.EmailTemplateUtils;
import fr.insalyon.creatis.vip.core.server.business.UserBusiness;
import fr.insalyon.creatis.vip.core.server.business.base.CommonBusiness;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.social.client.SocialConstants;
import fr.insalyon.creatis.vip.social.models.GroupMessage;
import fr.insalyon.creatis.vip.social.models.Message;
import fr.insalyon.creatis.vip.social.server.dao.GroupMessageDAO;
import fr.insalyon.creatis.vip.social.server.dao.MessageDAO;

@Service
@Transactional
public class MessageBusiness extends CommonBusiness {

    private final MessageDAO messageDAO;
    private final GroupMessageDAO groupMessageDAO;
    private final EmailBusiness emailBusiness;
    private final UserBusiness userBusiness;
    private final EmailTemplateUtils emailTemplateUtils;

    @Autowired
    public MessageBusiness(
            MessageDAO messageDAO, GroupMessageDAO groupMessageDAO,
            EmailBusiness emailBusiness, UserBusiness userBusiness, EmailTemplateUtils emailTemplateUtils) {
        this.messageDAO = messageDAO;
        this.groupMessageDAO = groupMessageDAO;
        this.emailBusiness = emailBusiness;
        this.userBusiness = userBusiness;
        this.emailTemplateUtils = emailTemplateUtils;
    }

    public List<Message> getMessagesByUser(Date startDate)
            throws VipException {

        try {
            User currentUser = userBusiness.getCurrentUser();
            return messageDAO.getMessagesByUser(
                    currentUser.getEmail(), SocialConstants.MESSAGE_MAX_DISPLAY, startDate);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public List<Message> getSentMessagesByUser(Date startDate)
            throws VipException {

        try {
            User currentUser = userBusiness.getCurrentUser();
            return messageDAO.getSentMessagesByUser(
                    currentUser.getEmail(), SocialConstants.MESSAGE_MAX_DISPLAY, startDate);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public List<GroupMessage> getGroupMessages(String groupName, Date startDate)
            throws VipException {

        try {
            return groupMessageDAO.getMessageByGroup(
                    groupName, SocialConstants.MESSAGE_MAX_DISPLAY, startDate);

        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void markAsRead(long id) throws VipException {
        try {
            User currentUser = userBusiness.getCurrentUser();
            messageDAO.markAsRead(id, currentUser.getEmail());
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void remove(long id) throws VipException {
        assertCurrentUserCanDeleteSentMessage(id);

        try {
            messageDAO.remove(id);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void removeByReceiver(long id) throws VipException {
        try {
            User currentUser = userBusiness.getCurrentUser();
            messageDAO.removeByReceiver(id, currentUser.getEmail());
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void removeGroupMessage(long id) throws VipException {
        assertCurrentUserCanDeleteGroupMessage(id);

        try {
           groupMessageDAO.remove(id);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void sendMessage(
            String[] recipients, String subject, String message)
            throws VipException {

        User user = userBusiness.getCurrentUser();
        assertCurrentUserCanSendMessage(user);

        try {
            // Handle "All" special case
            if (recipients.length > 0 && recipients[0].equals("All")) {
                List<String> users = new ArrayList<>();
                for (User u : userBusiness.getUsers()) {
                    // Dont send mail to locked users
                    if (!u.isAccountLocked()) {
                        users.add(u.getEmail());
                    }
                }
                recipients = users.toArray(new String[]{});
            } else {
                // If callers (GWT or others) still send emails, detect that
                // by checking for an '@' in the first element and treat the
                // array as emails. Otherwise treat elements as user IDs and
                // resolve them to emails.
                List<String> emails = new ArrayList<>();
                if (recipients.length > 0 && recipients[0].contains("@")) {
                    // recipients are already emails
                    for (String email : recipients) {
                        User recipient = userBusiness.getUserData(email);
                        if (recipient != null && !recipient.isAccountLocked()) {
                            emails.add(email);
                        }
                    }
                } else {
                    // Convert recipient IDs to emails
                    for (String recipientId : recipients) {
                        User recipient = userBusiness.get(recipientId);
                        if (recipient != null && !recipient.isAccountLocked()) {
                            emails.add(recipient.getEmail());
                        }
                    }
                }
                recipients = emails.toArray(new String[]{});
            }

            String emailContent = emailTemplateUtils.sendMessage(user, subject, message);

            for (String email : recipients) {
                emailBusiness.sendEmail("VIP Message: " + subject + " (" + user.getFullName() + ")",
                        emailContent, new String[]{email}, true, user.getEmail());
            }

            long messageId = messageDAO.add(user.getEmail(), subject, message);

            for (String recipient : recipients) {
                messageDAO.associateMessageToUser(recipient, messageId);
            }
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    public void copyMessageToVipSupport(
            String[] recipients, String subject, String message)
            throws VipException {

        User sender = userBusiness.getCurrentUser();

        String emailContent = emailTemplateUtils.vipSupportCopy(sender, Arrays.asList(recipients), subject, message);

        // if there is only one receiver, name it in subject, otherwise name the sender
        String subjectInfo = recipients.length == 1 ?
                "to " + recipients[0] : "from " + sender.getFullName();

        emailBusiness.sendEmailToAdmins(
            "[VIP Support Copy] " + subject + "(" + subjectInfo + ")",
            emailContent, true, sender.getEmail());
    }

    public void sendMessageToVipSupport(
            String subject, String message, List<String> workflowIDs,
            List<String> simulationNames) throws VipException {

        User user = userBusiness.getCurrentUser();

        String emailContent = emailTemplateUtils.sendMessageToVipSupport(user, subject, message, workflowIDs, simulationNames);

        emailBusiness.sendEmailToAdmins(
            "[VIP Contact] " + subject + " (" + user.getFullName() + ")",
            emailContent, true, user.getEmail());
    }

    public void sendGroupMessage(
            String groupName, String subject,
            String message) throws VipException {

        User user = userBusiness.getCurrentUser();
        assertCurrentUserCanSendGroupMessage(user, groupName);

        try {
            groupMessageDAO.add(user.getEmail(), groupName, subject, message);

            String emailContent = emailTemplateUtils.sendGroupMessage(user, groupName, subject, message);

            List<User> users = userBusiness.getUsersFromGroup(groupName);
            for (User u : users) {
                // Dont send mail to locked users and to itself
                if (!u.isAccountLocked() &&
                        !u.getEmail().equals(user.getEmail())) {
                    emailBusiness.sendEmail("VIP Message: " + subject + " (" + groupName + ")",
                            emailContent, new String[]{u.getEmail()}, true, user.getEmail());
                }
            }
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }


    public int verifyMessages(String email) throws VipException {

        try {
            return messageDAO.verifyMessages(email);
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    private void assertCurrentUserCanSendMessage(User user) throws VipException {
        if (user == null || (!user.isSystemAdministrator() && !user.isDeveloper())) {
            throw new VipException(DefaultError.ACCESS_DENIED);
        }
    }

    private void assertCurrentUserCanSendGroupMessage(User user, String groupName) throws VipException {
        if (user == null || (!user.isSystemAdministrator() && !user.isGroupAdmin(groupName))) {
            throw new VipException(DefaultError.ACCESS_DENIED);
        }
    }

    private void assertCurrentUserCanDeleteSentMessage(long id) throws VipException {
        User currentUser = userBusiness.getCurrentUser();

        if (currentUser == null) {
            throw new VipException(DefaultError.ACCESS_DENIED);
        }
        if (currentUser.isSystemAdministrator()) {
            return;
        }

        try {
            Message message = messageDAO.get(id);
            if (message == null || message.getSender() == null || !currentUser.getEmail().equals(message.getSender().getEmail())) {
                throw new VipException(DefaultError.ACCESS_DENIED);
            }
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }

    private void assertCurrentUserCanDeleteGroupMessage(long id) throws VipException {
        User currentUser = userBusiness.getCurrentUser();

        if (currentUser == null) {
            throw new VipException(DefaultError.ACCESS_DENIED);
        }
        if (currentUser.isSystemAdministrator()) {
            return;
        }

        try {
            GroupMessage groupMessage = groupMessageDAO.get(id);
            if (groupMessage == null) {
                throw new VipException(DefaultError.ACCESS_DENIED);
            }
            if (!currentUser.isGroupAdmin(groupMessage.getGroupName())) {
                throw new VipException(DefaultError.ACCESS_DENIED);
            }
        } catch (DAOException ex) {
            throw new VipException(ex);
        }
    }
}
