Módulo OpenID Connect Exposer
==============================

O Liferay, como qualquer aplicação OSGi, isola pacotes entre módulos. Classes marcadas como internal não são exportadas por padrão. Por isso, quando você tenta importar algo como:

`import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessorImpl;`

O OSGi vai tentar encontrar o pacote com.liferay.portal.security.sso.openid.connect.internal em algum bundle que o exporte. Como o bundle original (.impl) não exporta esse pacote, o módulo opendi-connect não consegue resolver a dependência e gera o erro abaixo quando fazemos o deployment do módulo openid-connect:

`Unresolved requirement: Import-Package: com.liferay.portal.security.sso.openid.connect.internal`

O principal objetivo do openid-connect-exposer-fragment é “injetar” a exportação desses pacotes.

Detalhes:

Um Module Fragment nunca é ativado sozinho. Na tela administrativa do liferay o Status de um Fragment fica RESOLVED após o deployment sem erros;

A função principal é ser anexado ao bundle “host” (com.liferay.portal.security.sso.openid.connect.impl) em tempo de execução.
Fazendo com que seja possível compartilhar o classloader do host, podendo acessar suas classes privadas, sobrescrevê-las, exportá-las ou modificá-las. Respeitando as boas práticas implementadas no OSGI e Liferay;

Devemos fazer o deployment deste pacote .jar antes do módulo openid-connect.
