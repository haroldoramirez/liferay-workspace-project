package br.com.haroldo.openidconnect.utils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import br.com.haroldo.openidconnect.filter.OpenIdConnectLoginFilter;

/**
 * @author Haroldo.Nobrega
 */
public class OpenIdConnectFilterUtil {
	
	private static Log log = LogFactoryUtil.getLog(OpenIdConnectLoginFilter.class);
	private static final String ERRO_QUERY_PARAMETERS = "Ocorreu um erro ao processar os query parameters: ";
	private static final String ERRO_REDIRECT_URL = "Ocorreu um erro ao processar os parametros da URL atual: ";
	
	/**
	 * Funcao responsavel por obter os parametros da URL
	 * 
	 * @param request
	 * @return parameters
	 * @throws Exception Caso ocorra algum erro.
	 */
	public static String getRedirectParameter(HttpServletRequest request) {
		
		try {
			
			// Obtem a URL completa do atributo
			String currentUrl = (String) request.getAttribute("CURRENT_URL");

			if (currentUrl == null) {
				return null; // Retorna nulo se não existir uma URL
			}
			
			if (currentUrl.contains("?")) {
				
				// Obtem a parte apos o "?" (query string)
				String queryString = currentUrl.split("\\?", 2)[1];

				// Processa os parametros da query string
				Map<String, String> parameters = getQueryParameters(queryString);

				// Retorna o valor do parametro "redirect"
				return parameters.get("redirect");
				
			}

			return currentUrl;
			
		} catch (Exception e) {
			
			log.error(ERRO_REDIRECT_URL + e);
			return null;
			
		}
		
	}

	/**
	 * Funcao responsavel por montar query parameters em um map
	 * 
	 * @param queryString
	 * @return queryParameters um Map
	 * @throws Exception Caso ocorra algum erro.
	 */
	private static Map<String, String> getQueryParameters(String queryString) throws Exception {
				
		try {
			
			Map<String, String> queryParameters = new HashMap<>();
			
			String[] pairs = queryString.split("&");
			
			for (String pair : pairs) {
				
				String[] keyValue = pair.split("=", 2);
				String key = URLDecoder.decode(keyValue[0], "UTF-8");
				String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
				queryParameters.put(key, value);
				
			}
			
			return queryParameters;
			
		} catch (Exception e) {
			
			if (log.isErrorEnabled()) {
				log.error(ERRO_QUERY_PARAMETERS + e);
			}
			
			return null;
		}

	}

}