Módulo OpenID Connect Exposer
==============================

O Liferay, como qualquer aplicação OSGi, isola pacotes entre módulos. Classes marcadas como internal não são exportadas por padrão. Por isso, quando você tenta importar algo através do modulo openid-connect como:

`import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessorImpl;`

O OSGi tentará localizar o pacote com.liferay.portal.security.sso.openid.connect.internal em algum bundle que o exporte. Como o bundle original (.impl) não exporta esse pacote, o módulo openid-connect não consegue resolver a dependência, resultando no seguinte erro ao fazer o deployment:

`Unresolved requirement: Import-Package: com.liferay.portal.security.sso.openid.connect.internal`

O principal objetivo do openid-connect-exposer-fragment é “injetar” a exportação desses pacotes.

Detalhes:

Um Module Fragment nunca é ativado sozinho. Após o deployment deste pacote sem erros podemos verificar seu Status como RESOLVED na tela administrativa do Liferay;

A função principal é ser anexado ao bundle “host” (com.liferay.portal.security.sso.openid.connect.impl) em tempo de execução;
Fazendo com que seja possível compartilhar o classloader do host, podendo acessar suas classes privadas, sobrescrevê-las, exportá-las ou modificá-las. Respeitando as boas práticas implementadas no OSGI e Liferay;

Devemos fazer o deployment deste pacote .jar antes do módulo openid-connect.
