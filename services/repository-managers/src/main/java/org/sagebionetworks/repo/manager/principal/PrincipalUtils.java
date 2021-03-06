package org.sagebionetworks.repo.manager.principal;

import java.util.Date;

import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.repo.util.SignedTokenUtil;
import org.sagebionetworks.util.ValidateArgument;

public class PrincipalUtils {
	public static final long EMAIL_VALIDATION_TIME_LIMIT_MILLIS = 24*3600*1000L; // 24 hours as milliseconds

	protected static AccountCreationToken createAccountCreationToken(NewUser user, Date now) {
		AccountCreationToken accountCreationToken = new AccountCreationToken();
		accountCreationToken.setEncodedMembershipInvtnSignedToken(user.getEncodedMembershipInvtnSignedToken());
		EmailValidationSignedToken emailValidationSignedToken = new EmailValidationSignedToken();
		emailValidationSignedToken.setEmail(user.getEmail());
		emailValidationSignedToken.setCreatedOn(now);
		SignedTokenUtil.signToken(emailValidationSignedToken);
		accountCreationToken.setEmailValidationSignedToken(emailValidationSignedToken);
		return accountCreationToken;
	}

	protected static EmailValidationSignedToken createEmailValidationSignedToken(Long userId, String email, Date now) {
		EmailValidationSignedToken emailValidationSignedToken = new EmailValidationSignedToken();
		emailValidationSignedToken.setUserId(userId + "");
		emailValidationSignedToken.setEmail(email);
		emailValidationSignedToken.setCreatedOn(now);
		SignedTokenUtil.signToken(emailValidationSignedToken);
		return emailValidationSignedToken;
	}

	protected static String validateEmailValidationSignedToken(EmailValidationSignedToken token, Date now) {
		if (token.getUserId() != null)
			throw new IllegalArgumentException("EmailValidationSignedToken.token.getUserId() must be null");
		String email = token.getEmail();
		ValidateArgument.required(email, "EmailValidationSignedToken.email");
		Date createdOn = token.getCreatedOn();
		ValidateArgument.required(createdOn, "EmailValidationSignedToken.createdOn");
		if (now.getTime() - createdOn.getTime() > EMAIL_VALIDATION_TIME_LIMIT_MILLIS)
			throw new IllegalArgumentException("Email validation link is out of date.");
		SignedTokenUtil.validateToken(token);
		return email;
	}

	protected static String validateAdditionalEmailSignedToken(EmailValidationSignedToken token, String userId, Date now) {
	    ValidateArgument.required(token.getUserId(), "EmailValidationSignedToken.userId");
		if (!token.getUserId().equals(userId))
			throw new IllegalArgumentException("Invalid token for userId " + userId);
		String email = token.getEmail();
		ValidateArgument.required(email, "EmailValidationSignedToken.email");
		Date createdOn = token.getCreatedOn();
		ValidateArgument.required(createdOn, "EmailValidationSignedToken.createdOn");
		if (now.getTime() - createdOn.getTime() > EMAIL_VALIDATION_TIME_LIMIT_MILLIS)
			throw new IllegalArgumentException("Email validation link is out of date.");
		SignedTokenUtil.validateToken(token);
		return email;
	}
}
