package fr.insalyon.creatis.vip.core.server.business;

import java.util.List;

import org.springframework.stereotype.Component;

import fr.insalyon.creatis.vip.core.models.User;

@Component
public class EmailTemplateUtils {

    public String registrationUserEmail(User user) {
        return """
                <html>
                <body>
                <p>Dear %s %s,</p>
                <p>We have successfully received your membership registration
                and your personal profile has been created.</p>
                <p>Please confirm your registration using the following activation code:</p>
                <p><b>%s</b></p>
                <p>Best Regards,<br/>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getCode());
    }

    public String registrationAdminEmail(User user, String groupsNames, String comments) {
        return """
                <html>
                <head></head>
                <body>
                <p>Dear Administrator,</p>
                <p>A new user requested an account:</p>
                <p><b>First Name:</b> %s</p>
                <p><b>Last Name:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>Institution:</b> %s</p>
                <p><b>Country:</b> %s</p>
                <p><b>Groups:</b> %s</p>
                <p><b>Comments:</b><br />%s</p>
                <p>&nbsp;</p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getInstitution(),
                user.getCountryCode().getCountryName(),
                groupsNames,
                comments);
    }

    public String registrationAdminEmailAutomatic(User user, String groupsNames, String comments) {
        return """
                <html>
                <head></head>
                <body>
                <p>Dear Administrators,</p>
                <p>The following account was automatically created:</p>
                <p><b>First Name:</b> %s</p>
                <p><b>Last Name:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>Institution:</b> %s</p>
                <p><b>Country:</b> %s</p>
                <p><b>Groups:</b> %s</p>
                <p><b>Comments:</b><br />%s</p>
                <p>&nbsp;</p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getInstitution(),
                user.getCountryCode().getCountryName(),
                groupsNames,
                comments);
    }

    public String sendMessage(User user, String subject, String message) {
        return """
                <html>
                <head></head>
                <body>
                <p>Hello,</p>
                <p><b>%s</b> sent you a message on VIP:</p>
                <div style="background-color: #F2F2F2">
                <br /><b>Subject:</b> %s<br />
                <em>%s</em><br />
                </div>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                subject,
                message);
    }

    public String vipSupportCopy(User sender, List<String> recipients, String subject, String message) {
        return """
                <html>
                <head></head>
                <body>
                <p><b>%s</b> sent a message to <b>%s</b> on VIP:</p>
                <div style="background-color: #F2F2F2">
                <br /><b>Subject:</b> %s<br />
                <em>%s</em><br />
                </div>
                </body>
                </html>
                """.formatted(
                sender.getFullName(),
                recipients,
                subject,
                message);
    }

    public String sendMessageToVipSupport(User user, String subject, String message, List<String> workflowIDs,
            List<String> simulationNames) {
        return """
                <html>
                <head></head>
                <body>
                <p><b>%s</b> sent you a message on VIP:</p>
                <div style="background-color: #F2F2F2">
                <br /><b>Subject:</b> %s<br />
                <em>%s</em><br />
                </div>
                <p>Workflow ID %s</p>
                <p>Simulation Name %s</p>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                subject,
                message,
                workflowIDs,
                simulationNames);
    }

    public String sendGroupMessage(User user, String groupName, String subject, String message) {
        return """
                <html>
                <head></head>
                <body>
                <p>Hello,</p>
                <p><b>%s</b> sent a message to the group '%s' on VIP:</p>
                <p style="background-color: #F2F2F2">
                <br /><b>Subject:</b> %s<br />
                <em>%s</em><br />
                </p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                groupName,
                subject,
                message);
    }

    public String sendContactMail(User user, String category, String subject, String comment) {
        return """
                <html>
                <head></head>
                <body>
                <p><b>VIP Contact</b></p>
                <p><b>User:</b> %s</p>
                <p><b>Email:</b> <a href="mailto:%s">%s</a></p>
                <p>&nbsp;</p>
                <p><b>Category:</b> %s</p>
                <p><b>Subject:</b> %s</p>
                <p>&nbsp;</p>
                <p><b>Comments:</b></p>
                <p>%s</p>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                user.getEmail(),
                user.getEmail(),
                category,
                subject,
                comment);
    }

    public String sendActivationCode(User user) {
        return """
                <html>
                <head></head>
                <body>
                <p>Dear %s,</p>
                <p>You requested us to send you your personal activation code.</p>
                <p>Please use the following code to activate your account:</p>
                <p><b>%s</b></p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                user.getCode());
    }

    public String sendResetCode(User user, String code) {
        return """
                <html>
                <head></head>
                <body>
                <p>Dear %s,</p>
                <p>You recently requested a new password to sign in to your VIP account.</p>
                <p>Please use the following code to reset your password:</p>
                <p><b>%s</b></p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                code);
    }

    public String requestNewEmail(User user, String code) {
        return """
                <html>
                <head></head>
                <body>
                <p>Dear %s,</p>
                <p>You requested to link your VIP account to this email address.</p>
                <p>Please use the following code to activate it in your VIP account page:</p>
                <p><b>%s</b></p>
                <p>You will have to refresh your VIP web page if you have not done it since you requested the change.</p>
                <p>Please note that your login email is still %s until you validate it.</p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """
                .formatted(
                        user.getFullName(),
                        code,
                        user.getEmail());
    }

    public String removeAccount(User user) {
        return """
                <html>
                <head></head>
                <body>
                <p>Dear Administrators,</p>
                <p>The following user removed her/his account:</p>
                <p><b>First Name:</b> %s</p>
                <p><b>Last Name:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>Institution:</b> %s</p>
                <p>&nbsp;</p>
                <p>Best Regards,</p>
                <p>VIP Team</p>
                </body>
                </html>
                """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getInstitution());
    }
}
