package br.com.haroldo.openidconnect.utils;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectSession;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import br.com.haroldo.openidconnect.OpenidConnect;

/**
 * @author Haroldo.Nobrega
 */
public class OpenIdConnectUserInfo {
	
	private static Log log = LogFactoryUtil.getLog(OpenidConnect.class);
	private static final String ERRO_USERINFO = "Ocorreu um erro ao processar o UserInfo: ";
	private static final String ERRO_DECODE_JWT = "Ocorreu um erro ao processar o payload do JWT: ";
	private static final String JWT_INVALIDO = "OpenIdConnect - JWT invalido retornado pela autenticacao RHSSO.";
	
	private OpenIdConnectUserInfo() {}
	
	/**
	 * Funcao responsavel de obter o Access Token e processa-lo para obter o id(screename e email)
	 * 
	 * @param openIdConnectSession
	 * @return userInfo em String
	 * @throws Exception Caso ocorra algum erro.
	 */
	public static Map<String, String> processUserInfo(OpenIdConnectSession openIdConnectSession) throws Exception {
		
		Map<String, String> userInfo = new HashMap<>();
		
		try {
			
			// JWT simples (sem assinatura), geralmente contém três partes: header, payload, e signature.
			String jwt = openIdConnectSession.getAccessTokenValue();

			// Extrair e decodificar o payload do JWT
			String payload = decodeJwtPayload(jwt);

			// Converter o payload em um JSONObject
			JSONObject jwtJson = JSONFactoryUtil.createJSONObject(payload);
			
			userInfo.put("screenName", jwtJson.getString("id"));
			userInfo.put("email", jwtJson.getString("email"));

			return userInfo;
			
		} catch(Exception e) {
			
			if (log.isErrorEnabled()) {
				log.error(ERRO_USERINFO + e);
			}

			return null;
			
		}

	}
	
	/**
	 * Funcao responsavel por processar o JWT e retornar o payload do token
	 * 
	 * @param jwt
	 * @return payload em String
	 * @throws Exception Caso ocorra algum erro.
	 */
	private static String decodeJwtPayload(String jwt) throws Exception {
		
		try {
			
			// O JWT é composto por três partes: header, payload, e signature
			// As partes são separadas por ponto, então pegamos a segunda parte (payload)
			String[] parts = jwt.split("\\.");

			if (parts.length != 3) {
				
				if (log.isErrorEnabled()) {
					log.error(JWT_INVALIDO);
				}
				
			}

			// Decodificar a segunda parte (payload) do JWT
			String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

			return payload;
			 
		} catch(Exception e) {
			
			if (log.isErrorEnabled()) {
				log.error(ERRO_DECODE_JWT + e);
			}
			
			return null;
			
		}

	}

}