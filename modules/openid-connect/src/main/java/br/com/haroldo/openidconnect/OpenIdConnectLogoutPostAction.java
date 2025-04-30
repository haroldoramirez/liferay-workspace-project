package br.com.haroldo.openidconnect;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

import br.com.haroldo.openidconnect.utils.OpenIdConnectFilterUtil;

@Component(
	immediate = true,
	property = "key=logout.events.post",
	service = LifecycleAction.class
)
public class OpenIdConnectLogoutPostAction implements LifecycleAction {
	
	private Log log = LogFactoryUtil.getLog(OpenIdConnectLogoutPostAction.class);
	
	//Exemplo de logout com LogoutPostAction nao testado

	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnectLogoutPostAction - processLifecycleEvent");
		}

		HttpServletResponse response = lifecycleEvent.getResponse();
		HttpServletRequest request = lifecycleEvent.getRequest();
		
		String currentURL = OpenIdConnectFilterUtil.getRedirectParameter(request);
		
		if (log.isDebugEnabled()) {
			log.debug("OpenIdConnectLogoutPostAction - currentURL: " + currentURL);
		}

		//Executar uma logica para verifica o tipo de logout
		//Logica para montar a url conforme abaixo

		String endSessionUrl = "https://<seu-rhsso>/protocol/openid-connect/logout?id_token_hint=...&post_logout_redirect_uri=...";

		try {
			
	        // Redireciona para o logout do RHSSO (Keycloak, por exemplo)
			response.sendRedirect(endSessionUrl);
			
		} catch (IOException e) {
			
			log.debug("OpenIdConnectLogoutPostAction - Erro ao redirecionar para logout do RHSSO", e);
			
			throw new ActionException();
			
		}

	}

}