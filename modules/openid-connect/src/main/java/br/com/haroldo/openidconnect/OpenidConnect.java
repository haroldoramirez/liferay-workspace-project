package br.com.haroldo.openidconnect;

import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.BaseAutoLogin;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnect;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectFlowState;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectSession;
import com.liferay.portal.security.sso.openid.connect.constants.OpenIdConnectWebKeys;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import br.com.haroldo.openidconnect.utils.OpenIdConnectUserInfo;

/**
 * @author Haroldo.Nobrega
 */
@Component(
    immediate = true,
    service = AutoLogin.class,
    property = {
        "service.ranking:Integer=100" // Alta prioridade para interceptar o servico original
    }
)
public class OpenidConnect extends BaseAutoLogin {
	
	@Reference
	private Http http;
		
	@Reference
	private OpenIdConnect openIdConnect;

	@Reference
	private Portal portal;

	@Reference
	private UserLocalService userLocalService;
		
	private Log log = LogFactoryUtil.getLog(OpenidConnect.class);
	private static final String ERRO_DOLOGIN = "OpenIdConnect Login - Ocorreu um erro ao realizar o OpenIdConnect Custom Login: ";
	private static final String NAO_ENCONTRADO_USUARIO_BY_SCREENNAME = "OpenIdConnect Login - Nao foi possivel encontrar o usuario pelo screenName: ";
	private static final String NAO_ENCONTRADO_USUARIO_BY_EMAILADDRESS = "OpenIdConnect Login - Nao foi possivel encontrar o usuario pelo email: ";
	
	HttpSession httpSession = null;
	
	/**
	 * Funcao responsavel por interceptar o processo de login do Liferay para atender os requisitos do Cliente
	 * 
	 * @param request, response
	 * @return String[]
	 * @throws IOException, ServletException Caso ocorra algum erro.
	 */
	@Override
	protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String, String> userInfoRHSSO = new HashMap<>();
		long companyId = 0L;
		OpenIdConnectSession openIdConnectSession = null;
		User userByScreenName = null;
		
		try {
						
			companyId = portal.getCompanyId(request);
			
			if (!openIdConnect.isEnabled(companyId)) {
				
				if (log.isDebugEnabled()) {
					log.debug("OpenIdConnect Login - Configuracao RHSSO esta desativada!");
				}
				
				return null;
			}

			httpSession = request.getSession(false);

			if (httpSession == null) {
				return null;
			}

			openIdConnectSession = (OpenIdConnectSession) httpSession.getAttribute(OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION);
			
			if (openIdConnectSession == null) {
				
				if (log.isDebugEnabled()) {
					log.debug("OpenIdConnect Login - Session OpenID nao encontrada!");
				}
				
				return null;
			}
			
			OpenIdConnectFlowState openIdConnectFlowState = openIdConnectSession.getOpenIdConnectFlowState();
			
			// Processo de Status Original do Liferay
			if (OpenIdConnectFlowState.AUTH_COMPLETE.equals(openIdConnectFlowState)) {
				
				if (log.isDebugEnabled()) {
					log.debug("OpenIdConnect Login status: " + openIdConnectFlowState);
				}

				userInfoRHSSO = OpenIdConnectUserInfo.processUserInfo(openIdConnectSession);
				
				if (userInfoRHSSO != null && !userInfoRHSSO.isEmpty()) {
					
					if (log.isDebugEnabled()) {
						log.debug("OpenIdConnect Login - Retorno do usuario apos a autenticacao pelo RHSSO. Com screenName: " + userInfoRHSSO.get("screenName") + " e email:" + userInfoRHSSO.get("email"));
					}
										
					userByScreenName = userLocalService.getUserByScreenName(companyId, userInfoRHSSO.get("screenName"));
						
					if (log.isDebugEnabled()) {
						log.debug("OpenIdConnect Login - Usuario encontrado na base do Liferay pelo ScreenName.");
					}
					
					deleteOrUpdateUser(companyId, userByScreenName, userInfoRHSSO);
										
					return setUserCredentials(userByScreenName, openIdConnectSession);
					
				}
				
			}
									
		} catch(NoSuchUserException nse) {
			
			log.error(NAO_ENCONTRADO_USUARIO_BY_SCREENNAME + userInfoRHSSO);
			
			User userByEmail = userLocalService.getUserByEmailAddress(companyId, userInfoRHSSO.get("email"));
			
			if (userByEmail != null) {
										        		        				
				log.error("OpenIdConnect Login - Ja existe um usuario cadastrado com o mesmo e-mail na base de usuarios do Liferay. screenName: " + userByEmail.getScreenName() + " com email: " + userByEmail.getEmailAddress() + " Usuario nao autenticado.");
				
				sendUnauthorizedErrorClient(request, response, "OpenIdConnect Login - Ja existe um usuario cadastrado com o mesmo e-mail na base do Liferay. Usuario nao autenticado. Redirecionado para a HOME.", userInfoRHSSO);
																
			}
												
		} catch(Exception e) {
			
			log.error(ERRO_DOLOGIN + e);
			
		}
		
		/**
		 * Manter o fluxo original do Liferay
		 *  
		 */
		
		return null;
				
	}
		
	/**
	 * Metodo responsavel por setar as credenciais do usuario encontrado pelo screenName
	 * 
	 */
	private String[] setUserCredentials(User user, OpenIdConnectSession openIdConnectSession) {
		
		String[] credentials = new String[3];

		credentials[0] = String.valueOf(user.getUserId());
		credentials[1] = user.getPassword();
		credentials[2] = Boolean.TRUE.toString();
		
		openIdConnectSession.setOpenIdConnectFlowState(OpenIdConnectFlowState.PORTAL_AUTH_COMPLETE);
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnect Login - Usuario com screenName: " + user.getScreenName() + " email:" + user.getDisplayEmailAddress() + " autenticado com sucesso.");
		}
		
		return credentials;
		
	}
					
	/**
	 * Metodo responsavel por retornar o erro personalizado 401 para ao cliente e continuar com o fluxo original do Liferay
	 * 
	 */	
	private void sendUnauthorizedErrorClient(HttpServletRequest request, HttpServletResponse response, String message, Map<String, String> userInfoRHSSO) throws IOException {
		
		String internalPath = "";
		
		try {
			
			// Verifica se a resposta já foi enviada
			if (response.isCommitted()) {
				log.error("OpenIdConnect Login - A resposta ja foi comprometida por outro processo.");
				return;
			}
			
			/**
			 * 
			 * O erro java.lang.IllegalStateException: UT010019: Response already committed ocorre porque o código está tentando modificar o HttpServletResponse 
			 * depois que ele ja foi enviado ao cliente pelo proprio Liferay. Isso nao e erro do nosso modulo personalizado.
			 * 
			 * Mas pela logica implementada alem de evitar o erro de committed, tambem evita o erro too many redirects no lado do cliente.
			 * Tambem e possivel adicionar no header do response, uma mensagem personalizada apos erro na autenticacao.
			 * 
			 * */
			
			invalidateOpenIdSession();
			
			response.setHeader("OpenidConnect-Error", message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			
			String homeURL = portal.getHomeURL(request);
			internalPath = portal.getPathMain() + new URL(homeURL).getPath();

			RequestDispatcher dispatcher = request.getRequestDispatcher(internalPath);
			dispatcher.forward(request, response);
			
		} catch (Exception e) {
			
			log.error("OpenIdConnect Login - Usuario nao autenticado."
					+ " ScreenName: " + userInfoRHSSO.get("screenName") 
					+ " E-mail: " + userInfoRHSSO.get("email") 
					+ " Erro: " + e.getMessage() + "."
					+ " Redirecionado para a HOME: " + internalPath);
			
		}
		
		return;
		
	}
		
	/**
	 * Metodo responsavel por invalidar a sessao
	 * 
	 */	
	private void invalidateOpenIdSession() {
		httpSession.removeAttribute(OpenIdConnectWebKeys.OPEN_ID_CONNECT_SESSION);
		httpSession.invalidate();
	}
	
	/**
	 * Metodo responsavel por remover um usuario importado recentemente e atualizar o email do usuario que esta logado
	 * 
	 * @param companyId
	 * @param userByScreenName
	 * @param userInfoRHSSO
	 */	
	private void deleteOrUpdateUser(long companyId, User userByScreenName, Map<String, String> userInfoRHSSO) {
				
		try {
						
			User userByEmail = userLocalService.getUserByEmailAddress(companyId, userInfoRHSSO.get("email"));
						
			if (!userByScreenName.getEmailAddress().equals(userByEmail.getEmailAddress())) {
				
				if (log.isDebugEnabled()) {
					log.debug("OpenIdConnect Login - Atualizacao paliativa de usuario logado.");
				}
				
				deleteUser(userByEmail);
				
				userByScreenName.setEmailAddress(userInfoRHSSO.get("email"));
									
				updateUser(userByScreenName);
						
				if (log.isDebugEnabled()) {
					log.debug("OpenIdConnect Login - Atualizacao automatica do email do usuario no Liferay efetuado com sucesso."
							+ " ScreenName: " + userByScreenName.getScreenName() 
							+ " E-mail: " + userByScreenName.getEmailAddress());
				}
								
			}
			
		} catch(NoSuchUserException nse) {
			
			log.warn(NAO_ENCONTRADO_USUARIO_BY_EMAILADDRESS + userInfoRHSSO);
															
		} catch(Exception e) {
			
			log.error(ERRO_DOLOGIN + e);
			
		}
		
		return;
		
	}
	
	private void updateUser(User userByScreeName) {
		
		try {
			
			if (log.isDebugEnabled()) {
				log.debug("OpenIdConnect Login - Update do usuario no Liferay com screeName: " + userByScreeName.getScreenName() + " e-email: " + userByScreeName.getEmailAddress());
			}
			
			userLocalService.updateUser(userByScreeName);
			
		} catch (Exception e) {
			
			log.error("OpenIdConnect Login - Ocorreu um erro ao fazer o update do usuario no Liferay."
					+ " ScreenName: " + userByScreeName.getScreenName()  
					+ " E-mail: " + userByScreeName.getEmailAddress()  
					+ " Erro: " + e.getMessage() + ".");
			
		}
		
	}
	
	private void deleteUser(User userByEmail) {
				
		try {
			
			if (log.isDebugEnabled()) {
				log.debug("OpenIdConnect Login - deletar do usuario no Liferay com screeName: " + userByEmail.getScreenName() + " e-email: " + userByEmail.getEmailAddress());
			}
			
			userLocalService.deleteUser(userByEmail);
			
		} catch (Exception e) {
			
			log.error("OpenIdConnect Login - Ocorreu um erro ao deletar do usuario no Liferay."
					+ " ScreenName: " + userByEmail.getScreenName()  
					+ " E-mail: " + userByEmail.getEmailAddress()  
					+ " Erro: " + e.getMessage() + ".");
			
		}
		
	}
		
}