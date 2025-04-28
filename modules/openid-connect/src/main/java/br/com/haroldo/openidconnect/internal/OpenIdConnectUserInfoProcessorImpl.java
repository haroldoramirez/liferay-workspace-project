package br.com.haroldo.openidconnect.internal;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectServiceException;
import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessor;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import java.util.Calendar;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import br.com.haroldo.openidconnect.exceptions.StrangersNotAllowedException;

/**
 * @author Haroldo.Nobrega
 */
@Component(
    immediate = true,
    service = OpenIdConnectUserInfoProcessor.class, // Aqui sim a versão internal
    property = {
    	"service.ranking:Integer=1000"
    }
)
public class OpenIdConnectUserInfoProcessorImpl implements OpenIdConnectUserInfoProcessor {
		
	private Log log = LogFactoryUtil.getLog(OpenIdConnectUserInfoProcessorImpl.class);
	
	@Reference
	private CompanyLocalService companyLocalService;

	@Reference
	private UserLocalService userLocalService;
	
	@Override
	public long processUserInfo(UserInfo userInfo, long companyId) throws PortalException {
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnectUserInfoProcessorImpl Custom Inicializado!");	
		}
				
		String emailAddress = userInfo.getEmailAddress();
		
		//Obter o ScreeName do usuario logado via RHSSO
		String userName = userInfo.getStringClaim("id");
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnectUserInfoProcessorImpl userName: " + userName);	
		}

		User userByScreenName = userLocalService.getUserByScreenName(companyId, userName);

		if (userByScreenName != null) {
			
			//Atualizar o email do usuario
			updateEmailUserLiferay(userInfo, userByScreenName, companyId);
			return userByScreenName.getUserId();
			
		}

		checkAddUser(companyId, emailAddress);

		String firstName = userInfo.getGivenName();
		String lastName = userInfo.getFamilyName();

		if (Validator.isNull(firstName) || Validator.isNull(lastName) || Validator.isNull(emailAddress)) {

			StringBundler sb = new StringBundler(9);

			sb.append("Unable to map OpenId Connect user to the portal, ");
			sb.append("missing or invalid profile information: ");
			sb.append("{emailAddresss=");
			sb.append(emailAddress);
			sb.append(", firstName=");
			sb.append(firstName);
			sb.append(", lastName=");
			sb.append(lastName);
			sb.append("}");

			throw new OpenIdConnectServiceException.UserMappingException(sb.toString());
			
		}

		long creatorUserId = 0;
		boolean autoPassword = true;
		String password1 = null;
		String password2 = null;
		boolean autoScreenName = true;
		String screenName = new String("");
		long facebookId = 0;

		Company company = companyLocalService.getCompany(companyId);

		Locale locale = company.getLocale();

		String middleName = userInfo.getMiddleName();
		long prefixId = 0;
		long suffixId = 0;
		boolean male = true;
		int birthdayMonth = Calendar.JANUARY;
		int birthdayDay = 1;
		int birthdayYear = 1970;
		String jobTitle = new String("");
		long[] groupIds = null;
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;
		boolean sendEmail = false;

		ServiceContext serviceContext = new ServiceContext();

		userByScreenName = userLocalService.addUser(
			creatorUserId, companyId, autoPassword, password1, password2,
			autoScreenName, screenName, emailAddress, facebookId, null, locale,
			firstName, middleName, lastName, prefixId, suffixId, male,
			birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
			organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

		userByScreenName = userLocalService.updatePasswordReset(userByScreenName.getUserId(), false);

		return userByScreenName.getUserId();

	}
	
	protected void checkAddUser(long companyId, String emailAddress) throws PortalException {
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnectUserInfoProcessorImpl - checkAddUser: " + emailAddress + " e " + companyId);	
		}

		Company company = companyLocalService.getCompany(companyId);

		if (!company.isStrangers()) {
			throw new StrangersNotAllowedException(companyId);
		}

		if (!company.isStrangersWithMx() && company.hasCompanyMx(emailAddress)) {
			throw new UserEmailAddressException.MustNotUseCompanyMx(emailAddress);
		}
		
	}
	
	/**
	 * Metodo responsavel atualizar o email do usuario na base do liferay caso necessario.
	 * 
	 * @param userByScreenName
	 * @param userInfoRHSSO
	 */
	private void updateEmailUserLiferay(UserInfo userInfo, User userByScreenName, long companyId) {
		
		try {
			
			String emailRHSSO = userInfo.getStringClaim("email");
			
			if (userByScreenName.getScreenName().equals(userInfo.getStringClaim("id"))) {
				
				// Atualizar apenas o email
				userByScreenName.setEmailAddress(emailRHSSO);
									
				//Faz o update do usuario
				userLocalService.updateUser(userByScreenName);
							
				log.debug("OpenIdConnectUserInfoProcessorImpl - Atualizacao automatica do email do usuario no Liferay efetuado com sucesso."
						+ " ScreenName: " + userByScreenName.getScreenName() 
						+ " E-mail: " + userByScreenName.getEmailAddress());
				
				return;
				
			}
			
		} catch (Exception e) {
			
			String errorMessage = e.getMessage();
			    
		    if (errorMessage != null && errorMessage.contains("ConstraintViolationException")) {
		        
				log.error("OpenIdConnectUserInfoProcessorImpl - Ocorreu um erro ao atualizar o email do usuario no Liferay. Já existe um outro usuario com o mesmo email na base do Liferay."
						+ " ScreenName: " + userInfo.getStringClaim("id") 
						+ " E-mail: " + userInfo.getStringClaim("email") 
						+ " Erro: " + e.getMessage() + ".");
				
				return;
		        
		    }
		    
			log.error("OpenIdConnectUserInfoProcessorImpl - Ocorreu um erro ao atualizar o email do usuario no Liferay."
					+ " ScreenName: " + userInfo.getStringClaim("id")  
					+ " E-mail: " + userInfo.getStringClaim("email")  
					+ " Erro: " + e.getMessage() + ".");
			
		}
	
	}

}